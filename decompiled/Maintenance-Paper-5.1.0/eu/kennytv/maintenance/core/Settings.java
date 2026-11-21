package eu.kennytv.maintenance.core;

import eu.kennytv.maintenance.api.event.MaintenanceReloadedEvent;
import eu.kennytv.maintenance.core.config.Config;
import eu.kennytv.maintenance.core.config.ConfigSection;
import eu.kennytv.maintenance.core.config.DisplayedMessages;
import eu.kennytv.maintenance.core.util.ServerType;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.Nullable;

public class Settings implements eu.kennytv.maintenance.api.Settings {
   private int pluginBridge;
   public static final String NEW_LINE_REPLACEMENT = "<br>";
   private static final int CONFIG_VERSION = 11;
   private static final int LANGUAGE_VERSION = 5;
   protected final MaintenancePlugin plugin;
   private final Map<UUID, String> whitelistedPlayers = new HashMap<>();
   private final String[] unsupportedFields;
   protected boolean maintenance;
   protected String activeMode;
   protected String activeReason;
   private Set<Integer> broadcastIntervals;
   private DisplayedMessages pingMessages;
   private DisplayedMessages timerSpecificPingMessages;
   private List<String> commandsOnMaintenanceEnable;
   private List<String> commandsOnMaintenanceDisable;
   private DisplayedMessages legacyParsedPlayerCountMessages;
   private DisplayedMessages legacyParsedTimerPlayerCountMessages;
   private DisplayedMessages legacyParsedPlayerCountHoverLines;
   private DisplayedMessages legacyParsedTimerPlayerCountHoverLines;
   private String prefixString;
   private String plainTextPrefix;
   private String languageName;
   private boolean enablePingMessages;
   private boolean customPlayerCountMessage;
   private boolean customPlayerCountHoverMessage;
   private boolean customMaintenanceIcon;
   private boolean joinNotifications;
   private boolean updateChecks;
   private boolean saveEndtimerOnStop;
   private boolean kickOnlinePlayers;
   private boolean debug;
   private long savedEndtimer;
   private boolean webhookEnabled;
   private Set<String> webhookEnabledEvents;
   private String webhookUrl;
   private String webhookUsername;
   private String webhookAvatarUrl;
   protected Config config;
   protected Config language;
   protected Config whitelist;

   public Settings(MaintenancePlugin plugin, String... unsupportedFields) {
      this.plugin = plugin;
      this.unsupportedFields = unsupportedFields;
      if (!plugin.getDataFolder().exists()) {
         this.updatePluginDirectory();
         plugin.getDataFolder().mkdirs();
      }

      this.createFile("config.yml");
      this.createFile("WhitelistedPlayers.yml");
      this.reloadConfigs();
      this.pluginBridge = this.preparePluginBridge();
   }

   @Override
   public void reloadConfigs() {
      try {
         this.config = new Config(new File(this.plugin.getDataFolder(), "config.yml"), this.unsupportedFields);
         this.config.load();
         this.config.resetAwesomeHeader();
         this.whitelist = new Config(new File(this.plugin.getDataFolder(), "WhitelistedPlayers.yml"));
         this.whitelist.load();
      } catch (Exception var4) {
         throw new RuntimeException("Unable to load Maintenance files - probably a malformed config file", var4);
      }

      this.loadSettings();
      this.createLanguageFile();

      try {
         this.language = new Config(new File(this.plugin.getDataFolder(), "language-" + this.languageName + ".yml"));
         this.language.load();
      } catch (IOException var3) {
         throw new RuntimeException("Unable to load Maintenance language file - probably a malformed file", var3);
      }

      try {
         this.updateLanguageFile();
      } catch (IOException var2) {
         this.plugin.getLogger().log(Level.SEVERE, "Couldn't update language file", (Throwable)var2);
      }

      this.prefixString = this.language.getString("prefix");
      this.plainTextPrefix = PlainTextComponentSerializer.plainText().serialize(this.parse(this.prefixString));
      this.plugin.getEventManager().callEvent(new MaintenanceReloadedEvent());
   }

   public void saveConfig() {
      try {
         this.config.save();
      } catch (IOException var2) {
         this.plugin.getLogger().log(Level.SEVERE, "Couldn't save the config file!", (Throwable)var2);
      }
   }

   public void createFile(String name) {
      this.createFile(name, name);
   }

