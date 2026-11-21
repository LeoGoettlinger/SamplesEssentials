package com.artillexstudios.axafkzone.libs.axapi.database;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.List;
import java.util.Map.Entry;

public class DatabaseTypes {
   private static DatabaseType defaultType;
   private static final Object2ObjectArrayMap<String, DatabaseType> registry = new Object2ObjectArrayMap();

   public static DatabaseType defaultType() {
      return defaultType;
   }

   public static DatabaseType register(DatabaseType type) {
      return register(type, false);
   }

   public static DatabaseType register(DatabaseType type, boolean def) {
      registry.put(type.name(), type);
      if (def) {
         defaultType = type;
      }

      return type;
   }

   public static DatabaseType fetch(String name) {
      ObjectIterator var1 = registry.entrySet().iterator();

      while (var1.hasNext()) {
         Entry<String, DatabaseType> entry = (Entry<String, DatabaseType>)var1.next();
         if (entry.getKey().equalsIgnoreCase(name)) {
            return entry.getValue();
         }
      }

      return null;
   }

   public static List<String> keys() {
      return registry.keySet().stream().toList();
   }
}
