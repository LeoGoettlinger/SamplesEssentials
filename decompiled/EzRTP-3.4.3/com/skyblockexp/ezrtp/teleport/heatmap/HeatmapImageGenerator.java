package com.skyblockexp.ezrtp.teleport.heatmap;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public final class HeatmapImageGenerator {
   private static final int DEFAULT_MAP_SIZE = 128;
   private static final Color[] HEATMAP_COLORS = new Color[]{
      new Color(253, 231, 37), new Color(255, 153, 51), new Color(255, 69, 0), new Color(220, 0, 0), new Color(128, 0, 0)
   };
   private final int mapSize;

   public HeatmapImageGenerator() {
      this(128);
   }

   public HeatmapImageGenerator(int mapSize) {
      if (mapSize > 0 && mapSize <= 1024) {
         this.mapSize = mapSize;
      } else {
         throw new IllegalArgumentException("Map size must be between 1 and 1024 pixels");
      }
   }

   public BufferedImage generate(HeatmapGenerator.HeatmapData heatmapData, int centerX, int centerZ, int radius) {
      return this.generate(heatmapData, centerX, centerZ, radius, Collections.emptyList(), ClaimOverlaySettings.defaults());
   }

   public BufferedImage generate(
      HeatmapGenerator.HeatmapData heatmapData, int centerX, int centerZ, int radius, List<ClaimChunkOverlay> claimChunks, ClaimOverlaySettings overlaySettings
   ) {
      if (heatmapData != null && heatmapData.getTotalLocations() != 0) {
         Map<HeatmapGenerator.GridCell, Integer> data = heatmapData.getData();
         if (data.isEmpty()) {
            return this.createEmptyMap();
         } else {
            int gridSize = heatmapData.getGridSize();
            int minCellX = Integer.MAX_VALUE;
            int maxCellX = Integer.MIN_VALUE;
            int minCellZ = Integer.MAX_VALUE;
            int maxCellZ = Integer.MIN_VALUE;

            for (HeatmapGenerator.GridCell cell : data.keySet()) {
               if (cell.x() < minCellX) {
                  minCellX = cell.x();
               }

               if (cell.x() > maxCellX) {
                  maxCellX = cell.x();
               }

               if (cell.z() < minCellZ) {
                  minCellZ = cell.z();
               }

               if (cell.z() > maxCellZ) {
                  maxCellZ = cell.z();
               }
            }

            int dataMinX = minCellX * gridSize;
            int dataMaxX = (maxCellX + 1) * gridSize - 1;
            int dataMinZ = minCellZ * gridSize;
            int dataMaxZ = (maxCellZ + 1) * gridSize - 1;
            boolean autoFit = radius <= 0
               || dataMinX < centerX - radius
               || dataMaxX > centerX + radius
               || dataMinZ < centerZ - radius
               || dataMaxZ > centerZ + radius;
            int minWorldX;
            int maxWorldX;
            int minWorldZ;
            int maxWorldZ;
            if (autoFit) {
               minWorldX = dataMinX;
               maxWorldX = dataMaxX;
               minWorldZ = dataMinZ;
               maxWorldZ = dataMaxZ;
            } else {
               minWorldX = centerX - radius;
               maxWorldX = centerX + radius;
               minWorldZ = centerZ - radius;
               maxWorldZ = centerZ + radius;
            }

            BufferedImage image = new BufferedImage(this.mapSize, this.mapSize, 2);
            Graphics2D g2d = image.createGraphics();
            g2d.setColor(new Color(20, 20, 30));
            g2d.fillRect(0, 0, this.mapSize, this.mapSize);
            int maxCount = heatmapData.getMaxCount();
            boolean allSingle = maxCount == 1;

            for (Entry<HeatmapGenerator.GridCell, Integer> entry : data.entrySet()) {
               HeatmapGenerator.GridCell cell = entry.getKey();
               int count = entry.getValue();
               int cellWorldX = cell.x() * gridSize + gridSize / 2;
               int cellWorldZ = cell.z() * gridSize + gridSize / 2;
               double relX = (double)(cellWorldX - minWorldX) / Math.max(1, maxWorldX - minWorldX);
               double relZ = (double)(cellWorldZ - minWorldZ) / Math.max(1, maxWorldZ - minWorldZ);
               int pixelX = (int)(relX * this.mapSize);
               int pixelZ = (int)(relZ * this.mapSize);
               int pixelWidth = Math.max(1, (int)Math.ceil((double)this.mapSize * gridSize / Math.max(1, maxWorldX - minWorldX)));
               int pixelHeight = Math.max(1, (int)Math.ceil((double)this.mapSize * gridSize / Math.max(1, maxWorldZ - minWorldZ)));
               pixelWidth = Math.min(pixelWidth, this.mapSize - pixelX);
               pixelHeight = Math.min(pixelHeight, this.mapSize - pixelZ);
               double intensity = allSingle ? 0.0 : (double)(count - 1) / (maxCount - 1);
               Color color = this.getColorForIntensity(intensity);
               if (pixelWidth > 0 && pixelHeight > 0) {
                  int alpha = (int)(50.0 + 205.0 * intensity);
                  Color alphaColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
                  g2d.setColor(alphaColor);
                  g2d.fillRect(pixelX, pixelZ, pixelWidth, pixelHeight);
               }
            }

            this.drawLegend(g2d, this.mapSize);
            this.drawClaimOverlays(
               g2d,
               minWorldX,
               maxWorldX,
               minWorldZ,
               maxWorldZ,
               claimChunks != null ? claimChunks : Collections.emptyList(),
               overlaySettings != null ? overlaySettings : ClaimOverlaySettings.defaults()
            );
            g2d.dispose();
            return image;
         }
      } else {
         return this.createEmptyMap();
      }
   }

   private void drawClaimOverlays(
      Graphics2D g2d, int minWorldX, int maxWorldX, int minWorldZ, int maxWorldZ, List<ClaimChunkOverlay> claimChunks, ClaimOverlaySettings settings
   ) {
      if (!claimChunks.isEmpty() && settings.enabled() && settings.style() == ClaimOverlayStyle.BORDER) {
         g2d.setColor(settings.color());
         g2d.setStroke(new BasicStroke(settings.lineWidth()));
         int worldWidth = Math.max(1, maxWorldX - minWorldX);
         int worldHeight = Math.max(1, maxWorldZ - minWorldZ);

         for (ClaimChunkOverlay chunk : claimChunks) {
            int chunkMinX = chunk.chunkX() * 16;
            int chunkMaxX = chunkMinX + 16;
            int chunkMinZ = chunk.chunkZ() * 16;
            int chunkMaxZ = chunkMinZ + 16;
            double relMinX = (double)(chunkMinX - minWorldX) / worldWidth;
            double relMaxX = (double)(chunkMaxX - minWorldX) / worldWidth;
            double relMinZ = (double)(chunkMinZ - minWorldZ) / worldHeight;
            double relMaxZ = (double)(chunkMaxZ - minWorldZ) / worldHeight;
            int px = Math.max(0, Math.min(this.mapSize - 1, (int)Math.round(relMinX * this.mapSize)));
            int pz = Math.max(0, Math.min(this.mapSize - 1, (int)Math.round(relMinZ * this.mapSize)));
            int pw = Math.max(1, (int)Math.round((relMaxX - relMinX) * this.mapSize));
            int ph = Math.max(1, (int)Math.round((relMaxZ - relMinZ) * this.mapSize));
            if (px < this.mapSize && pz < this.mapSize) {
               int maxWidth = this.mapSize - px - 1;
               int maxHeight = this.mapSize - pz - 1;
               g2d.drawRect(px, pz, Math.max(1, Math.min(pw, maxWidth)), Math.max(1, Math.min(ph, maxHeight)));
            }
         }
      }
   }

   private BufferedImage createEmptyMap() {
      BufferedImage image = new BufferedImage(this.mapSize, this.mapSize, 2);
      Graphics2D g2d = image.createGraphics();
      g2d.setColor(new Color(20, 20, 30));
      g2d.fillRect(0, 0, this.mapSize, this.mapSize);
      g2d.dispose();
      return image;
   }

   private Color getColorForIntensity(double intensity) {
      if (intensity <= 0.0) {
         return HEATMAP_COLORS[0];
      } else if (intensity >= 1.0) {
         return HEATMAP_COLORS[HEATMAP_COLORS.length - 1];
      } else {
         double scaledIntensity = intensity * (HEATMAP_COLORS.length - 1);
         int index = (int)Math.floor(scaledIntensity);
         double fraction = scaledIntensity - index;
         if (index >= HEATMAP_COLORS.length - 1) {
            return HEATMAP_COLORS[HEATMAP_COLORS.length - 1];
         } else {
            Color color1 = HEATMAP_COLORS[index];
            Color color2 = HEATMAP_COLORS[index + 1];
            int r = (int)(color1.getRed() + (color2.getRed() - color1.getRed()) * fraction);
            int g = (int)(color1.getGreen() + (color2.getGreen() - color1.getGreen()) * fraction);
            int b = (int)(color1.getBlue() + (color2.getBlue() - color1.getBlue()) * fraction);
            return new Color(r, g, b);
         }
      }
   }

   public int getMapSize() {
      return this.mapSize;
   }

   private void drawLegend(Graphics2D g2d, int mapSize) {
      int legendWidth = 80;
      int legendHeight = 12;
      int x = mapSize - legendWidth - 8;
      int y = mapSize - legendHeight - 8;

      for (int i = 0; i < legendWidth; i++) {
         double t = (double)i / (legendWidth - 1);
         Color c = this.getColorForIntensity(t);
         g2d.setColor(c);
         g2d.fillRect(x + i, y, 1, legendHeight);
      }

      g2d.setColor(Color.WHITE);
      g2d.drawRect(x, y, legendWidth, legendHeight);
      g2d.setFont(g2d.getFont().deriveFont(9.0F));
      g2d.drawString("Low", x, y - 2);
      g2d.drawString("High", x + legendWidth - 24, y - 2);
   }
}