   private void createFile(String name, String from) {
      File file = new File(this.plugin.getDataFolder(), name);
      if (!file.exists()) {
         try (InputStream in = this.plugin.getResource(from)) {
            Files.copy(in, file.toPath());
         } catch (IOException var9) {
            throw new RuntimeException("Unable to create " + name + " file for Maintenance!", var9);
         }
      }
   }

   private void saveWhitelistedPlayers() {
      try {
         this.whitelist.save();
      } catch (IOException var2) {
         this.plugin.getLogger().log(Level.SEVERE, "Couldn't save the whitelisted players file!", (Throwable)var2);
      }
   }

   private void createLanguageFile() {
      String fileName = "language-" + this.languageName + ".yml";
      File file = new File(this.plugin.getDataFolder(), fileName);
      if (!file.exists()) {
         try (InputStream in = this.plugin.getResource(fileName)) {
            Files.copy(in, file.toPath());
         } catch (IOException var8) {
            this.plugin.getLogger().warning("Unable to provide language " + this.languageName);
            if (!this.languageName.equals("en")) {
               this.plugin.getLogger().warning("Falling back to default language: en");
               this.languageName = "en";
               this.createLanguageFile();
            }
         }
      }
   }

   private void loadSettings() {
      this.updateConfig();
      ConfigSection pingMessageSection = this.config.getSection("ping-message");
      this.enablePingMessages = pingMessageSection.getBoolean("enabled", true);
      this.pingMessages = this.loadPingMessages(pingMessageSection.getSection("messages"));
      if (pingMessageSection.getBoolean("enable-timer-specific-messages")) {
         this.timerSpecificPingMessages = this.loadPingMessages(pingMessageSection.getSection("timer-messages"));
      } else {
         this.timerSpecificPingMessages = null;
      }

      this.maintenance = this.config.getBoolean("maintenance-enabled");
      this.activeMode = this.normalizeMode(this.config.getString("active-mode"));
      this.activeReason = this.normalizeReason(this.config.getString("active-reason"));
      this.commandsOnMaintenanceEnable = this.config.getStringList("commands-on-maintenance-enable");
      this.commandsOnMaintenanceDisable = this.config.getStringList("commands-on-maintenance-disable");
      this.customMaintenanceIcon = this.config.getBoolean("custom-maintenance-icon");
      this.joinNotifications = this.config.getBoolean("send-join-notification");
      this.broadcastIntervals = new HashSet<>(this.config.getIntList("timer-broadcast-for-seconds"));
      if (this.plugin.getServerType() != ServerType.SPONGE) {
         ConfigSection playerCountMessageSection = this.config.getSection("player-count-message");
         this.customPlayerCountMessage = playerCountMessageSection.getBoolean("enabled");
         this.legacyParsedPlayerCountMessages = this.loadPlayerCountMessages(playerCountMessageSection.getSection("messages"));
         if (playerCountMessageSection.getBoolean("enable-timer-specific-message")) {
            this.legacyParsedTimerPlayerCountMessages = this.loadPlayerCountMessages(playerCountMessageSection.getSection("timer-messages"));
         } else {
            this.legacyParsedTimerPlayerCountMessages = null;
         }
      }

      ConfigSection listHoverMessageSection = this.config.getSection("player-list-hover-message");
      this.customPlayerCountHoverMessage = listHoverMessageSection.getBoolean("enabled");
      this.legacyParsedPlayerCountHoverLines = this.loadPlayerCountHoverMessages(listHoverMessageSection.getSection("messages"));
      if (listHoverMessageSection.getBoolean("enable-timer-specific-message")) {
         this.legacyParsedTimerPlayerCountHoverLines = this.loadPlayerCountHoverMessages(listHoverMessageSection.getSection("timer-messages"));
      } else {
         this.legacyParsedTimerPlayerCountHoverLines = null;
      }

      this.languageName = this.config.getString("language").toLowerCase(Locale.ROOT);
      this.kickOnlinePlayers = this.config.getBoolean("kick-online-players", true);
      this.updateChecks = this.config.getBoolean("update-checks", true);
      this.debug = this.config.getBoolean("debug");
      ConfigSection webhookSection = this.config.getSection("webhook");
      this.webhookEnabled = webhookSection.getBoolean("enabled");
      this.webhookEnabledEvents = new HashSet<>(webhookSection.getStringList("enabled-events"));
      this.webhookUrl = webhookSection.getString("url", "");
      this.webhookUsername = webhookSection.getString("username", "Maintenance");
      this.webhookAvatarUrl = webhookSection.getString("avatar-url", "");
      ConfigSection section = this.config.getSection("continue-endtimer-after-restart");
      this.saveEndtimerOnStop = section.getBoolean("enabled");
      this.savedEndtimer = section.getLong("end");
      if (this.customMaintenanceIcon) {
         this.plugin.loadMaintenanceIcon();
      }

      this.loadWhitelistedPlayers();
      this.loadExtraSettings();
   }

