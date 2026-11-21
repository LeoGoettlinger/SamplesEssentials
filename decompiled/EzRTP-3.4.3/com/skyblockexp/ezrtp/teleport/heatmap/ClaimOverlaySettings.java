package com.skyblockexp.ezrtp.teleport.heatmap;

import java.awt.Color;
import org.bukkit.configuration.ConfigurationSection;

public record ClaimOverlaySettings(boolean enabled, ClaimOverlayStyle style, Color color, int lineWidth) {
   public static ClaimOverlaySettings fromConfiguration(ConfigurationSection baseConfiguration) {
      if (baseConfiguration == null) {
         return defaults();
      } else {
         boolean enabled = baseConfiguration.getBoolean("heatmap.claims-overlay.enabled", false);
         ClaimOverlayStyle style = ClaimOverlayStyle.parse(baseConfiguration.getString("heatmap.claims-overlay.style", "border"));
         String rawColor = baseConfiguration.getString("heatmap.claims-overlay.color", "#00FFFF");
         int lineWidth = Math.max(1, Math.min(4, baseConfiguration.getInt("heatmap.claims-overlay.line-width", 1)));
         return new ClaimOverlaySettings(enabled, style, parseColor(rawColor), lineWidth);
      }
   }

   public static ClaimOverlaySettings defaults() {
      return new ClaimOverlaySettings(false, ClaimOverlayStyle.BORDER, parseColor("#00FFFF"), 1);
   }

   private static Color parseColor(String rawHex) {
      if (rawHex == null) {
         return Color.CYAN;
      } else {
         String normalized = rawHex.trim();
         if (!normalized.startsWith("#")) {
            normalized = "#" + normalized;
         }

         try {
            return Color.decode(normalized);
         } catch (NumberFormatException var3) {
            return Color.CYAN;
         }
      }
   }
}
