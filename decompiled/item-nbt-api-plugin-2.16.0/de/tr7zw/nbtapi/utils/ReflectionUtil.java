package de.tr7zw.nbtapi.utils;

import de.tr7zw.nbtapi.NbtApiException;
import de.tr7zw.nbtapi.utils.nmsmappings.MojangToMapping;
import java.lang.reflect.Field;

public final class ReflectionUtil {
   public static Field getMappedField(Class<?> clazz, String mapping) {
      String mojmapName = mapping.split("#")[1];

      try {
         return clazz.getField(mojmapName);
      } catch (SecurityException | NoSuchFieldException var5) {
         try {
            return clazz.getDeclaredField(MojangToMapping.getMapping().get(mapping));
         } catch (Exception var4) {
            throw new NbtApiException("Unable to find field " + mapping + " in class " + clazz.getName(), var4);
         }
      }
   }
}
