package com.sk89q.util;

import java.lang.reflect.Field;

public final class ReflectionUtil {
   private ReflectionUtil() {
   }

   public static <T> T getField(Object from, String name) {
      return from instanceof Class ? getField((Class<?>)from, null, name) : getField(from.getClass(), from, name);
   }

   public static <T> T getField(Class<?> checkClass, Object from, String name) {
      while (true) {
         try {
            Field field = checkClass.getDeclaredField(name);
            field.setAccessible(true);
            return (T)field.get(from);
         } catch (IllegalAccessException | NoSuchFieldException var4) {
            if (checkClass.getSuperclass() == Object.class || (checkClass = checkClass.getSuperclass()) == null) {
               return null;
            }
         }
      }
   }
}
