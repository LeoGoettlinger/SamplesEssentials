package com.earth2me.essentials.utils;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public final class RegistryUtil {
   private static final Table<Class<?>, String, Object> registryCache = HashBasedTable.create();

   private RegistryUtil() {
   }

   public static <T> Object[] values(Class<T> registry) {
      if (registry.getEnumConstants() != null) {
         return registry.getEnumConstants();
      } else {
         T[] values = (T[])registryCache.get(registry, "$values");
         if (values != null) {
            return values;
         } else {
            List<T> set = new ArrayList<>();

            for (Field field : registry.getDeclaredFields()) {
               try {
                  Object value = field.get(null);
                  if (value != null && registry.isAssignableFrom(value.getClass())) {
                     set.add((T)value);
                  }
               } catch (IllegalAccessException | NullPointerException var8) {
               }
            }

            T[] array = (T[])(new Object[set.size()]);

            for (int i = 0; i < set.size(); i++) {
               array[i] = set.get(i);
            }

            registryCache.put(registry, "$values", array);
            return array;
         }
      }
   }

   public static <T> T valueOf(Class<T> registry, String... names) {
      for (String name : names) {
         T value = (T)registryCache.get(registry, name);
         if (value != null) {
            return value;
         }

         try {
            value = (T)registry.getDeclaredField(name).get(null);
            if (value != null) {
               registryCache.put(registry, name, value);
               return value;
            }
         } catch (IllegalAccessException | NoSuchFieldException var8) {
         }
      }

      return null;
   }
}
