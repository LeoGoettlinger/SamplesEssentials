package de.theredend2000.advancedhunt.migration.legacy;

public final class LegacyMigrationUtil {
   private LegacyMigrationUtil() {
   }

   public static String migrateLegacyPlaceholders(String command) {
      return command == null
         ? null
         : command.replace("%EGGS_FOUND%", "%found_treasures%")
            .replace("%EGGS_MAX%", "%max_treasures%")
            .replace("%TREASURES_FOUND%", "%found_treasures%")
            .replace("%TREASURES_MAX%", "%max_treasures%")
            .replace("%PLAYER%", "%player%")
            .replace("%PREFIX%", "%prefix%");
   }
}
