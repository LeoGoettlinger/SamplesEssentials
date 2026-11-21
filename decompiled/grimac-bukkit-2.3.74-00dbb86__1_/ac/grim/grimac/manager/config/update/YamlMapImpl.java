package ac.grim.grimac.manager.config.update;

import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class YamlMapImpl implements YamlMap {
   private final Map<String, Object> root;
   private final WriteLog writeLog;

   YamlMapImpl(@NotNull Map<String, Object> root, @NotNull WriteLog writeLog) {
      this.root = root;
      this.writeLog = writeLog;
   }

   @Nullable
   @Override
   public Object get(@NotNull String dottedPath) {
      return walkRead(this.root, dottedPath);
   }

   @Nullable
   @Override
   public String getString(@NotNull String dottedPath) {
      Object v = this.get(dottedPath);
      return v == null ? null : v.toString();
   }

   @Nullable
   @Override
   public Integer getInt(@NotNull String dottedPath) {
      Object v = this.get(dottedPath);
      if (v instanceof Number n) {
         return n.intValue();
      } else if (v instanceof String s) {
         try {
            return Integer.parseInt(s.trim());
         } catch (NumberFormatException var5) {
            return null;
         }
      } else {
         return null;
      }
   }

   @Nullable
   @Override
   public Long getLong(@NotNull String dottedPath) {
      Object v = this.get(dottedPath);
      if (v instanceof Number n) {
         return n.longValue();
      } else if (v instanceof String s) {
         try {
            return Long.parseLong(s.trim());
         } catch (NumberFormatException var5) {
            return null;
         }
      } else {
         return null;
      }
   }

   @Nullable
   @Override
   public Double getDouble(@NotNull String dottedPath) {
      Object v = this.get(dottedPath);
      if (v instanceof Number n) {
         return n.doubleValue();
      } else if (v instanceof String s) {
         try {
            return Double.parseDouble(s.trim());
         } catch (NumberFormatException var5) {
            return null;
         }
      } else {
         return null;
      }
   }

   @Nullable
   @Override
   public Boolean getBool(@NotNull String dottedPath) {
      Object v = this.get(dottedPath);
      if (v instanceof Boolean b) {
         return b;
      } else {
         if (v instanceof String s) {
            String t = s.trim();
            if (t.equalsIgnoreCase("true")) {
               return Boolean.TRUE;
            }

            if (t.equalsIgnoreCase("false")) {
               return Boolean.FALSE;
            }
         }

         return null;
      }
   }

   @Nullable
   @Override
   public List<?> getList(@NotNull String dottedPath) {
      return this.get(dottedPath) instanceof List<?> l ? l : null;
   }

   @Nullable
   @Override
   public Map<String, Object> getMap(@NotNull String dottedPath) {
      return (Map<String, Object>)(this.get(dottedPath) instanceof Map<?, ?> m ? m : null);
   }

   @Override
   public boolean has(@NotNull String dottedPath) {
      return walkRead(this.root, dottedPath) != null;
   }

   @NotNull
   @Override
   public YamlMap put(@NotNull String dottedPath, @NotNull Object value) {
      walkWrite(this.root, dottedPath, value);
      this.writeLog.recordPut(dottedPath, value);
      return this;
   }

   @NotNull
   @Override
   public YamlMap remove(@NotNull String dottedPath) {
      walkRemove(this.root, dottedPath);
      this.writeLog.recordRemove(dottedPath);
      return this;
   }

   @NotNull
   @Override
   public YamlMap rename(@NotNull String oldPath, @NotNull String newPath) {
      Object existing = walkRead(this.root, oldPath);
      if (existing == null) {
         return this;
      } else {
         this.put(newPath, existing);
         this.remove(oldPath);
         return this;
      }
   }

   @Nullable
   private static Object walkRead(Map<String, Object> root, String dottedPath) {
      if (dottedPath.isEmpty()) {
         return root;
      } else {
         String[] parts = dottedPath.split("\\.");
         Object cursor = root;

         for (String part : parts) {
            if (!(cursor instanceof Map<?, ?> m)) {
               return null;
            }

            cursor = m.get(part);
            if (cursor == null) {
               return null;
            }
         }

         return cursor;
      }
   }

   private static void walkWrite(Map<String, Object> root, String dottedPath, Object value) {
      String[] parts = dottedPath.split("\\.");
      Map<String, Object> cursor = root;

      for (int i = 0; i < parts.length - 1; i++) {
         String part = parts[i];
         Object next = cursor.get(part);
         if (!(next instanceof Map)) {
            next = new LinkedHashMap();
            cursor.put(part, next);
         }

         cursor = (Map<String, Object>)next;
      }

      cursor.put(parts[parts.length - 1], value);
   }

   private static void walkRemove(Map<String, Object> root, String dottedPath) {
      String[] parts = dottedPath.split("\\.");
      cursor = root;

      for (int i = 0; i < parts.length - 1; i++) {
         if (!(cursor.get(parts[i]) instanceof Map<String, Object> cursor)) {
            return;
         }
      }

      cursor.remove(parts[parts.length - 1]);
   }
}
