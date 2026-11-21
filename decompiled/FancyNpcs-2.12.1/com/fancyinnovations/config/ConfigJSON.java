package com.fancyinnovations.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import de.oliver.fancyanalytics.logger.ExtendedFancyLogger;
import de.oliver.fancyanalytics.logger.properties.ThrowableProperty;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigJSON {
   private final ExtendedFancyLogger logger;
   private final File configFile;
   private final Map<String, ConfigField<?>> fields;
   private final Map<String, Object> values;
   private final Gson gson;

   public ConfigJSON(ExtendedFancyLogger logger, String configFilePath) {
      this.logger = logger;
      this.configFile = new File(configFilePath);
      this.fields = new ConcurrentHashMap<>();
      this.values = new ConcurrentHashMap<>();
      this.gson = new GsonBuilder().serializeNulls().disableHtmlEscaping().setPrettyPrinting().create();
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
      if (!this.configFile.exists()) {
         try {
            File parent = this.configFile.getParentFile();
            if (parent != null && !parent.exists()) {
               parent.mkdirs();
            }

            if (!this.configFile.createNewFile()) {
               this.logger.error("Failed to create config file: " + this.configFile.getAbsolutePath());
               return;
            }
         } catch (IOException var11) {
            this.logger.error("Error creating config file: " + this.configFile.getAbsolutePath(), ThrowableProperty.of(var11));
            return;
         }

         JsonObject root = new JsonObject();

         for (ConfigField<?> field : this.fields.values()) {
            this.setDefault(root, field);
         }

         this.saveJson(root);
      } else {
         JsonObject root;
         try (FileReader reader = new FileReader(this.configFile)) {
            JsonElement parsed = JsonParser.parseReader(reader);
            if (parsed != null && parsed.isJsonObject()) {
               root = parsed.getAsJsonObject();
            } else {
               root = new JsonObject();
            }
         } catch (Exception var14) {
            this.logger.error("Error reading config file: " + this.configFile.getAbsolutePath(), ThrowableProperty.of(var14));
            root = new JsonObject();
         }

         boolean dirty = false;

         for (Entry<String, ConfigField<?>> entry : this.fields.entrySet()) {
            String path = entry.getKey();
            ConfigField<?> field = entry.getValue();
            if (!field.forRemoval()) {
               JsonElement elem = this.getElement(root, path);
               if (elem != null && !elem.isJsonNull()) {
                  try {
                     Object deserialized = this.gson.fromJson(elem, field.type());
                     if (deserialized != null && field.type().isInstance(deserialized)) {
                        this.values.put(path, deserialized);
                     } else {
                        Object converted = this.tryConvertNumber(deserialized, field.type());
                        if (converted != null) {
                           this.values.put(path, converted);
                        } else {
                           this.logger.warn("Value for path '" + path + "' is not of type '" + field.type().getSimpleName() + "'");
                           this.setDefault(root, field);
                           dirty = true;
                        }
                     }
                  } catch (ClassCastException | JsonSyntaxException var12) {
                     this.logger.warn("Failed to parse value for path '" + path + "': " + var12.getMessage());
                     this.setDefault(root, field);
                     dirty = true;
                  }
               } else {
                  this.logger.debug("Path '" + path + "' not found in config");
                  this.setDefault(root, field);
                  dirty = true;
               }
            } else if (this.isSet(root, path)) {
               this.logger.debug("Removing path '" + path + "' from config");
               this.removePath(root, path);
               dirty = true;
            }
         }

         if (dirty) {
            this.saveJson(root);
         }
      }
   }

   private void setDefault(JsonObject root, ConfigField<?> field) {
      this.logger.debug("Setting default value for path '" + field.path() + "': " + field.defaultValue());
      JsonElement elem = this.gson.toJsonTree(field.defaultValue(), field.type());
      this.setElement(root, field.path(), elem);
   }

   private void saveJson(JsonObject root) {
      try (FileWriter writer = new FileWriter(this.configFile)) {
         this.gson.toJson((JsonElement)root, writer);
      } catch (IOException var7) {
         this.logger.error("Error saving config file: " + this.configFile.getAbsolutePath(), ThrowableProperty.of(var7));
      }
   }

   private JsonElement getElement(JsonObject root, String path) {
      String[] parts = path.split("\\.");
      JsonElement current = root;

      for (String p : parts) {
         if (!current.isJsonObject()) {
            return null;
         }

         JsonObject obj = current.getAsJsonObject();
         if (!obj.has(p)) {
            return null;
         }

         current = obj.get(p);
      }

      return current;
   }

   private void setElement(JsonObject root, String path, JsonElement value) {
      String[] parts = path.split("\\.");
      JsonObject current = root;

      for (int i = 0; i < parts.length - 1; i++) {
         String p = parts[i];
         if (current.has(p) && current.get(p).isJsonObject()) {
            current = current.getAsJsonObject(p);
         } else {
            JsonObject child = new JsonObject();
            current.add(p, child);
            current = child;
         }
      }

      current.add(parts[parts.length - 1], value);
   }

   private boolean isSet(JsonObject root, String path) {
      return this.getElement(root, path) != null;
   }

   private void removePath(JsonObject root, String path) {
      String[] parts = path.split("\\.");
      JsonObject current = root;

      for (int i = 0; i < parts.length - 1; i++) {
         String p = parts[i];
         if (!current.has(p) || !current.get(p).isJsonObject()) {
            return;
         }

         current = current.getAsJsonObject(p);
      }

      current.remove(parts[parts.length - 1]);
   }

   private Object tryConvertNumber(Object value, Class<?> target) {
      if (!(value instanceof Number num)) {
         return null;
      } else if (target == Integer.class || target == int.class) {
         return num.intValue();
      } else if (target == Long.class || target == long.class) {
         return num.longValue();
      } else if (target == Double.class || target == double.class) {
         return num.doubleValue();
      } else if (target == Float.class || target == float.class) {
         return num.floatValue();
      } else if (target == Short.class || target == short.class) {
         return num.shortValue();
      } else {
         return target != Byte.class && target != byte.class ? null : num.byteValue();
      }
   }
}
