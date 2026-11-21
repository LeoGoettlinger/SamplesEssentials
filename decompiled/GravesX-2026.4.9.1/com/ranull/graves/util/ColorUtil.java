package com.ranull.graves.util;

import java.util.Locale;
import org.bukkit.Color;
import org.bukkit.Particle.DustOptions;

public class ColorUtil {
   private ColorUtil() {
   }

   public static Color getColor(String colorName) {
      if (colorName != null && !colorName.isEmpty()) {
         String s = colorName;
         if (colorName.startsWith("#")) {
            Color hex = getColorFromHex(colorName);
            if (hex != null) {
               return hex;
            }

            s = "RED";
         }

         String var4 = s.toUpperCase(Locale.ROOT);

         return switch (var4) {
            case "AQUA" -> Color.AQUA;
            case "BLACK" -> Color.BLACK;
            case "BLUE" -> Color.BLUE;
            case "FUCHSIA" -> Color.FUCHSIA;
            case "GRAY" -> Color.GRAY;
            case "GREEN" -> Color.GREEN;
            case "LIME" -> Color.LIME;
            case "MAROON" -> Color.MAROON;
            case "NAVY" -> Color.NAVY;
            case "OLIVE" -> Color.OLIVE;
            case "ORANGE" -> Color.ORANGE;
            case "PURPLE" -> Color.PURPLE;
            case "RED" -> Color.RED;
            case "SILVER" -> Color.SILVER;
            case "TEAL" -> Color.TEAL;
            case "WHITE" -> Color.WHITE;
            case "YELLOW" -> Color.YELLOW;
            default -> null;
         };
      } else {
         return null;
      }
   }

   public static Color getColorFromHex(String hex) {
      if (hex != null && hex.length() == 7 && hex.charAt(0) == '#') {
         try {
            int r = Integer.parseInt(hex.substring(1, 3), 16);
            int g = Integer.parseInt(hex.substring(3, 5), 16);
            int b = Integer.parseInt(hex.substring(5, 7), 16);
            return Color.fromRGB(r, g, b);
         } catch (NumberFormatException var4) {
            return null;
         }
      } else {
         return null;
      }
   }

   public static DustOptions createDustOptionsFromHex(String hexColor, float size) {
      Color color = getColorFromHex(hexColor);
      return color == null ? null : new DustOptions(color, size);
   }
}
