package com.magmaguy.elitemobs.magmacore.nightbreak;

import org.bukkit.configuration.file.FileConfiguration;

public final class NightbreakChatStyle {
   private static final String GRADIENT = "<g:#8B0000:#CC4400:#DAA520>";
   private static final String SEPARATOR = "<g:#8B0000:#CC4400:#DAA520>" + "▬".repeat(71) + "</g>";
   private static final String LEGACY_SEPARATOR = "<g:#8B0000:#CC4400:#DAA520>" + "▬".repeat(80) + "</g>";

   private NightbreakChatStyle() {
   }

   public static String separator() {
      return SEPARATOR;
   }

   public static void migrateSeparators(FileConfiguration configuration, String... keys) {
      for (String key : keys) {
         if (LEGACY_SEPARATOR.equals(configuration.getString(key))) {
            configuration.set(key, SEPARATOR);
         }
      }
   }
}
