package com.lishid.openinv.util;

import java.lang.reflect.Field;
import org.jetbrains.annotations.Nullable;

public final class ReflectionHelper {
   @Nullable
   public static <T> T grabObjectByType(Object holder, Class<T> fieldType) {
      Field field = grabFieldByType(holder.getClass(), fieldType);
      if (field != null) {
         try {
            return fieldType.cast(field.get(holder));
         } catch (IllegalAccessException var4) {
         }
      }

      return null;
   }

   @Nullable
   public static Field grabFieldByType(Class<?> holderType, Class<?> fieldType) {
      for (Field field : holderType.getDeclaredFields()) {
         if (fieldType.isAssignableFrom(field.getType())) {
            field.setAccessible(true);
            return field;
         }
      }

      return holderType.getSuperclass() != null ? grabFieldByType(fieldType, holderType.getSuperclass()) : null;
   }

   private ReflectionHelper() {
      throw new IllegalStateException("Cannot create instance of utility class.");
   }
}
