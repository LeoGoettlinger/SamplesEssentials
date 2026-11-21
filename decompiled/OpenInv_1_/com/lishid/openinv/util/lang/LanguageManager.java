package com.lishid.openinv.util.lang;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;
import java.util.logging.Level;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LanguageManager {
   private final Plugin plugin;
   private final File folder;
   private final String defaultLocale;
   private final Map<String, YamlConfiguration> locales;

   public LanguageManager(@NotNull Plugin plugin, @NotNull String defaultLocale) {
      this.plugin = plugin;
      this.defaultLocale = defaultLocale;
      this.locales = new HashMap<>();
      this.folder = new File(plugin.getDataFolder(), "locale");
      if (!this.folder.exists() && !this.folder.mkdirs()) {
         plugin.getLogger().warning(() -> "Unable to create " + this.folder.getPath() + "! Languages may not be editable.");
      }

      this.reload();
   }

   public void reload() {
      this.locales.clear();
      this.getOrLoadLocale(this.defaultLocale);
   }

   @NotNull
   private YamlConfiguration getOrLoadLocale(@NotNull String locale) {
      YamlConfiguration loaded = this.locales.get(locale);
      if (loaded != null) {
         return loaded;
      } else {
         LanguageManager.LangLocation lang = this.bestMatch(locale, null);
         if (!locale.equals(lang.locale)) {
            loaded = this.locales.get(lang.locale);
            if (loaded != null) {
               this.locales.put(locale, loaded);
               return loaded;
            }
         }

         YamlConfiguration localeConfig = this.loadLocale(lang);
         if (!locale.equals(this.defaultLocale)) {
            this.addTranslationFallthrough(lang, localeConfig);
            if (this.plugin.getConfig().getBoolean("settings.secret.warn-about-guess-section", true) && localeConfig.isConfigurationSection("guess")) {
               this.plugin.getLogger().info(() -> "[LanguageManager] Missing translations from " + lang.locale + ".yml! Check the guess section!");
            }
         }

         this.locales.put(locale, localeConfig);
         this.locales.put(lang.locale, localeConfig);
         return localeConfig;
      }
   }

   @NotNull
   private LanguageManager.LangLocation bestMatch(@NotNull String locale, @Nullable LanguageManager.LangLocation initial) {
      File file = new File(this.folder, locale + ".yml");
      InputStream bundled = this.plugin.getResource("locale/" + locale + ".yml");
      if (!file.exists() && bundled == null) {
         if (initial == null) {
            initial = new LanguageManager.LangLocation(locale, file, null);
         }

         int lastSeparator = locale.lastIndexOf(95);
         return lastSeparator < 1 ? initial : this.bestMatch(locale.substring(0, lastSeparator), initial);
      } else {
         return new LanguageManager.LangLocation(locale, file, bundled);
      }
   }

   @NotNull
   private YamlConfiguration loadLocale(@NotNull LanguageManager.LangLocation lang) {
      YamlConfiguration localeConfigDefaults;
      if (lang.bundled == null) {
         localeConfigDefaults = new YamlConfiguration();
      } else {
         try (BufferedReader reader = new BufferedReader(new InputStreamReader(lang.bundled, StandardCharsets.UTF_8))) {
            localeConfigDefaults = YamlConfiguration.loadConfiguration(reader);
         } catch (IOException var12) {
            this.plugin.getLogger().log(Level.WARNING, var12, () -> "[LanguageManager] Unable to load resource " + lang.locale + ".yml");
            localeConfigDefaults = new YamlConfiguration();
         }
      }

      if (!lang.file.exists()) {
         try {
            localeConfigDefaults.save(lang.file);
         } catch (IOException var8) {
            this.plugin.getLogger().log(Level.WARNING, var8, () -> "[LanguageManager] Unable to save resource " + lang.locale + ".yml");
         }

         return localeConfigDefaults;
      } else {
         YamlConfiguration localeConfig = YamlConfiguration.loadConfiguration(lang.file);
         List<String> newKeys = this.getMissingKeys(localeConfigDefaults, localeConfig::isSet);
         if (newKeys.isEmpty()) {
            return localeConfig;
         } else {
            ConfigurationSection guess = localeConfig.getConfigurationSection("guess");

            for (String newKey : newKeys) {
               localeConfig.set(newKey, localeConfigDefaults.get(newKey));
               if (guess != null) {
                  guess.set(newKey, null);
               }
            }

            if (guess != null && guess.getKeys(false).isEmpty()) {
               localeConfig.set("guess", null);
            }

            this.plugin.getLogger().info(() -> "[LanguageManager] Added new translation keys to " + lang.locale + ".yml: " + String.join(", ", newKeys));

            try {
               localeConfig.save(lang.file);
            } catch (IOException var9) {
               this.plugin.getLogger().log(Level.WARNING, var9, () -> "[LanguageManager] Unable to save resource " + lang.locale + ".yml");
            }

            return localeConfig;
         }
      }
   }

   private void addTranslationFallthrough(@NotNull LanguageManager.LangLocation location, @NotNull YamlConfiguration localeConfig) {
      YamlConfiguration defaultLocaleConfig = this.locales.get(this.defaultLocale);
      List<String> missingKeys = this.getMissingKeys(defaultLocaleConfig, keyx -> localeConfig.isSet(keyx) || localeConfig.isSet("guess." + keyx));
      if (!missingKeys.isEmpty()) {
         for (String key : missingKeys) {
            localeConfig.set("guess." + key, defaultLocaleConfig.get(key));
         }

         try {
            localeConfig.save(location.file);
         } catch (IOException var7) {
            this.plugin.getLogger().log(Level.WARNING, var7, () -> "[LanguageManager] Unable to save resource " + location.locale + ".yml");
         }
      }

      localeConfig.setDefaults(defaultLocaleConfig);
   }

   @NotNull
   private List<String> getMissingKeys(@NotNull Configuration configurationDefault, @NotNull Predicate<String> nodeSetPredicate) {
      List<String> missingKeys = new ArrayList<>();

      for (String key : configurationDefault.getKeys(true)) {
         if (!configurationDefault.isConfigurationSection(key) && !nodeSetPredicate.test(key)) {
            missingKeys.add(key);
         }
      }

      return missingKeys;
   }

   @Nullable
   public String getValue(@NotNull String key, @Nullable String locale) {
      String value = this.getOrLoadLocale(locale == null ? this.defaultLocale : locale.toLowerCase(Locale.ENGLISH)).getString(key);
      return value != null && !value.isBlank() ? ChatColor.translateAlternateColorCodes('&', value) : null;
   }

   @Nullable
   public String getValue(@NotNull String key, @Nullable String locale, @NotNull Replacement... replacements) {
      String value = this.getValue(key, locale);
      if (value == null) {
         return null;
      } else {
         for (Replacement replacement : replacements) {
            value = value.replace(replacement.placeholder(), replacement.value());
         }

         return value;
      }
   }

   @Nullable
   public String getLocalizedMessage(@NotNull CommandSender sender, @NotNull String key) {
      return this.getValue(key, this.getLocale(sender));
   }

   @Nullable
   public String getLocalizedMessage(@NotNull CommandSender sender, @NotNull String key, @NotNull Replacement... replacements) {
      return this.getValue(key, this.getLocale(sender), replacements);
   }

   @NotNull
   private String getLocale(@NotNull CommandSender sender) {
      return sender instanceof Player player ? player.getLocale() : this.plugin.getConfig().getString("settings.locale", "en");
   }

   public void sendMessage(@NotNull CommandSender sender, @NotNull String key) {
      String message = this.getLocalizedMessage(sender, key);
      if (message != null && !message.isEmpty()) {
         sender.sendMessage(message);
      }
   }

   public void sendMessage(@NotNull CommandSender sender, @NotNull String key, @NotNull Replacement... replacements) {
      String message = this.getLocalizedMessage(sender, key, replacements);
      if (message != null && !message.isEmpty()) {
         sender.sendMessage(message);
      }
   }

   public void sendSystemMessage(@NotNull Player player, @NotNull String key) {
      String message = this.getLocalizedMessage(player, key);
      if (message != null) {
         int newline = message.indexOf(10);
         if (newline != -1) {
            message = message.substring(0, newline);
         }

         if (!message.isEmpty()) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(message));
         }
      }
   }

   private record LangLocation(@NotNull String locale, @NotNull File file, @Nullable InputStream bundled) {
   }
}
