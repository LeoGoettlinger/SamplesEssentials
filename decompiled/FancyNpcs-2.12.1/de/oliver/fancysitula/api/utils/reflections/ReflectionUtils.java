package de.oliver.fancysitula.api.utils.reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import sun.misc.Unsafe;

public class ReflectionUtils {
   private static Unsafe getUnsafe() throws Exception {
      Field field = Unsafe.class.getDeclaredField("theUnsafe");
      field.setAccessible(true);
      return (Unsafe)field.get(null);
   }

   public static <T> T createUnsafeInstance(Class<T> clazz) throws Exception {
      Unsafe unsafe = getUnsafe();
      return (T)unsafe.allocateInstance(clazz);
   }

   public static <T> T createInstance(Class<T> clazz) throws Exception {
      Constructor<T> constructor = clazz.getDeclaredConstructor();
      constructor.setAccessible(true);
      return constructor.newInstance();
   }

   public static Object getField(Object object, String fieldName) throws Exception {
      Field field = object.getClass().getDeclaredField(fieldName);
      field.setAccessible(true);
      return field.get(object);
   }

   public static void setField(Object object, String fieldName, Object value) throws Exception {
      Field field = object.getClass().getDeclaredField(fieldName);
      field.setAccessible(true);
      field.set(object, value);
   }

   public static void setFinalField(Object target, String fieldName, Object value) throws Exception {
      Field field = target.getClass().getDeclaredField(fieldName);
      field.setAccessible(true);
      Unsafe unsafe = getUnsafe();
      long offset = unsafe.objectFieldOffset(field);
      if (field.getType() == int.class) {
         unsafe.putInt(target, offset, (Integer)value);
      } else if (field.getType() == long.class) {
         unsafe.putLong(target, offset, (Long)value);
      } else if (field.getType() == double.class) {
         unsafe.putDouble(target, offset, (Double)value);
      } else if (field.getType() == float.class) {
         unsafe.putFloat(target, offset, (Float)value);
      } else if (field.getType() == boolean.class) {
         unsafe.putBoolean(target, offset, (Boolean)value);
      } else if (field.getType() == byte.class) {
         unsafe.putByte(target, offset, (Byte)value);
      } else if (field.getType() == short.class) {
         unsafe.putShort(target, offset, (Short)value);
      } else if (field.getType() == char.class) {
         unsafe.putChar(target, offset, (Character)value);
      } else {
         unsafe.putObject(target, offset, value);
      }
   }

   public static <T> T getStaticField(Class<?> clazz, String fieldName) throws NoSuchFieldException, IllegalAccessException {
      Field field = clazz.getDeclaredField(fieldName);
      field.setAccessible(true);
      return (T)field.get(null);
   }
}
