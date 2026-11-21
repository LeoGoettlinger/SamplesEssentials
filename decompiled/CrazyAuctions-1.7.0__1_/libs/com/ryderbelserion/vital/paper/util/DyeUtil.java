package libs.com.ryderbelserion.vital.paper.util;

import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DyeUtil {
   private DyeUtil() {
      throw new AssertionError();
   }

   @NotNull
   public static DyeColor getDyeColor(@NotNull String value) {
      if (value.isEmpty()) {
         return DyeColor.WHITE;
      } else {
         String var1 = value.toUpperCase();

         return switch (var1) {
            case "ORANGE" -> DyeColor.ORANGE;
            case "MAGENTA" -> DyeColor.MAGENTA;
            case "LIGHT_BLUE" -> DyeColor.LIGHT_BLUE;
            case "YELLOW" -> DyeColor.YELLOW;
            case "LIME" -> DyeColor.LIME;
            case "PINK" -> DyeColor.PINK;
            case "GRAY" -> DyeColor.GRAY;
            case "LIGHT_GRAY" -> DyeColor.LIGHT_GRAY;
            case "CYAN" -> DyeColor.CYAN;
            case "PURPLE" -> DyeColor.PURPLE;
            case "BLUE" -> DyeColor.BLUE;
            case "BROWN" -> DyeColor.BROWN;
            case "GREEN" -> DyeColor.GREEN;
            case "RED" -> DyeColor.RED;
            case "BLACK" -> DyeColor.BLACK;
            default -> DyeColor.WHITE;
         };
      }
   }

   @NotNull
   public static Color getDefaultColor(@NotNull String color) {
      if (color.isEmpty()) {
         return Color.WHITE;
      } else {
         String var1 = color.toUpperCase();

         return switch (var1) {
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
            case "YELLOW" -> Color.YELLOW;
            default -> Color.WHITE;
         };
      }
   }

   @Nullable
   public static Color getColor(@NotNull String color) {
      if (color.isEmpty()) {
         return null;
      } else {
         String[] rgb = color.split(",");
         if (rgb.length != 3) {
            return null;
         } else {
            int red = Integer.parseInt(rgb[0]);
            int green = Integer.parseInt(rgb[1]);
            int blue = Integer.parseInt(rgb[2]);
            return Color.fromRGB(red, green, blue);
         }
      }
   }
}
