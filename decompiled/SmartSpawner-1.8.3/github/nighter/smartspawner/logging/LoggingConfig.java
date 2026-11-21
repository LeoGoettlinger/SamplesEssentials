package github.nighter.smartspawner.logging;

import github.nighter.smartspawner.SmartSpawner;
import java.io.File;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Generated;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class LoggingConfig {
   private static final String LOG_DIRECTORY = "logs";
   private static final String SECTION = "file.";
   private final SmartSpawner plugin;
   private boolean enabled;
   private boolean jsonFormat;
   private boolean consoleOutput;
   private Set<SpawnerEventType> enabledEvents;
   private String logDirectory;
   private int maxLogFiles;
   private long maxLogSizeMB;
   private boolean logAllEvents;
   private List<String> loggedEvents;

   public LoggingConfig(SmartSpawner plugin) {
      this.plugin = plugin;
      this.loadConfig();
   }

   public void loadConfig() {
      FileConfiguration cfg = YamlConfiguration.loadConfiguration(new File(this.plugin.getDataFolder(), "activity_log.yml"));
      this.enabled = cfg.getBoolean("file.enabled", true);
      this.jsonFormat = cfg.getBoolean("file.json_format", false);
      this.consoleOutput = cfg.getBoolean("file.console_output", false);
      this.logDirectory = "logs";
      this.maxLogFiles = cfg.getInt("file.max_log_files", 10);
      this.maxLogSizeMB = cfg.getLong("file.max_log_size_mb", 10L);
      this.logAllEvents = cfg.getBoolean("file.log_all_events", false);
      this.loggedEvents = cfg.getStringList("file.logged_events");
      this.enabledEvents = this.parseEnabledEvents();
   }

   private Set<SpawnerEventType> parseEnabledEvents() {
      Set<SpawnerEventType> events = EnumSet.noneOf(SpawnerEventType.class);
      if (this.logAllEvents) {
         return EnumSet.allOf(SpawnerEventType.class);
      } else if (this.loggedEvents != null && !this.loggedEvents.isEmpty()) {
         for (String eventName : this.loggedEvents) {
            try {
               events.add(SpawnerEventType.valueOf(eventName.trim().toUpperCase()));
            } catch (IllegalArgumentException var5) {
            }
         }

         return events;
      } else {
         events.add(SpawnerEventType.SPAWNER_PLACE);
         events.add(SpawnerEventType.SPAWNER_BREAK);
         events.add(SpawnerEventType.SPAWNER_EXPLODE);
         events.add(SpawnerEventType.SPAWNER_STACK_HAND);
         events.add(SpawnerEventType.SPAWNER_STACK_GUI);
         events.add(SpawnerEventType.SPAWNER_DESTACK_GUI);
         events.add(SpawnerEventType.SPAWNER_EXP_CLAIM);
         events.add(SpawnerEventType.SPAWNER_SELL_ALL);
         events.add(SpawnerEventType.SPAWNER_ITEM_TAKE_ALL);
         events.add(SpawnerEventType.SPAWNER_ITEMS_SORT);
         events.add(SpawnerEventType.SPAWNER_ITEM_FILTER);
         events.add(SpawnerEventType.SPAWNER_DROP_PAGE_ITEMS);
         events.add(SpawnerEventType.COMMAND_EXECUTE_PLAYER);
         events.add(SpawnerEventType.COMMAND_EXECUTE_CONSOLE);
         events.add(SpawnerEventType.COMMAND_EXECUTE_RCON);
         return events;
      }
   }

   public Set<SpawnerEventType> getEnabledEvents() {
      return new HashSet<>(this.enabledEvents);
   }

   public boolean isEventEnabled(SpawnerEventType eventType) {
      return !this.enabled || !this.enabledEvents.contains(eventType);
   }

   @Generated
   public boolean isEnabled() {
      return this.enabled;
   }

   @Generated
   public boolean isJsonFormat() {
      return this.jsonFormat;
   }

   @Generated
   public boolean isConsoleOutput() {
      return this.consoleOutput;
   }

   @Generated
   public String getLogDirectory() {
      return this.logDirectory;
   }

   @Generated
   public int getMaxLogFiles() {
      return this.maxLogFiles;
   }

   @Generated
   public long getMaxLogSizeMB() {
      return this.maxLogSizeMB;
   }

   @Generated
   public boolean isLogAllEvents() {
      return this.logAllEvents;
   }

   @Generated
   public List<String> getLoggedEvents() {
      return this.loggedEvents;
   }
}