   protected void loadWhitelistedPlayers() {
      this.whitelistedPlayers.clear();

      for (Entry<String, Object> entry : this.whitelist.getValues().entrySet()) {
         try {
            this.whitelistedPlayers.put(UUID.fromString(entry.getKey()), String.valueOf(entry.getValue()));
         } catch (IllegalArgumentException var4) {
            this.plugin.getLogger().warning("Invalid WhitelistedPlayers entry: " + entry.getKey());
         }
      }
   }

   private void updatePluginDirectory() {
      String oldDirName = "Maintenance" + this.plugin.getServerType();
      if (this.plugin.getServerType() == ServerType.SPONGE || this.plugin.getServerType() == ServerType.VELOCITY) {
         oldDirName = oldDirName.toLowerCase(Locale.ROOT);
      } else if (this.plugin.getServerType() == ServerType.PAPER) {
         oldDirName = "MaintenanceSpigot";
      }

      File oldDir = new File(this.plugin.getDataFolder().getParentFile(), oldDirName);
      if (oldDir.exists()) {
         try {
            Files.move(oldDir.toPath(), this.plugin.getDataFolder().toPath());
            this.plugin.getLogger().info("Moved old " + oldDirName + " to new " + this.plugin.getDataFolder().getName() + " directory!");
         } catch (IOException var4) {
            this.plugin
               .getLogger()
               .log(Level.SEVERE, "Error while copying " + oldDirName + " to new " + this.plugin.getDataFolder().getName() + " directory!", (Throwable)var4);
         }
      }
   }

   private void updateConfig() {
      int version = this.config.getInt("config-version");
      if (version != 11) {
         this.plugin.getLogger().info("Updating config to the latest version...");
         if (version < 6) {
            this.config.getStringList("pingmessages").replaceAll(this::legacyToMinimessage);
            this.config.getStringList("timerspecific-pingmessages").replaceAll(this::legacyToMinimessage);
            this.config.set("playercounthovermessage", this.legacyToMinimessage(this.config.getString("playercounthovermessage")));
            this.config.set("playercountmessage", this.legacyToMinimessage(this.config.getString("playercountmessage")));
         }

         if (version < 9) {
            this.config.move("enable-pingmessages", "ping-message.enabled");
            this.config.move("pingmessages", "ping-message.messages");
            this.config.move("enable-timerspecific-messages", "ping-message.enable-timer-specific-messages");
            this.config.move("timerspecific-pingmessages", "ping-message.timer-messages");
            this.config.move("enable-playercountmessage", "player-count-message.enabled");
            this.config.move("playercountmessage", "player-count-message.message");
            this.config.move("enable-playercounthovermessage", "player-list-hover-message.enabled");
            this.config.move("playercounthovermessage", "player-list-hover-message.message");
         }

         if (version < 11) {
            this.config.move("player-count-message.message", "player-count-message.messages.default");
            this.config.move("player-count-message.timer-message", "player-count-message.timer-messages.default");
            this.config.move("player-list-hover-message.message", "player-list-hover-message.messages.default");
            this.config.move("player-list-hover-message.timer-message", "player-list-hover-message.timer-messages.default");
            this.migrateModeSection("ping-message.messages");
            this.migrateModeSection("ping-message.timer-messages");
            this.migrateModeSection("player-count-message.messages");
            this.migrateModeSection("player-count-message.timer-messages");
            this.migrateModeSection("player-list-hover-message.messages");
            this.migrateModeSection("player-list-hover-message.timer-messages");
            if (this.config.getObject("proxied-maintenance-servers") instanceof List<?> list) {
               Map<String, String> migratedServers = new LinkedHashMap<>();

               for (Object entry : list) {
                  if (entry != null) {
                     migratedServers.put(String.valueOf(entry), "default");
                  }
               }

               this.config.set("proxied-maintenance-servers", migratedServers);
            }
         }

         this.createFile("config-new.yml", "config.yml");
         File file = new File(this.plugin.getDataFolder(), "config-new.yml");
         Config tempConfig = new Config(file, this.unsupportedFields);

         try {
            tempConfig.load();
         } catch (IOException var7) {
            var7.printStackTrace();
         }

         this.config.addMissingFields(tempConfig);
         this.config.replaceComments(tempConfig);
         this.config.set("config-version", 11);
         file.delete();
         tempConfig.clear();
         this.saveConfig();
         this.plugin.getLogger().info("Done! Updated config!");
      }
   }

