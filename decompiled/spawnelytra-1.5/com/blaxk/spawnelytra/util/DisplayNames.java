package com.blaxk.spawnelytra.util;

import java.util.Locale;

public enum DisplayNames {
   public static String language(String lang) {
      if (lang == null) {
         return "-";
      } else {
         String var1 = lang.toLowerCase(Locale.ROOT);

         return switch (var1) {
            case "de" -> "Deutsch";
            case "en" -> "English";
            case "es" -> "Español";
            case "fr" -> "Français";
            case "pl" -> "Polski";
            default -> lang;
         };
      }
   }

   public static String activationMode(String mode) {
      if (mode == null) {
         return "-";
      } else {
         String var1 = mode.toLowerCase(Locale.ROOT);

         return switch (var1) {
            case "double_jump" -> "Double Jump";
            case "auto" -> "Auto";
            case "sneak_jump" -> "Sneak Jump";
            case "f_key" -> "F Key";
            default -> mode;
         };
      }
   }

   public static String spawnMode(String mode) {
      if (mode == null) {
         return "-";
      } else {
         String var1 = mode.toLowerCase(Locale.ROOT);

         return switch (var1) {
            case "auto" -> "Auto";
            case "advanced" -> "Advanced";
            default -> mode;
         };
      }
   }
}
