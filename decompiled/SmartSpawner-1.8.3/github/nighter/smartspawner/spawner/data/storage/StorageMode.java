package github.nighter.smartspawner.spawner.data.storage;

import java.util.Locale;

public enum StorageMode {
   SQLITE,
   MYSQL;

   public static StorageMode fromConfig(String raw) {
      if (raw == null) {
         return SQLITE;
      } else {
         try {
            return valueOf(raw.trim().toUpperCase(Locale.ROOT));
         } catch (IllegalArgumentException var2) {
            return SQLITE;
         }
      }
   }
}
