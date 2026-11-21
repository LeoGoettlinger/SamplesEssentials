package me.byteful.plugin.leveltools.libs.nbtapi.utils;

import java.lang.reflect.Field;
import me.byteful.plugin.leveltools.libs.nbtapi.NbtApiException;
import me.byteful.plugin.leveltools.libs.nbtapi.utils.nmsmappings.MojangToMapping;

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
