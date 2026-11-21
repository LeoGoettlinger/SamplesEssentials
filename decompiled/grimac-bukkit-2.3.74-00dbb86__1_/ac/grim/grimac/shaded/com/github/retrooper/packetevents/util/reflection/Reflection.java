package ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.reflection;

import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class Reflection {
   public static Field[] getFields(final Class<?> cls) {
      if (cls == null) {
         return new Field[0];
      } else {
         Field[] declaredFields = cls.getDeclaredFields();

         for (Field f : declaredFields) {
            try {
               f.setAccessible(true);
            } catch (Throwable var7) {
            }
         }

         return declaredFields;
      }
   }

   public static Field getField(final Class<?> cls, final String name) {
      if (cls == null) {
         return null;
      } else {
         try {
            Field field = cls.getDeclaredField(name);
            field.setAccessible(true);
            return field;
         } catch (NoSuchFieldException var3) {
            if (cls.getSuperclass() != null) {
               return getField(cls.getSuperclass(), name);
            }
         } catch (Throwable var4) {
         }

         return null;
      }
   }

   public static Field getField(final Class<?> cls, final Class<?> dataType, final int index) {
      if (dataType != null && cls != null) {
         int currentIndex = 0;

         for (Field f : getFields(cls)) {
            if (dataType.isAssignableFrom(f.getType()) && currentIndex++ == index) {
               return f;
            }
         }

         return cls.getSuperclass() != null ? getField(cls.getSuperclass(), dataType, index) : null;
      } else {
         return null;
      }
   }

   public static Field getField(final Class<?> cls, final Class<?> dataType, final int index, boolean ignoreStatic) {
      if (dataType != null && cls != null) {
         int currentIndex = 0;

         for (Field f : getFields(cls)) {
            if (dataType.isAssignableFrom(f.getType()) && (!ignoreStatic || !Modifier.isStatic(f.getModifiers())) && currentIndex++ == index) {
               return f;
            }
         }

         return cls.getSuperclass() != null ? getField(cls.getSuperclass(), dataType, index) : null;
      } else {
         return null;
      }
   }

   public static Field getField(final Class<?> cls, final int index) {
      if (cls == null) {
         return null;
      } else {
         try {
            return getFields(cls)[index];
         } catch (Exception var3) {
            return cls.getSuperclass() != null ? getFields(cls.getSuperclass())[index] : null;
         }
      }
   }

   public static Method getMethod(final Class<?> cls, final String name, final Class<?>... params) {
      if (cls == null) {
         return null;
      } else {
         try {
            Method m = cls.getDeclaredMethod(name, params);
            m.setAccessible(true);
            return m;
         } catch (NoSuchMethodException var6) {
            try {
               Method mx = cls.getMethod(name, params);
               mx.setAccessible(true);
               return mx;
            } catch (NoSuchMethodException var5) {
               return cls.getSuperclass() != null ? getMethod(cls.getSuperclass(), name, params) : null;
            }
         }
      }
   }

   public static Method getMethod(final Class<?> cls, final int index, final Class<?>... params) {
      if (cls == null) {
         return null;
      } else {
         int currentIndex = 0;

         for (Method m : cls.getDeclaredMethods()) {
            if ((params == null || Arrays.equals((Object[])m.getParameterTypes(), (Object[])params)) && index == currentIndex++) {
               m.setAccessible(true);
               return m;
            }
         }

         return cls.getSuperclass() != null ? getMethod(cls.getSuperclass(), index, params) : null;
      }
   }

   public static Method getMethod(final Class<?> cls, final Class<?> returning, final int index, final Class<?>... params) {
      if (cls == null) {
         return null;
      } else {
         int currentIndex = 0;

         for (Method m : cls.getDeclaredMethods()) {
            if (Arrays.equals((Object[])m.getParameterTypes(), (Object[])params)
               && (returning == null || m.getReturnType().equals(returning))
               && index == currentIndex++) {
               m.setAccessible(true);
               return m;
            }
         }

         return cls.getSuperclass() != null ? getMethod(cls.getSuperclass(), null, index, params) : null;
      }
   }

   public static Method getMethodExact(final Class<?> cls, final String name, Class<?> returning, Class<?>... params) {
      if (cls == null) {
         return null;
      } else {
         for (Method m : cls.getDeclaredMethods()) {
            if (m.getName().equals(name)
               && Arrays.equals((Object[])m.getParameterTypes(), (Object[])params)
               && (returning == null || m.getReturnType().equals(returning))) {
               m.setAccessible(true);
               return m;
            }
         }

         return cls.getSuperclass() != null ? getMethodExact(cls.getSuperclass(), name, null, params) : null;
      }
   }

   public static Method getMethod(final Class<?> cls, final String name, final int index) {
      if (cls == null) {
         return null;
      } else {
         int currentIndex = 0;

         for (Method m : cls.getDeclaredMethods()) {
            if (m.getName().equals(name) && index == currentIndex++) {
               m.setAccessible(true);
               return m;
            }
         }

         return cls.getSuperclass() != null ? getMethod(cls.getSuperclass(), name, index) : null;
      }
   }

   public static Method getMethod(final Class<?> cls, final Class<?> returning, final int index) {
      if (cls == null) {
         return null;
      } else {
         int currentIndex = 0;

         for (Method m : cls.getDeclaredMethods()) {
            if ((returning == null || m.getReturnType().equals(returning)) && index == currentIndex++) {
               m.setAccessible(true);
               return m;
            }
         }

         return cls.getSuperclass() != null ? getMethod(cls.getSuperclass(), returning, index) : null;
      }
   }

   public static Method getMethodCheckContainsString(final Class<?> cls, final String nameContainsThisStr, final Class<?> returning) {
      if (cls == null) {
         return null;
      } else {
         for (Method m : cls.getDeclaredMethods()) {
            if (m.getName().contains(nameContainsThisStr) && (returning == null || m.getReturnType().equals(returning))) {
               m.setAccessible(true);
               return m;
            }
         }

         return cls.getSuperclass() != null ? getMethodCheckContainsString(cls.getSuperclass(), nameContainsThisStr, returning) : null;
      }
   }

   public static List<Method> getMethods(final Class<?> cls, final String name, final Class<?> returning) {
      if (cls == null) {
         return null;
      } else {
         List<Method> methods = new ArrayList<>();

         for (Method m : cls.getDeclaredMethods()) {
            if (m.getName().equals(name) && (returning == null || m.getReturnType().equals(returning))) {
               m.setAccessible(true);
               methods.add(m);
            }
         }

         if (cls.getSuperclass() != null) {
            methods.addAll(getMethods(cls.getSuperclass(), name, returning));
         }

         return methods;
      }
   }

   @Nullable
   public static Class<?> getClassByNameWithoutException(final String name) {
      try {
         return Class.forName(name);
      } catch (ClassNotFoundException var2) {
         return null;
      }
   }

   public static Constructor<?> getConstructor(final Class<?> cls, final int index) {
      if (cls == null) {
         return null;
      } else {
         Constructor<?> constructor = cls.getDeclaredConstructors()[index];
         constructor.setAccessible(true);
         return constructor;
      }
   }

   public static Constructor<?> getConstructor(final Class<?> cls, final Class<?>... params) {
      if (cls == null) {
         return null;
      } else {
         try {
            Constructor<?> c = cls.getDeclaredConstructor(params);
            c.setAccessible(true);
            return c;
         } catch (NoSuchMethodException var5) {
            try {
               Constructor<?> cx = cls.getConstructor(params);
               cx.setAccessible(true);
               return cx;
            } catch (NoSuchMethodException var4) {
               return null;
            }
         }
      }
   }
}
