package github.nighter.smartspawner.updates;

import github.nighter.smartspawner.SmartSpawner;
import java.io.File;

public class ConfigUpdater {
   private static final String FILE_NAME = "config.yml";
   private final SmartSpawner plugin;

   public ConfigUpdater(SmartSpawner plugin) {
      this.plugin = plugin;
   }

   public void checkAndUpdateConfig() {
      File configFile = new File(this.plugin.getDataFolder(), "config.yml");
      YamlMigrator.migrate(configFile, this.plugin.getResource("config.yml"), ConfigMigrations.CONFIG, ConfigMigrations.CONFIG_VALUES, this.plugin.getLogger());
      this.plugin.reloadConfig();
   }
}
