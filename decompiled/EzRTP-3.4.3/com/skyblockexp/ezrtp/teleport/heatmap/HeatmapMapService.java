package com.skyblockexp.ezrtp.teleport.heatmap;

import com.skyblockexp.ezrtp.util.ItemFlagUtil;
import com.skyblockexp.ezrtp.util.MessageUtil;
import com.skyblockexp.ezrtp.util.compat.ItemMetaCompat;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

public final class HeatmapMapService {
   private static final int MIN_DATA_THRESHOLD = 5;
   private final Logger logger;

   public HeatmapMapService(Logger logger) {
      this.logger = logger;
   }

   public ItemStack createHeatmapMap(HeatmapGenerator.HeatmapData heatmapData, Player player, int centerX, int centerZ, int radius) {
      return this.createHeatmapMap(heatmapData, player, centerX, centerZ, radius, Collections.emptyList(), ClaimOverlaySettings.defaults());
   }

   public ItemStack createHeatmapMap(
      HeatmapGenerator.HeatmapData heatmapData,
      Player player,
      int centerX,
      int centerZ,
      int radius,
      List<ClaimChunkOverlay> claimOverlays,
      ClaimOverlaySettings overlaySettings
   ) {
      if (heatmapData != null && heatmapData.getTotalLocations() >= 5) {
         try {
            HeatmapImageGenerator imageGenerator = new HeatmapImageGenerator();
            BufferedImage heatmapImage = imageGenerator.generate(heatmapData, centerX, centerZ, radius, claimOverlays, overlaySettings);
            MapView mapView = Bukkit.createMap(player.getWorld());

            for (MapRenderer renderer : mapView.getRenderers()) {
               mapView.removeRenderer(renderer);
            }

            HeatmapMapRenderer heatmapRenderer = new HeatmapMapRenderer(heatmapImage);
            mapView.addRenderer(heatmapRenderer);
            ItemStack mapItem = new ItemStack(Material.FILLED_MAP);
            MapMeta mapMeta = (MapMeta)mapItem.getItemMeta();
            if (mapMeta != null) {
               mapMeta.setMapView(mapView);
               ItemMetaCompat.setDisplayName(mapMeta, MessageUtil.legacyToComponent("&6&lRTP Heatmap"));
               List<Component> lore = Arrays.asList(
                  MessageUtil.legacyToComponent("&7Total Locations: &f" + heatmapData.getTotalLocations()),
                  MessageUtil.legacyToComponent("&7Grid Cells: &f" + heatmapData.getOccupiedCells()),
                  MessageUtil.legacyToComponent("&7Grid Size: &f" + heatmapData.getGridSize() + " blocks")
               );
               ItemMetaCompat.setLore(mapMeta, lore);
               ItemFlagUtil.setItemMetaCompatibly(mapItem, mapMeta);
            }

            return mapItem;
         } catch (Exception var15) {
            this.logger.warning("Failed to create heatmap map: " + var15.getMessage());
            return null;
         }
      } else {
         return null;
      }
   }

   public boolean saveHeatmapAsPng(HeatmapGenerator.HeatmapData heatmapData, File outputFile, int centerX, int centerZ, int radius) {
      return this.saveHeatmapAsPng(heatmapData, outputFile, centerX, centerZ, radius, Collections.emptyList(), ClaimOverlaySettings.defaults());
   }

   public boolean saveHeatmapAsPng(
      HeatmapGenerator.HeatmapData heatmapData,
      File outputFile,
      int centerX,
      int centerZ,
      int radius,
      List<ClaimChunkOverlay> claimOverlays,
      ClaimOverlaySettings overlaySettings
   ) {
      if (heatmapData != null && heatmapData.getTotalLocations() >= 5) {
         try {
            HeatmapImageGenerator imageGenerator = new HeatmapImageGenerator();
            BufferedImage heatmapImage = imageGenerator.generate(heatmapData, centerX, centerZ, radius, claimOverlays, overlaySettings);
            File parentDir = outputFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
               parentDir.mkdirs();
            }

            ImageIO.write(heatmapImage, "PNG", outputFile);
            this.logger.info("Heatmap saved to: " + outputFile.getAbsolutePath());
            return true;
         } catch (IOException var11) {
            this.logger.warning("Failed to save heatmap PNG: " + var11.getMessage());
            return false;
         }
      } else {
         return false;
      }
   }

   public boolean hasEnoughData(HeatmapGenerator.HeatmapData heatmapData) {
      return heatmapData != null && heatmapData.getTotalLocations() >= 5;
   }

   public int getMinDataThreshold() {
      return 5;
   }
}
