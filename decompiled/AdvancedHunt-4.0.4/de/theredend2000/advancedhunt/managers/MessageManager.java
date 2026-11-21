package de.theredend2000.advancedhunt.managers;

import de.theredend2000.advancedhunt.Main;
import de.theredend2000.advancedhunt.util.ConfigMigrationHandler;
import de.theredend2000.advancedhunt.util.ConfigUpdater;
import de.theredend2000.advancedhunt.util.HexColor;
import java.io.File;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class MessageManager {
   private final Main plugin;
   private FileConfiguration messagesConfig;
   private File messagesFile;
   private final Map<String, String> messageCache = new HashMap<>();
   private final Map<String, List<String>> messageListCache = new HashMap<>();

   public MessageManager(Main plugin) {
      this.plugin = plugin;
      this.loadMessages();
   }

   public void loadMessages() {
      String lang = this.plugin.getConfig().getString("language", "en");
      this.messagesFile = new File(this.plugin.getDataFolder(), "messages/messages_" + lang + ".yml");
      if (!this.messagesFile.exists()) {
         this.messagesFile.getParentFile().mkdirs();
         String resourcePath = "messages/messages_" + lang + ".yml";
         InputStream resource = this.plugin.getResource(resourcePath);
         if (resource != null) {
            this.plugin.saveResource(resourcePath, false);
         } else if (!lang.equals("en")) {
            this.plugin.getLogger().warning("Language file for " + lang + " not found. Falling back to English.");
            this.messagesFile = new File(this.plugin.getDataFolder(), "messages/messages_en.yml");
            if (!this.messagesFile.exists()) {
               this.plugin.saveResource("messages/messages_en.yml", false);
            }
         } else {
            this.plugin.saveResource("messages/messages_en.yml", false);
         }
      }

      String resourcePath = "messages/messages_" + lang + ".yml";
      if (this.plugin.getResource(resourcePath) != null) {
         ConfigUpdater.update(this.plugin, resourcePath, this.messagesFile, ConfigMigrationHandler::migrateMessages);
      } else if (this.messagesFile.getName().equals("messages_en.yml")) {
         ConfigUpdater.update(this.plugin, "messages/messages_en.yml", this.messagesFile, ConfigMigrationHandler::migrateMessages);
      }

      this.messagesConfig = YamlConfiguration.loadConfiguration(this.messagesFile);
      this.messageCache.clear();
      this.messageListCache.clear();
   }

   public String getMessage(String key) {
      if (this.messageCache.containsKey(key)) {
         return this.messageCache.get(key);
      } else {
         String message = this.messagesConfig.getString(key);
         if (message == null) {
            return "Missing message: " + key;
         } else {
            String prefix = this.messagesConfig.getString("prefix", "&6AdvancedHunt &8>> &7");
            message = message.replace("%prefix%", prefix);
            message = HexColor.color(message, '&');
            this.messageCache.put(key, message);
            return message;
         }
      }
   }

   public String getMessage(String key, String... placeholders) {
      String message = this.getMessage(key);

      for (int i = 0; i < placeholders.length; i += 2) {
         if (i + 1 < placeholders.length) {
            message = message.replace(placeholders[i], placeholders[i + 1]);
         }
      }

      return message;
   }

   public String getMessage(String key, boolean applyPrefix) {
      String message = this.messagesConfig.getString(key);
      if (message == null) {
         return "Missing message: " + key;
      } else {
         if (applyPrefix) {
            String prefix = this.messagesConfig.getString("prefix", "&6AdvancedHunt &8>> &7");
            message = message.replace("%prefix%", prefix);
         }

         return ChatColor.translateAlternateColorCodes('&', message);
      }
   }

   public String getMessage(String key, boolean applyPrefix, String... placeholders) {
      String message = this.getMessage(key, applyPrefix);

      for (int i = 0; i < placeholders.length; i += 2) {
         if (i + 1 < placeholders.length) {
            message = message.replace(placeholders[i], placeholders[i + 1]);
         }
      }

      return message;
   }

   public List<String> getMessageList(String key) {
      if (this.messageListCache.containsKey(key)) {
         return this.messageListCache.get(key);
      } else {
         List<String> messages = this.messagesConfig.getStringList(key);
         if (messages == null || messages.isEmpty()) {
            String single = this.messagesConfig.getString(key);
            if (single == null) {
               messages = new ArrayList<>();
               messages.add("Missing message list: " + key);
               return messages;
            }

            messages = new ArrayList<>();
            messages.add(single);
         }

         String prefix = this.messagesConfig.getString("prefix", "&6AdvancedHunt &8>> &7");
         List<String> processedMessages = messages.stream()
            .map(msg -> msg.replace("%prefix%", prefix))
            .map(msg -> ChatColor.translateAlternateColorCodes('&', msg))
            .collect(Collectors.toList());
         this.messageListCache.put(key, processedMessages);
         return processedMessages;
      }
   }

   public List<String> getMessageList(String key, String... placeholders) {
      List<String> messages = this.getMessageList(key);
      List<String> replacedMessages = new ArrayList<>();

      for (String msg : messages) {
         for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 < placeholders.length) {
               msg = msg.replace(placeholders[i], placeholders[i + 1]);
            }
         }

         replacedMessages.add(msg);
      }

      return replacedMessages;
   }

   public List<String> getMessageList(String key, boolean applyPrefix) {
      List<String> messages = this.messagesConfig.getStringList(key);
      if (messages == null || messages.isEmpty()) {
         String single = this.messagesConfig.getString(key);
         if (single == null) {
            messages = new ArrayList<>();
            messages.add("Missing message list: " + key);
            return messages;
         }

         messages = new ArrayList<>();
         messages.add(single);
      }

      String prefix = this.messagesConfig.getString("prefix", "&6AdvancedHunt &8>> &7");
      return messages.stream()
         .map(msg -> applyPrefix ? msg.replace("%prefix%", prefix) : msg)
         .map(msg -> ChatColor.translateAlternateColorCodes('&', msg))
         .collect(Collectors.toList());
   }

   public List<String> getMessageList(String key, boolean applyPrefix, String... placeholders) {
      List<String> messages = this.getMessageList(key, applyPrefix);
      List<String> replacedMessages = new ArrayList<>();

      for (String msg : messages) {
         for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 < placeholders.length) {
               msg = msg.replace(placeholders[i], placeholders[i + 1]);
            }
         }

         replacedMessages.add(msg);
      }

      return replacedMessages;
   }

   public void reloadMessages() {
      this.loadMessages();
   }

   public String formatDateTime(ZonedDateTime dateTime) {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
      return dateTime.format(formatter);
   }
}
