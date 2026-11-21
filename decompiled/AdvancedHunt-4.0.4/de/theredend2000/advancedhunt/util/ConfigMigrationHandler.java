package de.theredend2000.advancedhunt.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.function.Consumer;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigMigrationHandler {
   private static final VersionComparator VERSION_COMPARATOR = new VersionComparator();
   private static final NavigableMap<String, Consumer<FileConfiguration>> configMigrations = new TreeMap<>(VERSION_COMPARATOR);
   private static final NavigableMap<String, Consumer<FileConfiguration>> messageMigrations = new TreeMap<>(VERSION_COMPARATOR);
   private static final Map<String, String> LEGACY_PROGRESS_MENU_TITLES = createLegacyProgressMenuTitles();

   public static void migrateConfig(FileConfiguration config, String currentVersion, String newVersion) {
      applyMigrations(config, currentVersion, newVersion, configMigrations);
   }

   public static void migrateMessages(FileConfiguration config, String currentVersion, String newVersion) {
      applyMigrations(config, currentVersion, newVersion, messageMigrations);
   }

   static void applyMigrations(FileConfiguration config, String currentVersion, String newVersion, NavigableMap<String, Consumer<FileConfiguration>> migrations) {
      String normalizedCurrent = normalizeVersion(currentVersion);
      String normalizedNew = normalizeVersion(newVersion);
      if (VERSION_COMPARATOR.compare(normalizedCurrent, normalizedNew) < 0 && !migrations.isEmpty()) {
         for (Entry<String, Consumer<FileConfiguration>> entry : migrations.entrySet()) {
            String targetVersion = normalizeVersion(entry.getKey());
            if (VERSION_COMPARATOR.compare(targetVersion, normalizedCurrent) > 0 && VERSION_COMPARATOR.compare(targetVersion, normalizedNew) <= 0) {
               entry.getValue().accept(config);
            }
         }
      }
   }

   private static String normalizeVersion(String version) {
      String normalized = version;

      while (normalized.endsWith(".0")) {
         normalized = normalized.substring(0, normalized.length() - 2);
      }

      return normalized;
   }

   private static void migrateProgressMenuTitle(FileConfiguration config) {
      String currentTitle = config.getString("gui.progress.title");
      if (currentTitle != null) {
         String migratedTitle = LEGACY_PROGRESS_MENU_TITLES.get(currentTitle);
         if (migratedTitle != null) {
            config.set("gui.progress.title", migratedTitle);
         }
      }
   }

   private static Map<String, String> createLegacyProgressMenuTitles() {
      Map<String, String> titles = new LinkedHashMap<>();
      titles.put("&8Progress: %collection% &7[&e%progress%&7/&e%total% &7- &e%percentage%&7%]", "&8Progress: %collection%");
      titles.put("&8Fortschritt: %collection% &7[&e%progress%&7/&e%total% &7- &e%percentage%&7%]", "&8Fortschritt: %collection%");
      titles.put("&8Tiến trình: %collection% &7[&e%progress%&7/&e%total% &7- &e%percentage%&7%]", "&8Tiến trình: %collection%");
      titles.put("&8进度：%collection% &7[&e%progress%&7/&e%total% &7- &e%percentage%&7%]", "&8进度：%collection%");
      titles.put("&8Progress: %collection% &7[&e%progress%&7/&e%total% &7- &e%percentage%&7%]", "&8Progress: %collection%");
      return titles;
   }

   static {
      messageMigrations.put("2.1", ConfigMigrationHandler::migrateProgressMenuTitle);
   }
}
