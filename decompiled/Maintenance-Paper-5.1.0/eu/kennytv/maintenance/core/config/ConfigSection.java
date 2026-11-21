package eu.kennytv.maintenance.core.config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import org.jetbrains.annotations.Nullable;

public class ConfigSection {
   protected final Config root;
   protected final String currentPath;
   protected Map<String, Object> values;

   ConfigSection(Config root, String currentPath) {
      this(root, currentPath, new LinkedHashMap<>());
   }

   ConfigSection(Config root, String currentPath, Map<String, Object> values) {
      this.root = root;
      this.currentPath = currentPath;
      this.values = values;
   }

   @Nullable
   public <E> E get(String key) {
      return (E)this.getObject(key);
   }

   @Nullable
   public <E> E get(String key, @Nullable E def) {
      Object o = this.getObject(key);
      return (E)(o != null ? o : def);
   }

   @Nullable
   public Object getObject(String key) {
      return this.getObject(key, null);
   }

   @Nullable
   public Object getObject(String key, Object def) {
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
      return o != null ? o : def;
   }

   @Nullable
   public <E> E getOrSet(String key, @Nullable E def) {
      Object o = this.getObject(key);
      if (o != null) {
         return (E)o;
      } else {
         this.set(key, def);
         return def;
      }
   }

   @Nullable
   public ConfigSection getSection(String key) {
      Object o = this.getObject(key);
      return !(o instanceof Map) ? null : new ConfigSection(this.getRoot(), this.getFullKeyInPath(key), (Map<String, Object>)o);
   }

   public ConfigSection getOrCreateSection(String key) {
      Object o = this.getObject(key);
      if (!(o instanceof Map)) {
         this.set(key, new LinkedHashMap());
         return this.getSection(key);
      } else {
         return new ConfigSection(this.getRoot(), this.getFullKeyInPath(key), (Map<String, Object>)o);
      }
   }

   public boolean contains(String key) {
      return this.getObject(key) != null;
   }

   public void set(String key, @Nullable Object value) {
      int nextSeparatorIndex = -1;
      ConfigSection section = this;

      int sectionStartIndex;
      while ((nextSeparatorIndex = key.indexOf(46, sectionStartIndex = nextSeparatorIndex + 1)) != -1) {
         section = section.getOrCreateSection(key.substring(sectionStartIndex, nextSeparatorIndex));
      }

      String sectionKey = key.substring(sectionStartIndex);
      if (value == null) {
         section.values.remove(sectionKey);
         this.getRoot().getComments().remove(this.getFullKeyInPath(key));
      } else {
         section.values.put(sectionKey, value);
      }
   }

   public void move(String key, String toKey) {
      Object o = this.getObject(key);
      if (o != null) {
         this.remove(key);
         this.set(toKey, o);
      }
   }

   public void remove(String key) {
      this.set(key, null);
   }

   public Set<String> getKeys() {
      return this.values.keySet();
   }

   public Map<String, Object> getValues() {
      return this.values;
   }

   public boolean getBoolean(String key) {
      return this.getBoolean(key, false);
   }

   public boolean getBoolean(String key, boolean def) {
      Object o = this.getObject(key);
      return o instanceof Boolean ? (Boolean)o : def;
   }

   @Nullable
   public String getString(String key) {
      return this.get(key);
   }

   @Nullable
   public String getString(String key, @Nullable String def) {
      return this.getObject(key) instanceof String s ? s : def;
   }

   public int getInt(String key) {
      return this.getInt(key, 0);
   }

   public int getInt(String key, int def) {
      return this.getObject(key) instanceof Number number ? number.intValue() : def;
   }

   public double getDouble(String key) {
      return this.getDouble(key, 0.0);
   }

   public double getDouble(String key, double def) {
      return this.getObject(key) instanceof Number number ? number.doubleValue() : def;
   }

   public long getLong(String key) {
      return this.getLong(key, 0L);
   }

   public long getLong(String key, long def) {
      return this.getObject(key) instanceof Number number ? number.longValue() : def;
   }

   @Nullable
   public List<String> getStringList(String key) {
      return this.get(key);
   }

   @Nullable
   public List<String> getStringList(String key, @Nullable List<String> def) {
      List<String> list = this.get(key);
      return list != null ? list : def;
   }

   @Nullable
   public List<Integer> getIntList(String key) {
      return this.get(key);
   }

   @Nullable
   public List<Integer> getIntList(String key, @Nullable List<Integer> def) {
      List<Integer> list = this.get(key);
      return list != null ? list : def;
   }

   public boolean addMissingFields(ConfigSection fromSection) {
      boolean changed = false;

      for (Entry<String, Object> entry : fromSection.values.entrySet()) {
         String key = entry.getKey();
         Object value = this.values.get(key);
         if (value != null) {
            Object newValue = entry.getValue();
            if (value instanceof Map && newValue instanceof Map) {
               changed |= this.getSection(key).addMissingFields(fromSection.getSection(key));
            }
         } else {
            this.values.put(key, entry.getValue());
            changed = true;
         }
      }

      return changed;
   }

   public Config getRoot() {
      return this.root;
   }

   public String getCurrentPath() {
      return this.currentPath;
   }

   protected String getFullKeyInPath(String key) {
      return this.currentPath.isEmpty() ? key : this.currentPath + "." + key;
   }
}
