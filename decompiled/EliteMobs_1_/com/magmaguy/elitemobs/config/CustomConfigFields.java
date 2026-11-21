package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.config.translations.TranslationsConfig;
import com.magmaguy.elitemobs.versionnotifier.VersionChecker;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public class CustomConfigFields extends com.magmaguy.elitemobs.magmacore.config.CustomConfigFields {
   @Override
   public <T extends Enum<T>> T processEnum(String path, T value, T pluginDefault, Class<T> enumClass, boolean forceWriteDefault) {
      if (enumClass == Material.class && this.configHas(path)) {
         String rawValue = this.fileConfiguration.getString(path);
         if (rawValue != null && rawValue.toUpperCase(Locale.ROOT).endsWith("_SPEAR") && VersionChecker.serverVersionOlderThan(21, 11)) {
            return pluginDefault;
         }
      }

      return super.processEnum(path, value, pluginDefault, enumClass, forceWriteDefault);
   }

   public CustomConfigFields(String filename, boolean isEnabled) {
      super(filename, isEnabled);
   }

   @Override
   public void processConfigFields() {
   }

   @Override
   public String getFilename() {
      return super.getFilename();
   }

   protected String translatable(String filename, String key, String value) {
      return TranslationsConfig.add(filename, key, value);
   }

   protected List<String> translatable(String filename, String key, List<String> value) {
      return TranslationsConfig.add(filename, key, value);
   }

   @Override
   public ConfigurationSection processConfigurationSection(String path, Map<String, Object> value) {
      if (!this.configHas(path) && value != null) {
         this.fileConfiguration.addDefaults(value);
      }

      ConfigurationSection source = this.fileConfiguration.getConfigurationSection(path);
      if (source == null) {
         return null;
      } else {
         List<String> translatableKeys = source.getKeys(true)
            .stream()
            .filter(keyx -> keyx.equalsIgnoreCase("message"))
            .filter(keyx -> source.getString(keyx) != null)
            .toList();
         if (translatableKeys.isEmpty()) {
            return source;
         } else {
            ConfigurationSection translated = new YamlConfiguration().createSection(path);
            copySection(source, translated);

            for (String key : translatableKeys) {
               translated.set(key, TranslationsConfig.add(this.filename, key, source.getString(key)));
            }

            return translated;
         }
      }
   }

   private static void copySection(ConfigurationSection source, ConfigurationSection target) {
      for (String key : source.getKeys(false)) {
         Object value = source.get(key);
         if (value instanceof ConfigurationSection child) {
            copySection(child, target.createSection(key));
         } else {
            target.set(key, value);
         }
      }
   }
}
