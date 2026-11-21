package com.fancyinnovations.config;

import de.oliver.fancyanalytics.logger.ExtendedFancyLogger;
import de.oliver.fancyanalytics.logger.properties.ThrowableProperty;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.configuration.file.YamlConfiguration;

public class Config {
   private final ExtendedFancyLogger logger;
   private final File configFile;
   private final Map<String, ConfigField<?>> fields;
   private final Map<String, Object> values;

   public Config(ExtendedFancyLogger logger, String configFilePath) {
      this.logger = logger;
      this.configFile = new File(configFilePath);
      this.fields = new ConcurrentHashMap<>();
      this.values = new ConcurrentHashMap<>();
   }

   public void addField(ConfigField<?> field) {
      this.fields.put(field.path(), field);
   }

   public Map<String, ConfigField<?>> getFields() {
      return this.fields;
   }

   public <T> T get(String path) {
      ConfigField<?> field = this.fields.get(path);
      if (field == null) {
         return null;
      } else if (field.forceDefault()) {
         return (T)field.defaultValue();
      } else {
         Object value = this.values.computeIfAbsent(path, k -> field.defaultValue());
         return (T)field.type().cast(value);
      }
   }

   public void reload() {
      if (this.configFile.exists()) {
         YamlConfiguration yaml = YamlConfiguration.loadConfiguration(this.configFile);
         boolean dirty = false;

         for (Entry<String, ConfigField<?>> entry : this.fields.entrySet()) {
            String path = entry.getKey();
            ConfigField<?> field = entry.getValue();
            if (field.forRemoval()) {
               if (yaml.isSet(path)) {
                  this.logger.debug("Removing path '" + path + "' from config");
                  yaml.set(path, null);
                  dirty = true;
               }
            } else if (yaml.isSet(path)) {
               Object value = yaml.get(path);
               if (field.type().isInstance(value)) {
                  this.values.put(path, value);
               } else {
                  this.logger.warn("Value for path '" + path + "' is not of type '" + field.type().getSimpleName());
                  this.setDefault(yaml, field);
                  dirty = true;
               }
            } else {
               this.logger.debug("Path '" + path + "' not found in config");
               this.setDefault(yaml, field);
               dirty = true;
            }
         }

         if (dirty) {
            this.saveYaml(yaml);
         }
      } else {
         try {
            if (!this.configFile.createNewFile()) {
               this.logger.error("Failed to create config file: " + this.configFile.getAbsolutePath());
               return;
            }
         } catch (IOException var8) {
            this.logger.error("Error creating config file: " + this.configFile.getAbsolutePath(), ThrowableProperty.of(var8));
            return;
         }

         YamlConfiguration yaml = YamlConfiguration.loadConfiguration(this.configFile);

         for (ConfigField<?> field : this.fields.values()) {
            this.setDefault(yaml, field);
         }

         this.saveYaml(yaml);
      }
   }

   private void setDefault(YamlConfiguration yaml, ConfigField<?> field) {
      this.logger.debug("Setting default value for path '" + field.path() + "': " + field.defaultValue());
      yaml.set(field.path(), field.defaultValue());
      yaml.setInlineComments(field.path(), List.of(field.description()));
   }

   private void saveYaml(YamlConfiguration yaml) {
      try {
         yaml.save(this.configFile);
      } catch (IOException var3) {
         this.logger.error("Error saving config file: " + this.configFile.getAbsolutePath(), ThrowableProperty.of(var3));
      }
   }
}
