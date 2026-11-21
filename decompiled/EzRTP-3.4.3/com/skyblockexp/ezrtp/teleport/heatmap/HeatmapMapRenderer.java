package com.skyblockexp.ezrtp.teleport.heatmap;

import java.awt.image.BufferedImage;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.jetbrains.annotations.NotNull;

public final class HeatmapMapRenderer extends MapRenderer {
   private final BufferedImage heatmapImage;
   private boolean hasRendered = false;

   public HeatmapMapRenderer(@NotNull BufferedImage heatmapImage) {
      super(false);
      this.heatmapImage = heatmapImage;
   }

   public void render(@NotNull MapView map, @NotNull MapCanvas canvas, @NotNull Player player) {
      if (!this.hasRendered) {
         canvas.drawImage(0, 0, this.heatmapImage);
         this.hasRendered = true;
      }
   }
}
