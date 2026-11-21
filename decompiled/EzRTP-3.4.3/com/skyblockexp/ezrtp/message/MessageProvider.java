package com.skyblockexp.ezrtp.message;

import com.skyblockexp.ezrtp.platform.PlatformMessageService;
import com.skyblockexp.ezrtp.platform.PlatformMessageServiceRegistry;
import com.skyblockexp.ezrtp.util.PlaceholderUtil;
import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class MessageProvider {
   private final Map<String, String> messages;
   private final String language;
   private final Logger logger;
   private final Map<String, Map<Integer, Component>> componentCache = new HashMap<>();

   private MessageProvider(Map<String, String> messages, String language, Logger logger) {
      this.messages = messages;
      this.language = language;
      this.logger = logger;
   }

   public static MessageProvider load(File messagesDirectory, String language, Logger logger) {
      if (messagesDirectory != null && messagesDirectory.exists() && messagesDirectory.isDirectory()) {
         File languageFile = new File(messagesDirectory, language + ".yml");
         if (!languageFile.exists()) {
            logger.warning("Language file for '" + language + "' not found, using defaults");
            return createDefault(language, logger);
         } else {
            try {
               FileConfiguration config = YamlConfiguration.loadConfiguration(languageFile);
               ConfigurationSection source = config;
               if (config.isConfigurationSection("messages")) {
                  source = config.getConfigurationSection("messages");
               }

               Map<String, String> messages = new HashMap<>();

               for (MessageKey key : MessageKey.values()) {
                  String message = source != null ? source.getString(key.getKey()) : null;
                  if (message != null) {
                     messages.put(key.getKey(), message);
                  } else {
                     logger.warning("Missing message key: " + key.getKey() + " in language file: " + language);
                     messages.put(key.getKey(), getDefaultMessage(key));
                  }
               }

               String legacyForcertp = source != null ? source.getString("forcertp-target-notify") : null;
               if (legacyForcertp != null && (source == null || !source.contains("forcertp-target-notification"))) {
                  messages.put("forcertp-target-notification", legacyForcertp);
                  logger.info("Migrated legacy message key 'forcertp-target-notify' to 'forcertp-target-notification'");
               }

               logger.info("Locale: " + language + " (" + messages.size() + " messages)");
               return new MessageProvider(messages, language, logger);
            } catch (Exception var12) {
               logger.severe("Failed to load language file for '" + language + "': " + var12.getMessage());
               return createDefault(language, logger);
            }
         }
      } else {
         logger.warning("Messages directory does not exist, using defaults");
         return createDefault(language, logger);
      }
   }

   public static MessageProvider createDefault(String language, Logger logger) {
      Map<String, String> messages = new HashMap<>();

      for (MessageKey key : MessageKey.values()) {
         messages.put(key.getKey(), getDefaultMessage(key));
      }

      return new MessageProvider(messages, language, logger);
   }

   public String getMessage(MessageKey key) {
      return this.messages.getOrDefault(key.getKey(), getDefaultMessage(key));
   }

   public Component format(MessageKey key, Map<String, String> placeholders) {
      return this.format(key, placeholders, null);
   }

   public Component format(MessageKey key) {
      return this.format(key, null, null);
   }

   public Component format(MessageKey key, Map<String, String> placeholders, Object player) {
      String template = this.getMessage(key);
      int h = placeholders == null ? 0 : placeholders.hashCode();
      if (player != null) {
         try {
            Method getUniqueId = player.getClass().getMethod("getUniqueId");
            Object uuid = getUniqueId.invoke(player);
            h = 31 * h + (uuid != null ? uuid.hashCode() : player.hashCode());
         } catch (Throwable var14) {
            h = 31 * h + player.hashCode();
         }
      }

      Map<Integer, Component> inner = this.componentCache.computeIfAbsent(key.getKey(), k -> new HashMap<>());
      Component cached = inner.get(h);
      if (cached != null) {
         return cached;
      } else {
         String processed = template;
         if (placeholders != null && !placeholders.isEmpty()) {
            for (Entry<String, String> entry : placeholders.entrySet()) {
               String placeholderName = entry.getKey();
               String val = entry.getValue();
               processed = processed.replace("<" + placeholderName + ">", val).replace("[" + placeholderName + "]", val);
            }
         }

         Component formatted;
         if (player != null) {
            try {
               PlatformMessageService svc = PlatformMessageServiceRegistry.get();
               if (svc != null) {
                  Component tmp = MessageFormatter.format(processed);
                  formatted = svc.resolvePlaceholdersComponent(player, tmp, this.logger);
               } else {
                  String replaced = PlaceholderUtil.resolvePlaceholders(player, processed, this.logger);
                  formatted = MessageFormatter.format(replaced);
               }
            } catch (Throwable var13) {
               formatted = MessageFormatter.format(processed);
            }
         } else {
            formatted = MessageFormatter.format(processed);
         }

         inner.put(h, formatted);
         return formatted;
      }
   }

   public Component format(MessageKey key, Object player) {
      return this.format(key, null, player);
   }

   public String getLanguage() {
      return this.language;
   }

   private static String getDefaultMessage(MessageKey key) {
      return switch (key) {
         case TELEPORTING -> "<gray>Searching for a safe location...</gray>";
         case TELEPORT_SUCCESS -> "<green>Teleported to <white><x></white>, <white><z></white> in <white><world></white>.</green>";
         case TELEPORT_FAILED -> "<red>Unable to find a safe location. Please try again.</red>";
         case TELEPORT_FAILED_BIOME -> "<red>No valid biome was found. Please try again or try a different biome filter.</red>";
         case TELEPORT_FALLBACK_SUCCESS -> "<yellow>No locations found through search. Falling back to a cached random location: (<white><x></white>, <white><z></white>).</yellow>";
         case TELEPORT_FALLBACK_NO_CACHE -> "<red>No cached locations are available for teleportation. Please wait for locations to be pre-cached.</red>";
         case TELEPORT_FAILED_SEARCH -> "<red>Search failed: No valid locations found.</red>";
         case WORLD_MISSING -> "<red>The configured world '<white><world></white>' is not available.</red>";
         case JOIN_SEARCHING -> "<gray>Finding you a safe place to explore...</gray>";
         case INSUFFICIENT_FUNDS -> "<red>You need <white><cost></white> coins to use random teleport.</red>";
         case QUEUE_QUEUED -> "<gray>You joined the random teleport queue. Position: <white><position></white>.</gray>";
         case QUEUE_FULL -> "<red>The random teleport queue is currently full. Please try again soon.</red>";
         case COUNTDOWN_START -> "<yellow>Teleporting in <white><seconds></white> seconds...</yellow>";
         case COUNTDOWN_TICK -> "<gray><seconds>...</gray>";
         case COUNTDOWN_MOVE_WARN -> "<yellow>Stop moving or your teleport will be cancelled!</yellow>";
         case COUNTDOWN_MOVE_CANCEL -> "<red>Teleport cancelled because you moved.</red>";
         case COOLDOWN -> "<red>You must wait <white><seconds></white> seconds before using /rtp again.</red>";
         case LIMIT_DAILY -> "<red>You have reached your daily /rtp limit for this world.</red>";
         case LIMIT_WEEKLY -> "<red>You have reached your weekly /rtp limit for this world.</red>";
         case GUI_CACHE_FILTER_INFO -> "<yellow>Only showing options with cached RTP locations available.</yellow>";
         case GUI_NO_DESTINATIONS -> "<red>No teleport destinations are currently available.</red>";
         case GUI_COOLDOWN -> "<red>You cannot open the teleport GUI while on cooldown.</red>";
         case NETWORK_SERVICE_UNAVAILABLE -> "<red>Unable to connect to the network service right now. Please try again later.</red>";
         case NETWORK_SERVER_OFFLINE -> "<red>That server is currently offline.</red>";
         case COMMAND_NO_PERMISSION_RELOAD -> "<red>You do not have permission to reload EzRTP.</red>";
         case COMMAND_RELOAD_SUCCESS -> "<green><bold>✓</bold> EzRTP configuration reloaded successfully!</green>";
         case COMMAND_NO_PERMISSION_STATS -> "<red>You do not have permission to view EzRTP statistics.</red>";
         case COMMAND_SERVICE_NOT_INITIALIZED -> "<red>Teleport service not initialized.</red>";
         case COMMAND_NO_PERMISSION -> "<red>You do not have permission to use this command.</red>";
         case COMMAND_PLAYER_ONLY -> "<red>This command may only be used by players.</red>";
         case FORCERTP_NO_PERMISSION -> "<red>You do not have permission to use forced random teleport.</red>";
         case FORCERTP_PLAYER_NOT_FOUND -> "<red>Player '<white><player></white>' not found or is not online.</red>";
         case FORCERTP_SUCCESS -> "<green>Forcing random teleport for player <white><player></white>...</green>";
         case FORCERTP_TARGET_NOTIFY -> "<yellow>You are being teleported by an administrator...</yellow>";
         case FORCERTP_INVALID_USAGE -> "<red>Usage: /rtp forcertp <player> [world]</red>";
         case FORCERTP_WORLD_MISSING -> "<red>No RTP settings found for world: <white><world></white></red>";
         case FORCERTP_SERVICE_UNAVAILABLE -> "<red>Teleport service not initialized.</red>";
         case FORCERTP_EXECUTOR_NOTIFICATION -> "<green>Forcing random teleport for player <white><player></white>...</green>";
         case FORCERTP_TARGET_NOTIFICATION -> "<yellow>You are being teleported by an administrator...</yellow>";
         case HEATMAP_DISABLED -> "<red>Heatmap is disabled. Set <white>heatmap.enabled: true</white> in rtp.yml to enable it.</red>";
         case HEATMAP_BIOME_CACHING_DISABLED -> "<red>Biome caching is not enabled. Enable it in config.yml to use heatmap features.</red>";
         case HEATMAP_INVALID_BIOME -> "<red>Invalid biome: <white><biome></white></red>";
         case HEATMAP_INSUFFICIENT_DATA -> "<red>Not enough RTP data to generate a meaningful heatmap.</red>";
         case HEATMAP_SIMULATION_ADDED -> "<green>Added <white><count></white> simulated RTP sample<s> to <white><world></white> using the <white><pattern></white> pattern.</green>";
         case HEATMAP_SIMULATION_CLEARED -> "<green>Cleared <white><count></white> simulated RTP sample<s> from <white><world></white>.</green>";
         case HEATMAP_SIMULATION_LIMIT -> "<red>Amount too large. Maximum per command: <white><limit></white></red>";
         case HEATMAP_SIMULATION_INVALID_AMOUNT -> "<red>Invalid amount: <white><amount></white></red>";
         case HEATMAP_SIMULATION_WORLD_MISSING -> "<red>World not found: <white><world></white></red>";
         case HEATMAP_SAVE_SUCCESS -> "<green>Heatmap saved successfully!</green>";
         case HEATMAP_SAVE_FAILED -> "<red>Failed to save heatmap. Check console for errors.</red>";
         case FAKE_USAGE -> "<red>Usage: /rtp fake <amount|clear> [world]</red>";
         case FAKE_WORLD_MISSING -> "<red>World not found: <white><world></white></red>";
         case FAKE_WORLD_REQUIRED_CONSOLE -> "<red>You must specify a world when using this command from console.</red>";
         case FAKE_INVALID_AMOUNT -> "<red>Invalid amount: <white><amount></white></red>";
         case FAKE_AMOUNT_NEGATIVE -> "<red>Amount must be positive.</red>";
         case FAKE_AMOUNT_TOO_LARGE -> "<red>Amount too large. Maximum per command: <white><limit></white></red>";
         case FAKE_SIMULATION_STORE_MISSING -> "<red>Heatmap simulation store not initialized.</red>";
         case FAKE_CONFIG_MISSING -> "<red>EzRTP configuration not loaded yet.</red>";
         case FAKE_SETTINGS_MISSING -> "<red>No RTP settings found for world: <white><world></white></red>";
         case FAKE_SERVICE_MISSING -> "<red>Teleport service not initialized.</red>";
         case FAKE_SIMULATION_STATUS -> "<gray>World now has <white><count></white> simulated sample<s> (capacity <white><capacity></white>).</gray>";
         case FAKE_SIMULATION_HINT -> "<gray>Use <white>/rtp heatmap</white> or <white>/rtp heatmap save</white> to visualize the distribution.</gray>";
         case STATS_NO_BIOME_DATA -> "<yellow>No biome data available yet. Biome statistics are collected as players use /rtp with biome filtering enabled.</yellow>";
         case STATS_CACHE_INFO -> "<gold><bold>Cache Information:</bold></gold>";
         case STATS_BIOME_ACTIVITY_HEADER -> "<gold><bold>Biomes by Activity</bold> <gray>(Page <page>/<total>, Total: <count> biomes):</gray></gold>";
         case STATS_NAVIGATION -> "<gold><bold>Navigation:</bold></gold>";
         case STATS_LEGEND -> "<gold><bold>Legend:</bold></gold>";
         case COUNTDOWN_PVP_CANCEL -> "<red>Teleport cancelled because you entered combat.</red>";
         case QUEUE_PVP_TAG_CANCEL -> "<red>Your queued teleport was cancelled because you entered combat.</red>";
      };
   }
}
