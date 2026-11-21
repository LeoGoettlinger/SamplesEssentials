package me.byteful.plugin.leveltools.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;
import me.byteful.plugin.leveltools.LevelToolsPlugin;
import me.byteful.plugin.leveltools.config.migration.ConfigMigrator;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

public final class ConfigManager {
   private int worldEvents;
   private final LevelToolsPlugin plugin;
   private final Path dataFolder;
   private final Logger logger;
   private FileConfiguration mainConfig;
   private FileConfiguration triggerProfilesConfig;
   private FileConfiguration rewardProfilesConfig;
   private FileConfiguration displayProfilesConfig;
   private FileConfiguration itemProfilesConfig;

   public ConfigManager(@NotNull LevelToolsPlugin plugin) {
      this.plugin = plugin;
      this.dataFolder = plugin.getDataFolder().toPath();
      this.logger = plugin.getLogger();
   }

   public void loadAll() {
      this.ensureDataFolder();
      if (this.needsMigration()) {
         this.logger.info("Detected v1 configuration. Starting migration to v2...");
         ConfigMigrator migrator = new ConfigMigrator(this.plugin, this.dataFolder, this.logger);
         migrator.migrate();
         this.logger.info("Migration complete!");
      }

      this.mainConfig = this.loadOrCreate("config.yml");
      this.triggerProfilesConfig = this.loadOrCreate("trigger_profiles.yml");
      this.rewardProfilesConfig = this.loadOrCreate("reward_profiles.yml");
      this.displayProfilesConfig = this.loadOrCreate("display_profiles.yml");
      this.itemProfilesConfig = this.loadOrCreate("item_profiles.yml");
      this.logger.info("Loaded all configuration files.");
   }

   public void reload() {
      this.loadAll();
   }

   private void ensureDataFolder() {
      this.worldEvents = this.registerWorldEvents();

      try {
         if (!Files.exists(this.dataFolder)) {
            Files.createDirectories(this.dataFolder);
         }
      } catch (IOException var2) {
         this.logger.severe("Failed to create data folder: " + var2.getMessage());
      }
   }

   private boolean needsMigration() {
      Path configPath = this.dataFolder.resolve("config.yml");
      if (!Files.exists(configPath)) {
         return false;
      } else {
         YamlConfiguration config = YamlConfiguration.loadConfiguration(configPath.toFile());
         return config.contains("tool_rewards") || config.contains("sword_rewards") || config.contains("bow_rewards");
      }
   }

   @NotNull
   private FileConfiguration loadOrCreate(@NotNull String filename) {
      File file = this.dataFolder.resolve(filename).toFile();
      if (!file.exists()) {
         this.plugin.saveResource(filename, false);
      }

      FileConfiguration config = YamlConfiguration.loadConfiguration(file);
      InputStream defaultStream = this.plugin.getResource(filename);
      if (defaultStream != null) {
         YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
         config.setDefaults(defaultConfig);
      }

      return config;
   }

   public void saveConfig(@NotNull FileConfiguration config, @NotNull String filename) {
      try {
         config.save(this.dataFolder.resolve(filename).toFile());
      } catch (IOException var4) {
         this.logger.severe("Failed to save " + filename + ": " + var4.getMessage());
      }
   }

   @NotNull
   public FileConfiguration getMainConfig() {
      return this.mainConfig;
   }

   @NotNull
   public FileConfiguration getTriggerProfilesConfig() {
      return this.triggerProfilesConfig;
   }

   @NotNull
   public FileConfiguration getRewardProfilesConfig() {
      return this.rewardProfilesConfig;
   }

   @NotNull
   public FileConfiguration getDisplayProfilesConfig() {
      return this.displayProfilesConfig;
   }

   @NotNull
   public FileConfiguration getItemProfilesConfig() {
      return this.itemProfilesConfig;
   }
}
