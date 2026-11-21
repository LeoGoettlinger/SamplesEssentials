package ac.grim.grimac.shaded.fastutil.doubles;

import ac.grim.grimac.shaded.fastutil.bytes.Byte2DoubleFunction;
import ac.grim.grimac.shaded.fastutil.bytes.Byte2ShortFunction;
import ac.grim.grimac.shaded.fastutil.chars.Char2DoubleFunction;
import ac.grim.grimac.shaded.fastutil.chars.Char2ShortFunction;
import ac.grim.grimac.shaded.fastutil.floats.Float2DoubleFunction;
import ac.grim.grimac.shaded.fastutil.floats.Float2ShortFunction;
import ac.grim.grimac.shaded.fastutil.ints.Int2DoubleFunction;
import ac.grim.grimac.shaded.fastutil.ints.Int2ShortFunction;
import ac.grim.grimac.shaded.fastutil.longs.Long2DoubleFunction;
import ac.grim.grimac.shaded.fastutil.longs.Long2ShortFunction;
import ac.grim.grimac.shaded.fastutil.objects.Object2DoubleFunction;
import ac.grim.grimac.shaded.fastutil.objects.Object2ShortFunction;
import ac.grim.grimac.shaded.fastutil.objects.Reference2DoubleFunction;
import ac.grim.grimac.shaded.fastutil.objects.Reference2ShortFunction;
import ac.grim.grimac.shaded.fastutil.shorts.Short2ByteFunction;
import ac.grim.grimac.shaded.fastutil.shorts.Short2CharFunction;
import ac.grim.grimac.shaded.fastutil.shorts.Short2DoubleFunction;
import ac.grim.grimac.shaded.fastutil.shorts.Short2FloatFunction;
import ac.grim.grimac.shaded.fastutil.shorts.Short2IntFunction;
import ac.grim.grimac.shaded.fastutil.shorts.Short2LongFunction;
import ac.grim.grimac.shaded.fastutil.shorts.Short2ObjectFunction;
import ac.grim.grimac.shaded.fastutil.shorts.Short2ReferenceFunction;
import ac.grim.grimac.shaded.fastutil.shorts.Short2ShortFunction;
import java.util.function.DoubleToIntFunction;
import java.util.function.Function;

@FunctionalInterface
public interface Double2ShortFunction extends ac.grim.grimac.shaded.fastutil.Function<Double, Short>, DoubleToIntFunction {
   @Override
   default int applyAsInt(double operand) {
      return this.get(operand);
   }

   default short put(double key, short value) {
      throw new UnsupportedOperationException();
   }

   short get(double var1);

   default short getOrDefault(double key, short defaultValue) {
      short v;
      return (v = this.get(key)) == this.defaultReturnValue() && !this.containsKey(key) ? defaultValue : v;
   }

   default short remove(double key) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   default Short put(Double key, Short value) {
      double k = key;
      boolean containsKey = this.containsKey(k);
      short v = this.put(k, value.shortValue());
      return containsKey ? v : null;
   }

   @Deprecated
   default Short get(Object key) {
      if (key == null) {
         return null;
      } else {
         double k = (Double)key;
         short v;
         return (v = this.get(k)) == this.defaultReturnValue() && !this.containsKey(k) ? null : v;
      }
   }

   @Deprecated
   default Short getOrDefault(Object key, Short defaultValue) {
      if (key == null) {
         return defaultValue;
      } else {
         double k = (Double)key;
         short v = this.get(k);
         return v == this.defaultReturnValue() && !this.containsKey(k) ? defaultValue : v;
      }
   }

   @Deprecated
   default Short remove(Object key) {
      if (key == null) {
         return null;
      } else {
         double k = (Double)key;
         return this.containsKey(k) ? this.remove(k) : null;
      }
   }

   default boolean containsKey(double key) {
      return true;
   }

   @Deprecated
   @Override
   default boolean containsKey(Object key) {
      return key == null ? false : this.containsKey(((Double)key).doubleValue());
   }

   default void defaultReturnValue(short rv) {
      throw new UnsupportedOperationException();
   }

   default short defaultReturnValue() {
      return 0;
   }

   @Deprecated
   @Override
   default <T> Function<T, Short> compose(Function<? super T, ? extends Double> before) {
      return ac.grim.grimac.shaded.fastutil.Function.super.compose(before);
   }

   @Deprecated
   @Override
   default <T> Function<Double, T> andThen(Function<? super Short, ? extends T> after) {
      return ac.grim.grimac.shaded.fastutil.Function.super.andThen(after);
   }

   default Double2ByteFunction andThenByte(Short2ByteFunction after) {
      return k -> after.get(this.get(k));
   }

   default Byte2ShortFunction composeByte(Byte2DoubleFunction before) {
      return k -> this.get(before.get(k));
   }

   default Double2ShortFunction andThenShort(Short2ShortFunction after) {
      return k -> after.get(this.get(k));
   }

   default Short2ShortFunction composeShort(Short2DoubleFunction before) {
      return k -> this.get(before.get(k));
   }

   default Double2IntFunction andThenInt(Short2IntFunction after) {
      return k -> after.get(this.get(k));
   }

   default Int2ShortFunction composeInt(Int2DoubleFunction before) {
      return k -> this.get(before.get(k));
   }

   default Double2LongFunction andThenLong(Short2LongFunction after) {
      return k -> after.get(this.get(k));
   }

   default Long2ShortFunction composeLong(Long2DoubleFunction before) {
      return k -> this.get(before.get(k));
   }

   default Double2CharFunction andThenChar(Short2CharFunction after) {
      return k -> after.get(this.get(k));
   }

   default Char2ShortFunction composeChar(Char2DoubleFunction before) {
      return k -> this.get(before.get(k));
   }

   default Double2FloatFunction andThenFloat(Short2FloatFunction after) {
      return k -> after.get(this.get(k));
   }

   default Float2ShortFunction composeFloat(Float2DoubleFunction before) {
      return k -> this.get(before.get(k));
   }

   default Double2DoubleFunction andThenDouble(Short2DoubleFunction after) {
      return k -> after.get(this.get(k));
   }

   default Double2ShortFunction composeDouble(Double2DoubleFunction before) {
      return k -> this.get(before.get(k));
   }

   default <T> Double2ObjectFunction<T> andThenObject(Short2ObjectFunction<? extends T> after) {
      return k -> (T)after.get(this.get(k));
   }

   default <T> Object2ShortFunction<T> composeObject(Object2DoubleFunction<? super T> before) {
      return k -> this.get(before.getDouble(k));
   }

   default <T> Double2ReferenceFunction<T> andThenReference(Short2ReferenceFunction<? extends T> after) {
      return k -> (T)after.get(this.get(k));
   }

   default <T> Reference2ShortFunction<T> composeReference(Reference2DoubleFunction<? super T> before) {
      return k -> this.get(before.getDouble(k));
   }
}
