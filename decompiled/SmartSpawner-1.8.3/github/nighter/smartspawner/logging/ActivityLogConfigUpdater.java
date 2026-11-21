package github.nighter.smartspawner.logging;

import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.updates.ConfigMigrations;
import github.nighter.smartspawner.updates.YamlMigrator;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public class ActivityLogConfigUpdater {
   public static final String FILE_NAME = "activity_log.yml";
   private static final String LEGACY_FILE_NAME = "discord_logging.yml";
   private static final String LEGACY_CONFIG_SECTION = "logging";
   private static final String FILE_SECTION = "file";
   private final SmartSpawner plugin;

   public ActivityLogConfigUpdater(SmartSpawner plugin) {
      this.plugin = plugin;
   }

   public void checkAndUpdate() {
      File file = new File(this.plugin.getDataFolder(), "activity_log.yml");
      if (importLegacySources(this.plugin.getDataFolder(), this.plugin.getLogger())) {
         this.plugin.reloadConfig();
      }

      boolean legacyLayout = isLegacyLayout(file);
      YamlMigrator.migrate(
         file, this.plugin.getResource("activity_log.yml"), ConfigMigrations.ACTIVITY_LOG, ConfigMigrations.ACTIVITY_LOG_LAYOUT, this.plugin.getLogger()
      );
      if (legacyLayout) {
         rebuildFromDefaults(file, this.plugin.getResource("activity_log.yml"), this.plugin.getLogger());
      }
   }

   static boolean importLegacySources(File dataFolder, Logger log) {
      File file = new File(dataFolder, "activity_log.yml");
      renameLegacyFile(dataFolder, file, log);
      return importFileLoggingFromConfig(dataFolder, file, log);
   }

   static boolean isLegacyLayout(File file) {
      return file.exists() && !YamlConfiguration.loadConfiguration(file).contains("discord");
   }

   private static void renameLegacyFile(File dataFolder, File file, Logger log) {
      if (!file.exists()) {
         File legacy = new File(dataFolder, "discord_logging.yml");
         if (legacy.exists()) {
            if (legacy.renameTo(file)) {
               log.info("Renamed discord_logging.yml to activity_log.yml");
            } else {
               log.warning("Could not rename discord_logging.yml to activity_log.yml, the Discord settings in it will be ignored.");
            }
         }
      }
   }

   static void rebuildFromDefaults(File file, InputStream resource, Logger log) {
      if (resource != null) {
         YamlConfiguration rebuilt;
         try (InputStreamReader reader = new InputStreamReader(resource, StandardCharsets.UTF_8)) {
            rebuilt = YamlConfiguration.loadConfiguration(reader);
         } catch (IOException var10) {
            log.log(Level.WARNING, "Could not read the bundled activity_log.yml", (Throwable)var10);
            return;
         }

         YamlConfiguration user = YamlConfiguration.loadConfiguration(file);

         for (String path : user.getKeys(true)) {
            if (!user.isConfigurationSection(path)) {
               rebuilt.set(path, user.get(path));
            }
         }

         try {
            rebuilt.save(file);
         } catch (IOException var8) {
            log.log(Level.WARNING, "Failed to rewrite activity_log.yml", (Throwable)var8);
         }
      }
   }

   private static boolean importFileLoggingFromConfig(File dataFolder, File file, Logger log) {
      File configFile = new File(dataFolder, "config.yml");
      if (!configFile.exists()) {
         return false;
      } else {
         YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
         ConfigurationSection logging = config.getConfigurationSection("logging");
         if (logging == null) {
            return false;
         } else {
            YamlConfiguration activityLog = YamlConfiguration.loadConfiguration(file);

            for (String key : logging.getKeys(true)) {
               if (!logging.isConfigurationSection(key)) {
                  String path = "file." + key;
                  if (!activityLog.contains(path)) {
                     activityLog.set(path, logging.get(key));
                  }
               }
            }

            try {
               activityLog.save(file);
            } catch (IOException var11) {
               log.log(Level.SEVERE, "Failed to move the logging settings into activity_log.yml", (Throwable)var11);
               return false;
            }

            config.set("logging", null);

            try {
               config.save(configFile);
            } catch (IOException var10) {
               log.log(Level.WARNING, "Failed to remove the old logging section from config.yml", (Throwable)var10);
               return false;
            }

            log.info("Moved the logging settings from config.yml into activity_log.yml");
            return true;
         }
      }
   }
}
