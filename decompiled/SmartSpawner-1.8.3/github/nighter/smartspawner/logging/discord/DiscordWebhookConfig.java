package github.nighter.smartspawner.logging.discord;

import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.logging.SpawnerEventType;
import java.io.File;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import lombok.Generated;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class DiscordWebhookConfig {
   private static final String FILE_NAME = "activity_log.yml";
   private static final String SECTION = "discord.";
   private final SmartSpawner plugin;
   private boolean enabled;
   private String webhookUrl;
   private boolean showPlayerHead;
   private boolean logAllEvents;
   private Set<SpawnerEventType> enabledEvents;

   public DiscordWebhookConfig(SmartSpawner plugin) {
      this.plugin = plugin;
      this.loadConfig();
   }

   public void loadConfig() {
      File activityLogFile = new File(this.plugin.getDataFolder(), "activity_log.yml");
      if (!activityLogFile.exists()) {
         this.enabled = false;
      } else {
         FileConfiguration cfg = YamlConfiguration.loadConfiguration(activityLogFile);
         this.enabled = cfg.getBoolean("discord.enabled", false);
         this.webhookUrl = cfg.getString("discord.webhook_url", "");
         this.showPlayerHead = cfg.getBoolean("discord.show_player_head", true);
         this.logAllEvents = cfg.getBoolean("discord.log_all_events", false);
         this.enabledEvents = this.parseEnabledEvents(cfg);
      }
   }

   private Set<SpawnerEventType> parseEnabledEvents(FileConfiguration cfg) {
      if (this.logAllEvents) {
         return EnumSet.allOf(SpawnerEventType.class);
      } else {
         List<String> list = cfg.getStringList("discord.logged_events");
         if (list.isEmpty()) {
            Set<SpawnerEventType> defaults = EnumSet.noneOf(SpawnerEventType.class);
            defaults.add(SpawnerEventType.SPAWNER_PLACE);
            defaults.add(SpawnerEventType.SPAWNER_BREAK);
            defaults.add(SpawnerEventType.SPAWNER_EXPLODE);
            defaults.add(SpawnerEventType.SPAWNER_STACK_HAND);
            defaults.add(SpawnerEventType.SPAWNER_STACK_GUI);
            defaults.add(SpawnerEventType.SPAWNER_DESTACK_GUI);
            defaults.add(SpawnerEventType.COMMAND_EXECUTE_PLAYER);
            defaults.add(SpawnerEventType.COMMAND_EXECUTE_CONSOLE);
            defaults.add(SpawnerEventType.COMMAND_EXECUTE_RCON);
            return defaults;
         } else {
            Set<SpawnerEventType> events = EnumSet.noneOf(SpawnerEventType.class);

            for (String name : list) {
               try {
                  events.add(SpawnerEventType.valueOf(name.trim().toUpperCase()));
               } catch (IllegalArgumentException var7) {
                  this.plugin.getLogger().warning("activity_log.yml: unknown event type '" + name + "', skipping.");
               }
            }

            return events;
         }
      }
   }

   public boolean isEventEnabled(SpawnerEventType eventType) {
      return this.enabled && this.enabledEvents.contains(eventType);
   }

   @Generated
   public boolean isEnabled() {
      return this.enabled;
   }

   @Generated
   public String getWebhookUrl() {
      return this.webhookUrl;
   }

   @Generated
   public boolean isShowPlayerHead() {
      return this.showPlayerHead;
   }

   @Generated
   public boolean isLogAllEvents() {
      return this.logAllEvents;
   }

   @Generated
   public Set<SpawnerEventType> getEnabledEvents() {
      return this.enabledEvents;
   }

   public static class EmbedField {
      private final String name;
      private final String value;
      private final boolean inline;

      public EmbedField(String name, String value, boolean inline) {
         this.name = name;
         this.value = value;
         this.inline = inline;
      }

      @Generated
      public String getName() {
         return this.name;
      }

      @Generated
      public String getValue() {
         return this.value;
      }

      @Generated
      public boolean isInline() {
         return this.inline;
      }
   }
}