   private void updateLanguageFile() throws IOException {
      int version = this.language.getInt("language-version");
      if (version != 5) {
         this.plugin.getLogger().info("Updating language file to the latest version...");
         if (version < 1) {
            for (Entry<String, Object> entry : this.language.getValues().entrySet()) {
               if (entry.getValue() instanceof String value) {
                  String var11 = value.replace("&8[&eMaintenance&8] ", "<prefix>");
                  var11 = this.legacyToMinimessage(var11);
                  var11 = var11.replace("%NEWLINE%", "<br>");
                  this.language.set(entry.getKey(), var11);
               }
            }
         }

         String filePrefix = "language-" + this.languageName;
         this.createFile(filePrefix + "-new.yml", filePrefix + ".yml");
         File file = new File(this.plugin.getDataFolder(), filePrefix + "-new.yml");
         Config tempConfig = new Config(file);

         try {
            tempConfig.load();
         } finally {
            file.delete();
         }

         this.language.addMissingFields(tempConfig);
         this.language.replaceComments(tempConfig);
         tempConfig.clear();
         this.language.set("language-version", 5);
         this.language.save();
         this.plugin.getLogger().info("Updated language file!");
      }
   }

   private String legacyToMinimessage(String s) {
      TextComponent component = LegacyComponentSerializer.legacyAmpersand().deserialize(s);
      String serialized = (String)MiniMessage.miniMessage().serialize(component);
      return serialized.replaceAll("</[a-z_]+>", "");
   }

   private void migrateModeSection(String path) {
      Object section = this.config.getObject(path);
      if (section != null && !(section instanceof Map)) {
         Map<String, Object> map = new LinkedHashMap<>();
         map.put("default", section);
         this.config.set(path, map);
      }
   }

   private String getConfigMessage(ConfigSection section, String path) {
      String s = section.getString(path);
      if (s == null) {
         this.plugin.getLogger().warning("The config file is missing the following string: " + path);
         return path;
      } else {
         return this.replaceNewlineVar(s);
      }
   }

   public String getConfigMessage(String path) {
      return this.getConfigMessage(this.config, path);
   }

   public String getLanguageString(String path, String... replacements) {
      String s = this.language.getString(path);
      if (s == null) {
         this.plugin.getLogger().warning("The language file is missing the following string: " + path);
         return path;
      } else {
         if (replacements.length != 0) {
            if (replacements.length % 2 != 0) {
               throw new IllegalArgumentException("Invalid replacement count: " + replacements.length);
            }

            for (int i = 0; i < replacements.length; i += 2) {
               String key = replacements[i];
               s = s.replace(key, replacements[i + 1]);
            }
         }

         return this.replaceNewlineVar(s).replace("<prefix>", this.prefixString);
      }
   }

   public String getWebhookMessage(String path, String... replacements) {
      String s = this.language.getString(path);
      if (s == null) {
         this.plugin.getLogger().warning("The language file is missing the following webhook message: " + path);
         return path;
      } else {
         if (replacements.length != 0) {
            if (replacements.length % 2 != 0) {
               throw new IllegalArgumentException("Invalid replacement count: " + replacements.length);
            }

            for (int i = 0; i < replacements.length; i += 2) {
               String key = replacements[i];
               s = s.replace(key, replacements[i + 1]);
            }
         }

         return this.replaceNewlineVar(s);
      }
   }

   @Nullable
   public String getLanguageStringOrNull(String path, String... replacements) {
      return this.language.contains(path) ? this.getLanguageString(path, replacements) : null;
   }

   public Component getMessage(String path, String... replacements) {
      return this.parse(this.getLanguageString(path, replacements));
   }

   public Component getRandomPingMessage() {
      return this.getRandomPingMessage(this.activeMode);
   }

   public Component getRandomPingMessage(@Nullable String mode) {
      if (this.plugin.isTaskRunning() && !this.plugin.getRunnable().shouldEnable() && this.hasTimerSpecificPingMessages()) {
         return this.getPingMessage(this.timerSpecificPingMessages, mode);
      } else {
         return (Component)(this.pingMessages.isEmpty() ? Component.empty() : this.getPingMessage(this.pingMessages, mode));
      }
   }

