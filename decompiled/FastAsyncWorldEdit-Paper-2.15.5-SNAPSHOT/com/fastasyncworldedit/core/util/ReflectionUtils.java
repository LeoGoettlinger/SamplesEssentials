package com.fastasyncworldedit.core.util;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.annotation.Nonnull;
import sun.misc.Unsafe;

public class ReflectionUtils {
   private static final VarHandle REFERENCE_ARRAY_HANDLE = MethodHandles.arrayElementVarHandle(Object[].class);
   private static Unsafe UNSAFE;

   public static <T> T as(Class<T> t, Object o) {
      return t.isInstance(o) ? t.cast(o) : null;
   }

   public static <T> boolean compareAndSet(T[] array, T expectedValue, T newValue, int index) {
      return REFERENCE_ARRAY_HANDLE.compareAndSet((Object[])array, (int)index, (Object)expectedValue, (Object)newValue);
   }

   public static void setAccessibleNonFinal(Field field) {
      field.setAccessible(true);
      if (Modifier.isFinal(field.getModifiers())) {
         try {
            Field lookupField = Lookup.class.getDeclaredField("IMPL_LOOKUP");
            lookupField.setAccessible(true);
            ((Lookup)lookupField.get(null)).findSetter(Field.class, "modifiers", int.class).invokeExact((Field)field, (int)(field.getModifiers() & -17));
         } catch (Throwable var2) {
            var2.printStackTrace();
         }
      }
   }

   public static void setFailsafeFieldValue(Field field, Object target, Object value) throws IllegalAccessException {
      setAccessibleNonFinal(field);
      field.set(target, value);
   }

   public static Object getHandle(Object wrapper) {
      Method getHandle = makeMethod(wrapper.getClass(), "getHandle");
      return callMethod(getHandle, wrapper);
   }

   public static Method makeMethod(Class<?> clazz, String methodName, Class<?>... parameters) {
      try {
         return clazz.getDeclaredMethod(methodName, parameters);
      } catch (NoSuchMethodException var4) {
         return null;
      } catch (Exception var5) {
         throw new RuntimeException(var5);
      }
   }

   public static <T> T callMethod(Method method, Object instance, Object... parameters) {
      if (method == null) {
         throw new RuntimeException("No such method");
      } else {
         method.setAccessible(true);

         try {
            return (T)method.invoke(instance, parameters);
         } catch (InvocationTargetException var4) {
            throw new RuntimeException(var4.getCause());
         } catch (Exception var5) {
            throw new RuntimeException(var5);
         }
      }
   }

   public static <T extends AccessibleObject> T setAccessible(T ao) {
      ao.setAccessible(true);
      return ao;
   }

   public static <T> T getField(@Nonnull Field field, Object instance) {
      field.setAccessible(true);

      try {
         return (T)field.get(instance);
      } catch (Exception var3) {
         throw new RuntimeException(var3);
      }
   }

   public static Class<?> getClass(String name) {
      try {
         return Class.forName(name);
      } catch (ClassNotFoundException var2) {
         return null;
      }
   }

   public static <T> Class<? extends T> getClass(String name, Class<T> superClass) {
      try {
         return Class.forName(name).asSubclass(superClass);
      } catch (ClassNotFoundException | ClassCastException var3) {
         return null;
      }
   }

   public static void unsafeSet(Field field, Object base, Object value) {
      UNSAFE.putObject(base, UNSAFE.objectFieldOffset(field), value);
   }

   public static Unsafe getUnsafe() {
      return UNSAFE;
   }

   static {
      try {
         Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
         unsafeField.setAccessible(true);
         UNSAFE = (Unsafe)unsafeField.get(null);
      } catch (IllegalAccessException | NoSuchFieldException var1) {
         var1.printStackTrace();
      }
   }
}
