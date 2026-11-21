package ac.grim.grimac.shaded.fastutil.shorts;

import ac.grim.grimac.shaded.fastutil.SafeMath;
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
import ac.grim.grimac.shaded.fastutil.chars.Char2ShortFunction;
import ac.grim.grimac.shaded.fastutil.doubles.Double2ByteFunction;
import ac.grim.grimac.shaded.fastutil.doubles.Double2ShortFunction;
import ac.grim.grimac.shaded.fastutil.floats.Float2ByteFunction;
import ac.grim.grimac.shaded.fastutil.floats.Float2ShortFunction;
import ac.grim.grimac.shaded.fastutil.ints.Int2ByteFunction;
import ac.grim.grimac.shaded.fastutil.ints.Int2ShortFunction;
import ac.grim.grimac.shaded.fastutil.longs.Long2ByteFunction;
import ac.grim.grimac.shaded.fastutil.longs.Long2ShortFunction;
import ac.grim.grimac.shaded.fastutil.objects.Object2ByteFunction;
import ac.grim.grimac.shaded.fastutil.objects.Object2ShortFunction;
import ac.grim.grimac.shaded.fastutil.objects.Reference2ByteFunction;
import ac.grim.grimac.shaded.fastutil.objects.Reference2ShortFunction;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;

@FunctionalInterface
public interface Short2ByteFunction extends ac.grim.grimac.shaded.fastutil.Function<Short, Byte>, IntUnaryOperator {
   @Deprecated
   @Override
   default int applyAsInt(int operand) {
      return this.get(SafeMath.safeIntToShort(operand));
   }

   default byte put(short key, byte value) {
      throw new UnsupportedOperationException();
   }

   byte get(short var1);

   default byte getOrDefault(short key, byte defaultValue) {
      byte v;
      return (v = this.get(key)) == this.defaultReturnValue() && !this.containsKey(key) ? defaultValue : v;
   }

   default byte remove(short key) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   default Byte put(Short key, Byte value) {
      short k = key;
      boolean containsKey = this.containsKey(k);
      byte v = this.put(k, value.byteValue());
      return containsKey ? v : null;
   }

   @Deprecated
   default Byte get(Object key) {
      if (key == null) {
         return null;
      } else {
         short k = (Short)key;
         byte v;
         return (v = this.get(k)) == this.defaultReturnValue() && !this.containsKey(k) ? null : v;
      }
   }

   @Deprecated
   default Byte getOrDefault(Object key, Byte defaultValue) {
      if (key == null) {
         return defaultValue;
      } else {
         short k = (Short)key;
         byte v = this.get(k);
         return v == this.defaultReturnValue() && !this.containsKey(k) ? defaultValue : v;
      }
   }

   @Deprecated
   default Byte remove(Object key) {
      if (key == null) {
         return null;
      } else {
         short k = (Short)key;
         return this.containsKey(k) ? this.remove(k) : null;
      }
   }

   default boolean containsKey(short key) {
      return true;
   }

   @Deprecated
   @Override
   default boolean containsKey(Object key) {
      return key == null ? false : this.containsKey(((Short)key).shortValue());
   }

   default void defaultReturnValue(byte rv) {
      throw new UnsupportedOperationException();
   }

   default byte defaultReturnValue() {
      return 0;
   }

   @Deprecated
   @Override
   default <T> Function<T, Byte> compose(Function<? super T, ? extends Short> before) {
      return ac.grim.grimac.shaded.fastutil.Function.super.compose(before);
   }

   @Deprecated
   @Override
   default <T> Function<Short, T> andThen(Function<? super Byte, ? extends T> after) {
      return ac.grim.grimac.shaded.fastutil.Function.super.andThen(after);
   }

   default Short2ByteFunction andThenByte(Byte2ByteFunction after) {
      return k -> after.get(this.get(k));
   }

   default Byte2ByteFunction composeByte(Byte2ShortFunction before) {
      return k -> this.get(before.get(k));
   }

   default Short2ShortFunction andThenShort(Byte2ShortFunction after) {
      return k -> after.get(this.get(k));
   }

   default Short2ByteFunction composeShort(Short2ShortFunction before) {
      return k -> this.get(before.get(k));
   }

   default Short2IntFunction andThenInt(Byte2IntFunction after) {
      return k -> after.get(this.get(k));
   }

   default Int2ByteFunction composeInt(Int2ShortFunction before) {
      return k -> this.get(before.get(k));
   }

   default Short2LongFunction andThenLong(Byte2LongFunction after) {
      return k -> after.get(this.get(k));
   }

   default Long2ByteFunction composeLong(Long2ShortFunction before) {
      return k -> this.get(before.get(k));
   }

   default Short2CharFunction andThenChar(Byte2CharFunction after) {
      return k -> after.get(this.get(k));
   }

   default Char2ByteFunction composeChar(Char2ShortFunction before) {
      return k -> this.get(before.get(k));
   }

   default Short2FloatFunction andThenFloat(Byte2FloatFunction after) {
      return k -> after.get(this.get(k));
   }

   default Float2ByteFunction composeFloat(Float2ShortFunction before) {
      return k -> this.get(before.get(k));
   }

   default Short2DoubleFunction andThenDouble(Byte2DoubleFunction after) {
      return k -> after.get(this.get(k));
   }

   default Double2ByteFunction composeDouble(Double2ShortFunction before) {
      return k -> this.get(before.get(k));
   }

   default <T> Short2ObjectFunction<T> andThenObject(Byte2ObjectFunction<? extends T> after) {
      return k -> (T)after.get(this.get(k));
   }

   default <T> Object2ByteFunction<T> composeObject(Object2ShortFunction<? super T> before) {
      return k -> this.get(before.getShort(k));
   }

   default <T> Short2ReferenceFunction<T> andThenReference(Byte2ReferenceFunction<? extends T> after) {
      return k -> (T)after.get(this.get(k));
   }

   default <T> Reference2ByteFunction<T> composeReference(Reference2ShortFunction<? super T> before) {
      return k -> this.get(before.getShort(k));
   }
}