   private Component getPingMessage(DisplayedMessages list, @Nullable String mode) {
      String component = list.getRandomEntry(mode);
      return this.parse(this.plugin.replacePingVariables(component, mode));
   }

   private DisplayedMessages loadPingMessages(ConfigSection section) {
      return this.loadDisplayedMessages(section, (list, s) -> list.add(this.replaceNewlineVar(s)));
   }

   private DisplayedMessages loadPlayerCountHoverMessages(ConfigSection section) {
      return this.loadDisplayedMessages(section, (list, s) -> {
         for (String line : s.split("<br>")) {
            list.add(this.toLegacy(this.parse(line)));
         }
      });
   }

   private DisplayedMessages loadPlayerCountMessages(ConfigSection section) {
      return this.loadDisplayedMessages(section, (list, s) -> list.add(this.toLegacy(this.parse(s))));
   }

   private DisplayedMessages loadDisplayedMessages(ConfigSection section, BiConsumer<List<String>, String> messageFunction) {
      Map<String, List<String>> modes = new HashMap<>(section.getKeys().size());

      for (Entry<String, Object> entry : section.getValues().entrySet()) {
         List<String> components = new ArrayList<>();
         if (entry.getValue() instanceof String s) {
            messageFunction.accept(components, s);
         } else {
            Object var11 = entry.getValue();
            if (var11 instanceof List) {
               for (String s : (List)var11) {
                  messageFunction.accept(components, s);
               }
            }
         }

         modes.put(entry.getKey(), components);
      }

      return new DisplayedMessages(modes);
   }

   @Override
   public boolean removeWhitelistedPlayer(UUID uuid) {
      if (this.whitelistedPlayers.remove(uuid) == null) {
         return false;
      } else {
         this.whitelist.remove(uuid.toString());
         this.saveWhitelistedPlayers();
         return true;
      }
   }

   @Override
   public boolean removeWhitelistedPlayer(String name) {
      UUID uuid = null;

      for (Entry<UUID, String> e : this.whitelistedPlayers.entrySet()) {
         if (e.getValue().equalsIgnoreCase(name)) {
            uuid = e.getKey();
            break;
         }
      }

      return uuid == null ? false : this.removeWhitelistedPlayer(uuid);
   }

   @Override
   public boolean addWhitelistedPlayer(UUID uuid, String name) {
      boolean added = !name.equals(this.whitelistedPlayers.put(uuid, name));
      if (!added) {
         return false;
      } else {
         this.whitelist.set(uuid.toString(), name);
         this.saveWhitelistedPlayers();
         return true;
      }
   }

   @Override
   public Map<UUID, String> getWhitelistedPlayers() {
      return this.whitelistedPlayers;
   }

   @Override
   public boolean isWhitelisted(UUID uuid) {
      return this.whitelistedPlayers.containsKey(uuid);
   }

   @Override
   public boolean isMaintenance() {
      return this.maintenance;
   }

   @Nullable
   @Override
   public String activeMode() {
      return this.activeMode;
   }

   public void setActiveMode(@Nullable String activeMode) {
      String normalized = this.normalizeMode(activeMode);
      if (!Objects.equals(this.activeMode, normalized)) {
         this.activeMode = normalized;
         this.config.set("active-mode", normalized != null ? normalized : "default");
         this.saveConfig();
      }
   }

   @Override
   public String activeReason() {
      return this.activeReason;
   }

   @Override
   public void setActiveReason(@Nullable String reason) {
      String normalized = this.normalizeReason(reason);
      if (!Objects.equals(this.activeReason, normalized)) {
         this.activeReason = normalized;
         this.config.set("active-reason", normalized);
         this.saveConfig();
      }
   }

   public void setMaintenance(boolean maintenance, @Nullable String mode) {
      if (this.maintenance != maintenance) {
         this.maintenance = maintenance;
         this.config.set("maintenance-enabled", maintenance);
         this.activeMode = this.normalizeMode(mode);
         this.config.set("active-mode", this.activeMode != null ? this.activeMode : "default");
         this.saveConfig();
      }
   }

   @Override
   public boolean isEnablePingMessages() {
      return this.enablePingMessages;
   }

   @Override
   public boolean isJoinNotifications() {
      return this.joinNotifications;
   }

   @Override
   public boolean hasCustomIcon() {
      return this.customMaintenanceIcon;
   }

