package github.nighter.smartspawner.hooks.economy;

import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.updates.ConfigMigrations;
import github.nighter.smartspawner.updates.YamlMigrator;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public class SellIntegrationConfigUpdater {
   public static final String FILE_NAME = "sell_integration.yml";
   private static final String CONFIG_FILE_NAME = "config.yml";
   private static final String LEGACY_CONFIG_SECTION = "sell_integration";
   private static final String LEGACY_PRICE_FILE_NAME = "item_prices.yml";
   private static final String PRICES_SECTION = "custom_prices.prices";
   private final SmartSpawner plugin;

   public SellIntegrationConfigUpdater(SmartSpawner plugin) {
      this.plugin = plugin;
   }

   public void checkAndUpdate() {
      if (importLegacySources(this.plugin.getDataFolder(), this.plugin.getLogger())) {
         this.plugin.reloadConfig();
      }

      YamlMigrator.migrate(
         new File(this.plugin.getDataFolder(), "sell_integration.yml"),
         this.plugin.getResource("sell_integration.yml"),
         ConfigMigrations.SELL_INTEGRATION,
         null,
         true,
         ConfigMigrations.SELL_INTEGRATION_PRICES,
         this.plugin.getLogger()
      );
   }

   static boolean importLegacySources(File dataFolder, Logger log) {
      File file = new File(dataFolder, "sell_integration.yml");
      boolean configRewritten = importSettingsFromConfig(dataFolder, file, log);
      importLegacyPrices(dataFolder, file, log);
      return configRewritten;
   }

   private static boolean importSettingsFromConfig(File dataFolder, File file, Logger log) {
      File configFile = new File(dataFolder, "config.yml");
      if (!configFile.exists()) {
         return false;
      } else {
         YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
         ConfigurationSection legacy = config.getConfigurationSection("sell_integration");
         if (legacy == null) {
            return false;
         } else {
            YamlConfiguration sellIntegration = YamlConfiguration.loadConfiguration(file);

            for (String key : legacy.getKeys(true)) {
               if (!legacy.isConfigurationSection(key) && !sellIntegration.contains(key)) {
                  sellIntegration.set(key, legacy.get(key));
               }
            }

            if (!save(sellIntegration, file, "Failed to move the sell settings into sell_integration.yml", log)) {
               return false;
            } else {
               config.set("sell_integration", null);

               try {
                  config.save(configFile);
               } catch (IOException var9) {
                  log.log(Level.WARNING, "Failed to remove the old sell_integration section from config.yml", (Throwable)var9);
                  return false;
               }

               log.info("Moved the sell settings from config.yml into sell_integration.yml");
               return true;
            }
         }
      }
   }

   private static void importLegacyPrices(File dataFolder, File file, Logger log) {
      File legacyFile = new File(dataFolder, "item_prices.yml");
      if (legacyFile.exists()) {
         YamlConfiguration legacy = YamlConfiguration.loadConfiguration(legacyFile);
         YamlConfiguration sellIntegration = YamlConfiguration.loadConfiguration(file);

         for (String key : legacy.getKeys(false)) {
            if (!legacy.isConfigurationSection(key)) {
               String path = "custom_prices.prices." + key;
               if (!sellIntegration.contains(path)) {
                  sellIntegration.set(path, legacy.get(key));
               }
            }
         }

         if (save(sellIntegration, file, "Failed to move the item prices into sell_integration.yml", log)) {
            if (legacyFile.delete()) {
               log.info("Moved the prices from item_prices.yml into sell_integration.yml");
            } else {
               log.warning(
                  "Moved the prices from item_prices.yml into sell_integration.yml, but could not delete the old file. Delete it yourself, it is no longer read."
               );
            }
         }
      }
   }

   private static boolean save(YamlConfiguration config, File file, String failureMessage, Logger log) {
      try {
         config.save(file);
         return true;
      } catch (IOException var5) {
         log.log(Level.SEVERE, failureMessage, (Throwable)var5);
         return false;
      }
   }
}
