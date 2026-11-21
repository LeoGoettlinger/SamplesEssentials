package com.github.retrooper.packetevents.util.reflection;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ReflectionObject implements ReflectionObjectReader, ReflectionObjectWriter {
   private static final Map<Class<?>, Map<Class<?>, Field[]>> FIELD_CACHE = new ConcurrentHashMap<>();
   private static final Field[] EMPTY_FIELD_ARRAY = new Field[0];
   protected final Object object;
   private final Class<?> clazz;

   public ReflectionObject() {
      this.object = null;
      this.clazz = null;
   }

   public ReflectionObject(Object object) {
      this.object = object;
      this.clazz = object.getClass();
   }

   public ReflectionObject(Object object, Class<?> clazz) {
      this.object = object;
      this.clazz = clazz;
   }

   @Override
   public boolean readBoolean(int index) {
      return this.read(index, boolean.class);
   }

   @Override
   public byte readByte(int index) {
      return this.read(index, byte.class);
   }

   @Override
   public short readShort(int index) {
      return this.read(index, short.class);
   }

   @Override
   public int readInt(int index) {
      return this.read(index, int.class);
   }

   @Override
   public long readLong(int index) {
      return this.read(index, long.class);
   }

   @Override
   public float readFloat(int index) {
      return this.read(index, float.class);
   }

   @Override
   public double readDouble(int index) {
      return this.read(index, double.class);
   }

   @Override
   public boolean[] readBooleanArray(int index) {
      return this.read(index, boolean[].class);
   }

   @Override
   public byte[] readByteArray(int index) {
      return this.read(index, byte[].class);
   }

   @Override
   public short[] readShortArray(int index) {
      return this.read(index, short[].class);
   }

   @Override
   public int[] readIntArray(int index) {
      return this.read(index, int[].class);
   }

   @Override
   public long[] readLongArray(int index) {
      return this.read(index, long[].class);
   }

   @Override
   public float[] readFloatArray(int index) {
      return this.read(index, float[].class);
   }

   @Override
   public double[] readDoubleArray(int index) {
      return this.read(index, double[].class);
   }

   @Override
   public String[] readStringArray(int index) {
      return this.read(index, String[].class);
   }

   @Override
   public String readString(int index) {
      return this.read(index, String.class);
   }

   @Override
   public Object readAnyObject(int index) {
      try {
         Field f = this.clazz.getDeclaredFields()[index];
         if (!f.isAccessible()) {
            f.setAccessible(true);
         }

         try {
            return f.get(this.object);
         } catch (NullPointerException | ArrayIndexOutOfBoundsException | IllegalAccessException var4) {
            var4.printStackTrace();
            return null;
         }
      } catch (ArrayIndexOutOfBoundsException var5) {
         throw new IllegalStateException("PacketEvents failed to find any field indexed " + index + " in the " + this.clazz.getSimpleName() + " class!");
      }
   }

   @Override
   public <T> T readObject(int index, Class<? extends T> type) {
      return this.read(index, type);
   }

   @Override
   public <T> T[] readObjectArray(int index, Class<? extends T> type) {
      return (T[])((Object[])this.read(0, Array.newInstance(type, 0).getClass()));
   }

   @Override
   public Enum<?> readEnumConstant(int index, Class<? extends Enum<?>> type) {
      return this.read(index, type);
   }

   public <T> T read(int index, Class<? extends T> type) {
      try {
         Field field = this.getField(type, index);
         return (T)field.get(this.object);
      } catch (NullPointerException | ArrayIndexOutOfBoundsException | IllegalAccessException var4) {
         throw new IllegalStateException(
            "PacketEvents failed to find a " + type.getSimpleName() + " indexed " + index + " by its type in the " + this.clazz.getName() + " class!"
         );
      }
   }

   @Override
   public void writeBoolean(int index, boolean value) {
      this.write(boolean.class, index, value);
   }

   @Override
   public void writeByte(int index, byte value) {
      this.write(byte.class, index, value);
   }

   @Override
   public void writeShort(int index, short value) {
      this.write(short.class, index, value);
   }

   @Override
   public void writeInt(int index, int value) {
      this.write(int.class, index, value);
   }

   @Override
   public void writeLong(int index, long value) {
      this.write(long.class, index, value);
   }

   @Override
   public void writeFloat(int index, float value) {
      this.write(float.class, index, value);
   }

   @Override
   public void writeDouble(int index, double value) {
      this.write(double.class, index, value);
   }

   @Override
   public void writeString(int index, String value) {
      this.write(String.class, index, value);
   }

   @Override
   public void writeObject(int index, Object value) {
      this.write(value.getClass(), index, value);
   }

   @Override
   public void writeBooleanArray(int index, boolean[] array) {
      this.write(boolean[].class, index, array);
   }

   @Override
   public void writeByteArray(int index, byte[] value) {
      this.write(byte[].class, index, value);
   }

   @Override
   public void writeShortArray(int index, short[] value) {
      this.write(short[].class, index, value);
   }

   @Override
   public void writeIntArray(int index, int[] value) {
      this.write(int[].class, index, value);
   }

   @Override
   public void writeLongArray(int index, long[] value) {
      this.write(long[].class, index, value);
   }

   @Override
   public void writeFloatArray(int index, float[] value) {
      this.write(float[].class, index, value);
   }

   @Override
   public void writeDoubleArray(int index, double[] value) {
      this.write(double[].class, index, value);
   }

   @Override
   public void writeStringArray(int index, String[] value) {
      this.write(String[].class, index, value);
   }

   @Override
   public void writeAnyObject(int index, Object value) {
      try {
         Field f = this.clazz.getDeclaredFields()[index];
         f.set(this.object, value);
      } catch (Exception var4) {
         throw new IllegalStateException("PacketEvents failed to find any field indexed " + index + " in the " + this.clazz.getSimpleName() + " class!");
      }
   }

   @Override
   public void writeEnumConstant(int index, Enum<?> enumConstant) {
      try {
         this.write(enumConstant.getClass(), index, enumConstant);
      } catch (IllegalStateException var4) {
         this.write(enumConstant.getDeclaringClass(), index, enumConstant);
      }
   }

   public void write(Class<?> type, int index, Object value) throws IllegalStateException {
      Field field = this.getField(type, index);
      if (field == null) {
         throw new IllegalStateException(
            "PacketEvents failed to find a " + type.getSimpleName() + " indexed " + index + " by its type in the " + this.clazz.getName() + " class!"
         );
      } else {
         try {
            field.set(this.object, value);
         } catch (NullPointerException | IllegalAccessException var6) {
            var6.printStackTrace();
         }
      }
   }

   public <T> List<T> readList(int index) {
      return this.read(index, List.class);
   }

   public void writeList(int index, List<?> list) {
      this.write(List.class, index, list);
   }

   private Field getField(Class<?> type, int index) {
      Map<Class<?>, Field[]> cached = FIELD_CACHE.computeIfAbsent(this.clazz, k -> new ConcurrentHashMap<>());
      Field[] fields = cached.computeIfAbsent(type, typeClass -> this.getFields((Class<?>)typeClass, this.clazz.getDeclaredFields()));
      if (fields.length >= index + 1) {
         return fields[index];
      } else {
         throw new IllegalStateException(
            "PacketEvents failed to find a " + type.getSimpleName() + " indexed " + index + " by its type in the " + this.clazz.getName() + " class!"
         );
      }
   }

   private Field[] getFields(Class<?> type, Field[] fields) {
      List<Field> ret = new ArrayList<>();

      for (Field field : fields) {
         if (field.getType().equals(type)) {
            if (!field.isAccessible()) {
               field.setAccessible(true);
            }

            ret.add(field);
         }
      }

      return ret.toArray(EMPTY_FIELD_ARRAY);
   }
}
