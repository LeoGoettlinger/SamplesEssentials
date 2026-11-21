package me.byteful.plugin.leveltools.config.migration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.logging.Logger;
import me.byteful.plugin.leveltools.LevelToolsPlugin;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

public final class ConfigMigrator {
   private final LevelToolsPlugin plugin;
   private final Path dataFolder;
   private final Logger logger;

   public ConfigMigrator(@NotNull LevelToolsPlugin plugin, @NotNull Path dataFolder, @NotNull Logger logger) {
      this.plugin = plugin;
      this.dataFolder = dataFolder;
      this.logger = logger;
   }

   public void migrate() {
      Path oldConfigPath = this.dataFolder.resolve("config.yml");
      Path backupPath = this.dataFolder.resolve("old_config.yml");
      if (!Files.exists(oldConfigPath)) {
         this.logger.warning("No config.yml found to migrate.");
      } else {
         YamlConfiguration oldConfig = YamlConfiguration.loadConfiguration(oldConfigPath.toFile());
         this.backupOldConfig(oldConfigPath, backupPath);
         V1ToV2Migration migration = new V1ToV2Migration(oldConfig, this.dataFolder, this.logger);
         migration.execute();
      }
   }

   private void backupOldConfig(@NotNull Path configPath, @NotNull Path backupPath) {
      try {
         Files.move(configPath, backupPath, StandardCopyOption.REPLACE_EXISTING);
         this.logger.info("Backed up old config.yml to old_config.yml");
      } catch (IOException var4) {
         this.logger.severe("Failed to backup old config: " + var4.getMessage());
      }
   }
}
