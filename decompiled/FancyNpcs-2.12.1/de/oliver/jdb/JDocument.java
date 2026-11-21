package de.oliver.jdb;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public class JDocument {
   @NotNull
   private final Map<String, Object> data;

   public JDocument(@NotNull Map<String, Object> data) {
      this.data = data;
   }

   public Object get(String key) {
      return this.getValue(key, Object.class);
   }

   public boolean contains(String key) {
      return this.get(key) != null;
   }

   public Set<String> getKeys(String key) {
      Map<String, Object> map = (Map<String, Object>)this.getValue(key, Map.class);
      return (Set<String>)(map != null ? map.keySet() : new HashSet<>());
   }

   public String getString(String key) {
      return this.getValueOrDefault(key, String.class, "");
   }

   public boolean getBoolean(String key) {
      return this.getValueOrDefault(key, Boolean.class, false);
   }

   public byte getByte(String key) {
      return this.getValueOrDefault(key, Byte.class, (byte)0);
   }

   public short getShort(String key) {
      return this.getValueOrDefault(key, Short.class, (short)0);
   }

   public int getInt(String key) {
      return this.getValueOrDefault(key, Integer.class, 0);
   }

   public long getLong(String key) {
      return this.getValueOrDefault(key, Long.class, 0L);
   }

   public float getFloat(String key) {
      return this.getValueOrDefault(key, Float.class, 0.0F);
   }

   public double getDouble(String key) {
      return this.getValueOrDefault(key, Double.class, 0.0);
   }

   private Object getValue(String key, Class<?> clazz) {
      String[] parts = key.split("\\.");
      Map<String, Object> current = this.data;

      for (int i = 0; i < parts.length; i++) {
         Object value = current.get(parts[i]);
         if (value == null || i < parts.length - 1 && !(value instanceof Map)) {
            return null;
         }

         if (i == parts.length - 1) {
            return clazz.isInstance(value) ? value : null;
         }

         current = (Map<String, Object>)value;
      }

      return null;
   }

   private <T> T getValueOrDefault(String key, Class<T> clazz, T defaultValue) {
      T value = (T)this.getValue(key, clazz);
      return value != null ? value : defaultValue;
   }
}
