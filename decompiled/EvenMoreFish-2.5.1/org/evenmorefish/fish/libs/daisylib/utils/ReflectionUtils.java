package org.evenmorefish.fish.libs.daisylib.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class ReflectionUtils {
   @Nullable
   public static Class<?> getClassOrNull(@NonNull String name) {
      try {
         return Class.forName(name);
      } catch (ClassNotFoundException var2) {
         return null;
      }
   }

   @Nullable
   public static Method getMethodOrNull(@NonNull Class<?> clazz, @NonNull String method, @NonNull Class<?>... parameterTypes) {
      try {
         return clazz.getDeclaredMethod(method, parameterTypes);
      } catch (NoSuchMethodException var4) {
         return null;
      }
   }

   @Nullable
   public static Method getMethodOrNull(@NonNull String clazzName, @NonNull String method, @NonNull Class<?>... parameterTypes) {
      try {
         Class<?> clazz = Class.forName(clazzName);
         return clazz.getDeclaredMethod(method, parameterTypes);
      } catch (ClassNotFoundException | NoSuchMethodException var4) {
         return null;
      }
   }

   @Nullable
   public static Object invokeMethodOrNull(@NonNull Method method, @Nullable Object object, @NonNull Object... args) {
      try {
         return method.invoke(object, args);
      } catch (InvocationTargetException | IllegalAccessException var4) {
         return null;
      }
   }

   @Nullable
   public static <T> T invokeMethodOrNull(@NonNull Method method, @NonNull Class<T> clazz, @NonNull Object object, @NonNull Object... args) {
      try {
         Object value = method.invoke(object, args);
         return clazz.cast(value);
      } catch (InvocationTargetException | ClassCastException | IllegalAccessException var5) {
         return null;
      }
   }

   @Nullable
   public static Field getFieldOrNull(@NonNull Class<?> clazz, @NonNull String field) {
      try {
         return clazz.getDeclaredField(field);
      } catch (NoSuchFieldException var3) {
         return null;
      }
   }

   @Nullable
   public static Field getFieldOrNull(@NonNull String clazzName, @NonNull String field) {
      try {
         Class<?> clazz = Class.forName(clazzName);
         return clazz.getDeclaredField(field);
      } catch (ClassNotFoundException | NoSuchFieldException var3) {
         return null;
      }
   }
}
