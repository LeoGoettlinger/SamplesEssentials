package com.earth2me.essentials.libs.configurate.serialize;

import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.Nullable;
import com.earth2me.essentials.libs.configurate.util.CheckedConsumer;
import com.earth2me.essentials.libs.configurate.util.Types;
import com.earth2me.essentials.libs.geantyref.GenericTypeReflector;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Array;
import java.lang.reflect.Type;

abstract class ArraySerializer<T> extends AbstractListChildSerializer<T> {
   @Override
   protected AnnotatedType elementType(final AnnotatedType containerType) throws SerializationException {
      AnnotatedType componentType = GenericTypeReflector.getArrayComponentType(containerType);
      if (componentType == null) {
         throw new SerializationException(containerType, "Must be array type");
      } else {
         return componentType;
      }
   }

   static final class Booleans extends ArraySerializer<boolean[]> {
      static final Class<boolean[]> TYPE = boolean[].class;

      protected boolean[] createNew(final int length, final AnnotatedType elementType) {
         return new boolean[length];
      }

      protected void forEachElement(final boolean[] collection, final CheckedConsumer<Object, SerializationException> action) throws SerializationException {
         for (boolean b : collection) {
            action.accept(b);
         }
      }

      protected void deserializeSingle(final int index, final boolean[] collection, @Nullable final Object deserialized) throws SerializationException {
         collection[index] = deserialized == null ? false : Scalars.BOOLEAN.deserialize(deserialized);
      }
   }

   static final class Bytes extends ArraySerializer<byte[]> {
      static final Class<byte[]> TYPE = byte[].class;

      protected byte[] createNew(final int length, final AnnotatedType elementType) {
         return new byte[length];
      }

      protected void forEachElement(final byte[] collection, final CheckedConsumer<Object, SerializationException> action) throws SerializationException {
         for (byte b : collection) {
            action.accept(b);
         }
      }

      protected void deserializeSingle(final int index, final byte[] collection, @Nullable final Object deserialized) throws SerializationException {
         collection[index] = deserialized == null ? 0 : Scalars.INTEGER.deserialize(deserialized).byteValue();
      }
   }

   static final class Chars extends ArraySerializer<char[]> {
      static final Class<char[]> TYPE = char[].class;

      protected char[] createNew(final int length, final AnnotatedType elementType) {
         return new char[length];
      }

      protected void forEachElement(final char[] collection, final CheckedConsumer<Object, SerializationException> action) throws SerializationException {
         for (char b : collection) {
            action.accept(b);
         }
      }

      protected void deserializeSingle(final int index, final char[] collection, @Nullable final Object deserialized) throws SerializationException {
         collection[index] = deserialized == null ? 0 : Scalars.CHAR.deserialize(deserialized);
      }
   }

   static final class Doubles extends ArraySerializer<double[]> {
      static final Class<double[]> TYPE = double[].class;

      protected double[] createNew(final int length, final AnnotatedType elementType) {
         return new double[length];
      }

      protected void forEachElement(final double[] collection, final CheckedConsumer<Object, SerializationException> action) throws SerializationException {
         for (double b : collection) {
            action.accept(b);
         }
      }

      protected void deserializeSingle(final int index, final double[] collection, @Nullable final Object deserialized) throws SerializationException {
         collection[index] = deserialized == null ? 0.0 : Scalars.DOUBLE.deserialize(deserialized);
      }
   }

   static final class Floats extends ArraySerializer<float[]> {
      static final Class<float[]> TYPE = float[].class;

      protected float[] createNew(final int length, final AnnotatedType elementType) {
         return new float[length];
      }

      protected void forEachElement(final float[] collection, final CheckedConsumer<Object, SerializationException> action) throws SerializationException {
         for (float b : collection) {
            action.accept(b);
         }
      }

      protected void deserializeSingle(final int index, final float[] collection, @Nullable final Object deserialized) throws SerializationException {
         collection[index] = deserialized == null ? 0.0F : Scalars.FLOAT.deserialize(deserialized);
      }
   }

   static final class Ints extends ArraySerializer<int[]> {
      static final Class<int[]> TYPE = int[].class;

      protected int[] createNew(final int length, final AnnotatedType elementType) {
         return new int[length];
      }

      protected void forEachElement(final int[] collection, final CheckedConsumer<Object, SerializationException> action) throws SerializationException {
         for (int b : collection) {
            action.accept(b);
         }
      }

      protected void deserializeSingle(final int index, final int[] collection, @Nullable final Object deserialized) throws SerializationException {
         collection[index] = deserialized == null ? 0 : Scalars.INTEGER.deserialize(deserialized);
      }
   }

   static final class Longs extends ArraySerializer<long[]> {
      static final Class<long[]> TYPE = long[].class;

      protected long[] createNew(final int length, final AnnotatedType elementType) {
         return new long[length];
      }

      protected void forEachElement(final long[] collection, final CheckedConsumer<Object, SerializationException> action) throws SerializationException {
         for (long b : collection) {
            action.accept(b);
         }
      }

      protected void deserializeSingle(final int index, final long[] collection, @Nullable final Object deserialized) throws SerializationException {
         collection[index] = deserialized == null ? 0L : Scalars.LONG.deserialize(deserialized);
      }
   }

   static final class Objects extends ArraySerializer<Object[]> {
      public static boolean accepts(final Type token) {
         if (!Types.isArray(token)) {
            return false;
         } else {
            Type componentType = GenericTypeReflector.getArrayComponentType(token);
            return componentType.equals(GenericTypeReflector.box(componentType));
         }
      }

      protected Object[] createNew(final int length, final AnnotatedType elementType) {
         return (Object[])Array.newInstance(GenericTypeReflector.erase(elementType.getType()), length);
      }

      protected void forEachElement(final Object[] collection, final CheckedConsumer<Object, SerializationException> action) throws SerializationException {
         for (Object o : collection) {
            action.accept(o);
         }
      }

      protected void deserializeSingle(final int index, final Object[] collection, @Nullable final Object deserialized) {
         collection[index] = deserialized;
      }
   }

   static final class Shorts extends ArraySerializer<short[]> {
      static final Class<short[]> TYPE = short[].class;

      protected short[] createNew(final int length, final AnnotatedType elementType) {
         return new short[length];
      }

      protected void forEachElement(final short[] collection, final CheckedConsumer<Object, SerializationException> action) throws SerializationException {
         for (short b : collection) {
            action.accept(b);
         }
      }

      protected void deserializeSingle(final int index, final short[] collection, @Nullable final Object deserialized) throws SerializationException {
         collection[index] = deserialized == null ? 0 : Scalars.INTEGER.deserialize(deserialized).shortValue();
      }
   }
}
