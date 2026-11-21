package github.nighter.smartspawner.logging.discord;

import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.logging.SpawnerEventType;
import java.io.File;
import java.util.EnumMap;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class DiscordEmbedConfigManager {
   private static final String CONFIG_FILE = "activity_log.yml";
   private static final String SECTION = "embeds.";
   private final SmartSpawner plugin;
   private final EnumMap<SpawnerEventType, DiscordEventEmbedConfig> cache = new EnumMap<>(SpawnerEventType.class);

   public DiscordEmbedConfigManager(SmartSpawner plugin, DiscordWebhookConfig config) {
      this.plugin = plugin;
   }

   public DiscordEventEmbedConfig getEmbedConfig(SpawnerEventType eventType) {
      return this.cache.computeIfAbsent(eventType, this::loadEventConfig);
   }

   public void invalidateCache() {
      this.cache.clear();
   }

   private DiscordEventEmbedConfig loadEventConfig(SpawnerEventType eventType) {
      File configFile = new File(this.plugin.getDataFolder(), "activity_log.yml");
      if (!configFile.exists()) {
         return DiscordEventEmbedConfig.defaults();
      } else {
         FileConfiguration cfg = YamlConfiguration.loadConfiguration(configFile);
         ConfigurationSection section = cfg.getConfigurationSection("embeds." + eventType.name());
         return section == null ? DiscordEventEmbedConfig.defaults() : DiscordEventEmbedConfig.fromSection(section);
      }
   }
}
