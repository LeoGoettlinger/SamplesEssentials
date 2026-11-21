package su.nightexpress.nightcore.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class Reflex {
   public static boolean classExists(@NonNull String path) {
      return findClass(path).isPresent();
   }

   @Deprecated
   @Nullable
   public static Class<?> getClass(@NonNull String path, @NonNull String name) {
      return getClass(path + "." + name);
   }

   @Deprecated
   @Nullable
   public static Class<?> getInnerClass(@NonNull String path, @NonNull String name) {
      return getClass(path + "$" + name);
   }

   @Deprecated
   @Nullable
   public static Class<?> getNMSClass(@NonNull String path, @NonNull String realName) {
      return getNMSClass(path, realName, null);
   }

   @Deprecated
   @Nullable
   public static Class<?> getNMSClass(@NonNull String path, @NonNull String realName, @Nullable String obfName) {
      Class<?> byRealName = getClass(path + "." + realName, false);
      if (byRealName != null) {
         return byRealName;
      } else {
         return obfName != null ? getClass(path + "." + obfName, false) : null;
      }
   }

   @Deprecated
   private static Class<?> getClass(@NonNull String path) {
      return getClass(path, true);
   }

   @Deprecated
   private static Class<?> getClass(@NonNull String path, boolean printError) {
      try {
         return Class.forName(path);
      } catch (ClassNotFoundException var3) {
         if (printError) {
            var3.printStackTrace();
         }

         return null;
      }
   }

   @NonNull
   public static Class<?> safeClass(@NonNull String path, @NonNull String name, @NonNull String altName) {
      return findClass(path, name, altName)
         .orElseThrow(() -> new IllegalStateException("Could not load classes: '" + name + "' and '" + altName + "' in '" + path + "'"));
   }

   @NonNull
   public static Class<?> safeClass(@NonNull String path, @NonNull String name) {
      return findClass(path, name).orElseThrow(() -> new IllegalStateException("Could not load class: '" + name + "' in '" + path + "'"));
   }

   @NonNull
   public static Class<?> safeInnerClass(@NonNull String path, @NonNull String name) {
      return findInnerClass(path, name).orElseThrow(() -> new IllegalStateException("Could not load inner class: '" + name + "' in '" + path + "'"));
   }

   @NonNull
   public static Class<?> safeClass(@NonNull String path) {
      return findClass(path).orElseThrow(() -> new IllegalStateException("Could not load class: '" + path + "'"));
   }

   @NonNull
   public static Optional<Class<?>> findClass(@NonNull String path, @NonNull String name, @NonNull String altName) {
      return findClass(path, name).or(() -> findClass(path, altName));
   }

   @NonNull
   public static Optional<Class<?>> findClass(@NonNull String path, @NonNull String name) {
      return findClass(path + "." + name);
   }

   @NonNull
   public static Optional<Class<?>> findInnerClass(@NonNull String path, @NonNull String name) {
      return findClass(path + "$" + name);
   }

   @NonNull
   public static Optional<Class<?>> findClass(@NonNull String path) {
      try {
         return Optional.of(Class.forName(path));
      } catch (ClassNotFoundException var2) {
         return Optional.empty();
      }
   }

   public static Constructor<?> getConstructor(@NonNull Class<?> source, Class<?>... types) {
      try {
         Constructor<?> constructor = source.getDeclaredConstructor(types);
         constructor.setAccessible(true);
         return constructor;
      } catch (ReflectiveOperationException var3) {
         var3.printStackTrace();
         return null;
      }
   }

   public static Object invokeConstructor(@NonNull Constructor<?> constructor, Object... obj) {
      try {
         return constructor.newInstance(obj);
      } catch (ReflectiveOperationException var3) {
         var3.printStackTrace();
         return null;
      }
   }

   @NonNull
   public static <T> List<T> getStaticFields(@NonNull Class<?> source, @NonNull Class<T> type, boolean includeParent) {
      List<T> list = new ArrayList<>();

      for (Field field : getFields(source, includeParent)) {
         if (Modifier.isStatic(field.getModifiers()) && type.isAssignableFrom(field.getType()) && field.trySetAccessible()) {
            try {
               list.add(type.cast(field.get(null)));
            } catch (IllegalAccessException | IllegalArgumentException var7) {
               var7.printStackTrace();
            }
         }
      }

      return list;
   }

   @NonNull
   public static List<Field> getFields(@NonNull Class<?> source) {
      return getFields(source, true);
   }

   @NonNull
   public static List<Field> getFields(@NonNull Class<?> source, boolean includeParent) {
      List<Field> result = new ArrayList<>();

      for (Class<?> lookupClass = source; lookupClass != null && lookupClass != Object.class; lookupClass = lookupClass.getSuperclass()) {
         if (!result.isEmpty()) {
            result.addAll(0, Arrays.asList(lookupClass.getDeclaredFields()));
         } else {
            Collections.addAll(result, lookupClass.getDeclaredFields());
         }

         if (!includeParent) {
            break;
         }
      }

      return result;
   }

   public static Field getField(@NonNull Class<?> source, @NonNull String name) {
      try {
         return source.getDeclaredField(name);
      } catch (NoSuchFieldException var4) {
         Class<?> superClass = source.getSuperclass();
         return superClass == null ? null : getField(superClass, name);
      }
   }

   public static Object getFieldValue(@NonNull Object source, @NonNull String realName, @NonNull String obfName) {
      Object byName = getFieldValue(source, realName);
      return byName == null ? getFieldValue(source, obfName) : byName;
   }

   public static Object getFieldValue(@NonNull Object source, @NonNull String name) {
      try {
         Class<?> clazz = source instanceof Class ? (Class)source : source.getClass();
         Field field = getField(clazz, name);
         if (field == null) {
            return null;
         } else {
            field.setAccessible(true);
            return field.get(source);
         }
      } catch (IllegalAccessException var4) {
         var4.printStackTrace();
         return null;
      }
   }

   public static boolean setFieldValue(@NonNull Object source, @NonNull String name, @Nullable Object value) {
      try {
         boolean isStatic = source instanceof Class;
         Class<?> clazz = isStatic ? (Class)source : source.getClass();
         Field field = getField(clazz, name);
         if (field == null) {
            return false;
         } else {
            field.setAccessible(true);
            field.set(isStatic ? null : source, value);
            return true;
         }
      } catch (IllegalAccessException var6) {
         var6.printStackTrace();
         return false;
      }
   }

   @Deprecated
   public static Method getMethod(@NonNull Class<?> source, @NonNull String realName, @NonNull String obfName, Class<?>... params) {
      Method byName = getMethod(source, realName, params);
      return byName == null ? getMethod(source, obfName, params) : byName;
   }

   @Deprecated
   public static Method getMethod(@NonNull Class<?> source, @NonNull String name, Class<?>... params) {
      try {
         return source.getDeclaredMethod(name, params);
      } catch (NoSuchMethodException var5) {
         Class<?> superClass = source.getSuperclass();
         return superClass == null ? null : getMethod(superClass, name);
      }
   }

   @NonNull
   public static Method safeMethod(@NonNull Class<?> source, @NonNull String name, @NonNull String altName, Class<?>... params) {
      return findMethod(source, name, altName, params)
         .orElseThrow(() -> new IllegalStateException("Could not find methods: '" + name + "' and '" + altName + "' in '" + source.getName() + "'"));
   }

   @NonNull
   public static Method safeMethod(@NonNull Class<?> source, @NonNull String name, Class<?>... params) {
      return findMethod(source, name, params)
         .orElseThrow(() -> new IllegalStateException("Could not find method: '" + name + "' in '" + source.getName() + "'"));
   }

   @NonNull
   public static Optional<Method> findMethod(@NonNull Class<?> source, @NonNull String name, @NonNull String altName, Class<?>... params) {
      return findMethod(source, name, params).or(() -> findMethod(source, altName, params));
   }

   @NonNull
   public static Optional<Method> findMethod(@NonNull Class<?> source, @NonNull String name, Class<?>... params) {
      try {
         return Optional.of(source.getDeclaredMethod(name, params));
      } catch (NoSuchMethodException var5) {
         Class<?> superClass = source.getSuperclass();
         return superClass == null ? Optional.empty() : findMethod(superClass, name);
      }
   }

   @NonNull
   public static Optional<Object> safeInvoke(@NonNull Method method, @Nullable Object by, Object... param) {
      try {
         method.setAccessible(true);
         return Optional.ofNullable(method.invoke(by, param));
      } catch (ReflectiveOperationException var4) {
         var4.printStackTrace();
         return Optional.empty();
      }
   }

   @Nullable
   public static Object invokeMethod(@NonNull Method method, @Nullable Object by, Object... param) {
      return safeInvoke(method, by, param).orElse(null);
   }
}