   @Override
   public boolean debugEnabled() {
      return this.debug;
   }

   public boolean hasUpdateChecks() {
      return this.updateChecks;
   }

   public boolean isSaveEndtimerOnStop() {
      return this.saveEndtimerOnStop;
   }

   public boolean hasTimerSpecificPingMessages() {
      return this.timerSpecificPingMessages != null;
   }

   public boolean isKickOnlinePlayers() {
      return this.kickOnlinePlayers;
   }

   public long getSavedEndtimer() {
      return this.savedEndtimer;
   }

   public void setSavedEndtimer(long millis) {
      if (this.savedEndtimer != millis) {
         this.savedEndtimer = millis;
         this.config.getSection("continue-endtimer-after-restart").set("end", millis);
         this.saveConfig();
      }
   }

   public Config getConfig() {
      return this.config;
   }

   public DisplayedMessages getPingMessages() {
      return this.pingMessages;
   }

   public List<String> getCommandsOnMaintenanceEnable() {
      return this.commandsOnMaintenanceEnable;
   }

   public List<String> getCommandsOnMaintenanceDisable() {
      return this.commandsOnMaintenanceDisable;
   }

   @Nullable
   public DisplayedMessages getTimerSpecificPingMessages() {
      return this.timerSpecificPingMessages;
   }

   public Set<Integer> getBroadcastIntervals() {
      return this.broadcastIntervals;
   }

   public boolean isWebhookEnabled() {
      return this.webhookEnabled;
   }

   public boolean isWebhookEventEnabled(String event) {
      return this.webhookEnabledEvents.contains(event);
   }

   public String getWebhookUrl() {
      return this.webhookUrl;
   }

   public String getWebhookUsername() {
      return this.webhookUsername;
   }

   public String getWebhookAvatarUrl() {
      return this.webhookAvatarUrl;
   }

   public String getPlainTextPrefix() {
      return this.plainTextPrefix;
   }

   public String getLegacyParsedPlayerCountMessage() {
      return this.getLegacyParsedPlayerCountMessage(this.activeMode);
   }

   public String getLegacyParsedPlayerCountMessage(@Nullable String mode) {
      DisplayedMessages messages = this.timerDependent(this.legacyParsedPlayerCountMessages, this.legacyParsedTimerPlayerCountMessages);
      String message = messages.getRandomEntry(mode);
      return this.plugin.replacePingVariables(message, mode);
   }

   public String[] getLegacyParsedPlayerCountHoverLines() {
      return this.getLegacyParsedPlayerCountHoverLines(this.activeMode);
   }

   public String[] getLegacyParsedPlayerCountHoverLines(@Nullable String mode) {
      DisplayedMessages messages = this.timerDependent(this.legacyParsedPlayerCountHoverLines, this.legacyParsedTimerPlayerCountHoverLines);
      List<String> lines = messages.getMessages(mode);
      String[] parsedLines = new String[lines.size()];

      for (int i = 0; i < lines.size(); i++) {
         parsedLines[i] = this.plugin.replacePingVariables(lines.get(i), mode);
      }

      return parsedLines;
   }

   private <T> T timerDependent(T normal, @Nullable T timerSpecific) {
      return timerSpecific != null && this.plugin.isTaskRunning() && !this.plugin.getRunnable().shouldEnable() ? timerSpecific : normal;
   }

   public Component getKickMessage() {
      return this.parse(this.plugin.replacePingVariables(this.getLanguageString("kickmessage")));
   }

   public String getLanguage() {
      return this.languageName;
   }

   public boolean hasCustomPlayerCountMessage() {
      return this.customPlayerCountMessage;
   }

   public boolean hasCustomPlayerCountHoverMessage() {
      return this.customPlayerCountHoverMessage;
   }

   protected Component parse(String s) {
      return MiniMessage.miniMessage().deserialize(s);
   }

   protected String toLegacy(Component component) {
      return LegacyComponentSerializer.legacySection().serialize(component);
   }

   protected String replaceNewlineVar(String s) {
      return s.replace("<br>", "\n");
   }

   @Nullable
   protected String normalizeMode(@Nullable String mode) {
      return mode != null && !mode.isBlank() && !mode.equalsIgnoreCase("default") ? mode.toLowerCase(Locale.ROOT) : null;
   }

   private String normalizeReason(@Nullable String reason) {
      return reason != null && !reason.isBlank() ? reason : "";
   }

   protected void loadExtraSettings() {
   }
}
