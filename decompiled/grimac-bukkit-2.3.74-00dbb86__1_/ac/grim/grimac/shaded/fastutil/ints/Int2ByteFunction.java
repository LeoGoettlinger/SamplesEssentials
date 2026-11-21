package ac.grim.grimac.shaded.fastutil.ints;

import ac.grim.grimac.shaded.fastutil.bytes.Byte2ByteFunction;
import ac.grim.grimac.shaded.fastutil.bytes.Byte2CharFunction;
import ac.grim.grimac.shaded.fastutil.bytes.Byte2DoubleFunction;
import ac.grim.grimac.shaded.fastutil.bytes.Byte2FloatFunction;
import ac.grim.grimac.shaded.fastutil.bytes.Byte2IntFunction;
import ac.grim.grimac.shaded.fastutil.bytes.Byte2LongFunction;
import ac.grim.grimac.shaded.fastutil.bytes.Byte2ObjectFunction;
import ac.grim.grimac.shaded.fastutil.bytes.Byte2ReferenceFunction;
import ac.grim.grimac.shaded.fastutil.bytes.Byte2ShortFunction;
import ac.grim.grimac.shaded.fastutil.chars.Char2ByteFunction;
import ac.grim.grimac.shaded.fastutil.chars.Char2IntFunction;
import ac.grim.grimac.shaded.fastutil.doubles.Double2ByteFunction;
import ac.grim.grimac.shaded.fastutil.doubles.Double2IntFunction;
import ac.grim.grimac.shaded.fastutil.floats.Float2ByteFunction;
import ac.grim.grimac.shaded.fastutil.floats.Float2IntFunction;
import ac.grim.grimac.shaded.fastutil.longs.Long2ByteFunction;
import ac.grim.grimac.shaded.fastutil.longs.Long2IntFunction;
import ac.grim.grimac.shaded.fastutil.objects.Object2ByteFunction;
import ac.grim.grimac.shaded.fastutil.objects.Object2IntFunction;
import ac.grim.grimac.shaded.fastutil.objects.Reference2ByteFunction;
import ac.grim.grimac.shaded.fastutil.objects.Reference2IntFunction;
import ac.grim.grimac.shaded.fastutil.shorts.Short2ByteFunction;
import ac.grim.grimac.shaded.fastutil.shorts.Short2IntFunction;
import java.util.function.Function;

@FunctionalInterface
public interface Int2ByteFunction extends ac.grim.grimac.shaded.fastutil.Function<Integer, Byte>, java.util.function.IntUnaryOperator {
   @Override
   default int applyAsInt(int operand) {
      return this.get(operand);
   }

   default byte put(int key, byte value) {
      throw new UnsupportedOperationException();
   }

   byte get(int var1);

   default byte getOrDefault(int key, byte defaultValue) {
      byte v;
      return (v = this.get(key)) == this.defaultReturnValue() && !this.containsKey(key) ? defaultValue : v;
   }

   default byte remove(int key) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   default Byte put(Integer key, Byte value) {
      int k = key;
      boolean containsKey = this.containsKey(k);
      byte v = this.put(k, value.byteValue());
      return containsKey ? v : null;
   }

   @Deprecated
   default Byte get(Object key) {
      if (key == null) {
         return null;
      } else {
         int k = (Integer)key;
         byte v;
         return (v = this.get(k)) == this.defaultReturnValue() && !this.containsKey(k) ? null : v;
      }
   }

   @Deprecated
   default Byte getOrDefault(Object key, Byte defaultValue) {
      if (key == null) {
         return defaultValue;
      } else {
         int k = (Integer)key;
         byte v = this.get(k);
         return v == this.defaultReturnValue() && !this.containsKey(k) ? defaultValue : v;
      }
   }

   @Deprecated
   default Byte remove(Object key) {
      if (key == null) {
         return null;
      } else {
         int k = (Integer)key;
         return this.containsKey(k) ? this.remove(k) : null;
      }
   }

   default boolean containsKey(int key) {
      return true;
   }

   @Deprecated
   @Override
   default boolean containsKey(Object key) {
      return key == null ? false : this.containsKey(((Integer)key).intValue());
   }

   default void defaultReturnValue(byte rv) {
      throw new UnsupportedOperationException();
   }

   default byte defaultReturnValue() {
      return 0;
   }

   @Deprecated
   @Override
   default <T> Function<T, Byte> compose(Function<? super T, ? extends Integer> before) {
      return ac.grim.grimac.shaded.fastutil.Function.super.compose(before);
   }

   @Deprecated
   @Override
   default <T> Function<Integer, T> andThen(Function<? super Byte, ? extends T> after) {
      return ac.grim.grimac.shaded.fastutil.Function.super.andThen(after);
   }

   default Int2ByteFunction andThenByte(Byte2ByteFunction after) {
      return k -> after.get(this.get(k));
   }

   default Byte2ByteFunction composeByte(Byte2IntFunction before) {
      return k -> this.get(before.get(k));
   }

   default Int2ShortFunction andThenShort(Byte2ShortFunction after) {
      return k -> after.get(this.get(k));
   }

   default Short2ByteFunction composeShort(Short2IntFunction before) {
      return k -> this.get(before.get(k));
   }

   default Int2IntFunction andThenInt(Byte2IntFunction after) {
      return k -> after.get(this.get(k));
   }

   default Int2ByteFunction composeInt(Int2IntFunction before) {
      return k -> this.get(before.get(k));
   }

   default Int2LongFunction andThenLong(Byte2LongFunction after) {
      return k -> after.get(this.get(k));
   }

   default Long2ByteFunction composeLong(Long2IntFunction before) {
      return k -> this.get(before.get(k));
   }

   default Int2CharFunction andThenChar(Byte2CharFunction after) {
      return k -> after.get(this.get(k));
   }

   default Char2ByteFunction composeChar(Char2IntFunction before) {
      return k -> this.get(before.get(k));
   }

   default Int2FloatFunction andThenFloat(Byte2FloatFunction after) {
      return k -> after.get(this.get(k));
   }

   default Float2ByteFunction composeFloat(Float2IntFunction before) {
      return k -> this.get(before.get(k));
   }

   default Int2DoubleFunction andThenDouble(Byte2DoubleFunction after) {
      return k -> after.get(this.get(k));
   }

   default Double2ByteFunction composeDouble(Double2IntFunction before) {
      return k -> this.get(before.get(k));
   }

   default <T> Int2ObjectFunction<T> andThenObject(Byte2ObjectFunction<? extends T> after) {
      return k -> (T)after.get(this.get(k));
   }

   default <T> Object2ByteFunction<T> composeObject(Object2IntFunction<? super T> before) {
      return k -> this.get(before.getInt(k));
   }

   default <T> Int2ReferenceFunction<T> andThenReference(Byte2ReferenceFunction<? extends T> after) {
      return k -> (T)after.get(this.get(k));
   }

   default <T> Reference2ByteFunction<T> composeReference(Reference2IntFunction<? super T> before) {
      return k -> this.get(before.getInt(k));
   }
}
