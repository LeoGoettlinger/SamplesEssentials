package com.viaversion.viaversion.util;

import com.viaversion.viaversion.libs.gson.JsonElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ConfigSection {
   protected final Config root;
   protected final String path;
   protected Map<String, Object> values;

   ConfigSection(Config root, String path) {
      this(root, path, new HashMap<>());
   }

   ConfigSection(Config root, String path, Map<String, Object> values) {
      this.root = root;
      this.path = path;
      this.values = values;
   }

   public void set(String path, Object value) {
      this.values.put(path, value);
   }

   public Map<String, Object> getValues() {
      return this.values;
   }

   @Nullable
   public <T> T get(String key) {
      return this.get(key, null);
   }

   public boolean contains(String key) {
      return this.get(key) != null;
   }

   @Nullable
   public <T> T get(String key, @Nullable T def) {
      String[] parts = key.split("\\.");
      ConfigSection section = this;

      for (int i = 0; i < parts.length - 1; i++) {
         section = section.getSection(parts[i]);
         if (section == null) {
            return def;
         }
      }

      String valueKey = parts[parts.length - 1];
      Object o = section.values.get(valueKey);
      return (T)(o != null ? o : def);
   }

   public boolean getBoolean(String key, boolean def) {
      Object o = this.values.get(key);
      return o instanceof Boolean ? (Boolean)o : def;
   }

   @Nullable
   public String getString(String key, @Nullable String def) {
      return this.values.get(key) instanceof String s ? s : def;
   }

   public int getInt(String key, int def) {
      return this.values.get(key) instanceof Number num ? num.intValue() : def;
   }

   public double getDouble(String key, double def) {
      return this.values.get(key) instanceof Number num ? num.doubleValue() : def;
   }

   public List<Integer> getIntegerList(String key) {
      Object o = this.values.get(key);
      return (List<Integer>)(o != null ? (List)o : new ArrayList<>());
   }

   public List<String> getStringList(String key) {
      Object o = this.values.get(key);
      return (List<String>)(o != null ? (List)o : new ArrayList<>());
   }

   public <T> List<T> getListSafe(String key, Class<T> type, String invalidValueMessage) {
      if (this.values.get(key) instanceof List<?> list) {
         List<T> filteredValues = new ArrayList<>(list.size());

         for (Object o1 : list) {
            if (type.isInstance(o1)) {
               filteredValues.add(type.cast(o1));
            } else if (invalidValueMessage != null) {
               this.logger().warning(String.format(invalidValueMessage, o1));
            }
         }

         return filteredValues;
      } else {
         return new ArrayList<>();
      }
   }

   @Nullable
   public JsonElement getSerializedComponent(String key) {
      return this.values.get(key) instanceof String s && !s.isEmpty() ? ComponentUtil.legacyToJson(s) : null;
   }

   @Nullable
   public ConfigSection getSection(String key) {
      Object o = this.get(key);
      return o instanceof Map ? new ConfigSection(this.root(), this.fullKeyInPath(key), (Map<String, Object>)o) : null;
   }

   private String fullKeyInPath(String key) {
      return this.path.isEmpty() ? key : this.path + "." + key;
   }

   public Config root() {
      return this.root;
   }

   protected Logger logger() {
      return this.root().logger();
   }
}
