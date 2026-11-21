package ac.grim.grimac.shaded.fastutil.floats;

import ac.grim.grimac.shaded.fastutil.SafeMath;
import ac.grim.grimac.shaded.fastutil.bytes.Byte2DoubleFunction;
import ac.grim.grimac.shaded.fastutil.bytes.Byte2FloatFunction;
import ac.grim.grimac.shaded.fastutil.chars.Char2DoubleFunction;
import ac.grim.grimac.shaded.fastutil.chars.Char2FloatFunction;
import ac.grim.grimac.shaded.fastutil.doubles.Double2ByteFunction;
import ac.grim.grimac.shaded.fastutil.doubles.Double2CharFunction;
import ac.grim.grimac.shaded.fastutil.doubles.Double2DoubleFunction;
import ac.grim.grimac.shaded.fastutil.doubles.Double2FloatFunction;
import ac.grim.grimac.shaded.fastutil.doubles.Double2IntFunction;
import ac.grim.grimac.shaded.fastutil.doubles.Double2LongFunction;
import ac.grim.grimac.shaded.fastutil.doubles.Double2ObjectFunction;
import ac.grim.grimac.shaded.fastutil.doubles.Double2ReferenceFunction;
import ac.grim.grimac.shaded.fastutil.doubles.Double2ShortFunction;
import ac.grim.grimac.shaded.fastutil.ints.Int2DoubleFunction;
import ac.grim.grimac.shaded.fastutil.ints.Int2FloatFunction;
import ac.grim.grimac.shaded.fastutil.longs.Long2DoubleFunction;
import ac.grim.grimac.shaded.fastutil.longs.Long2FloatFunction;
import ac.grim.grimac.shaded.fastutil.objects.Object2DoubleFunction;
import ac.grim.grimac.shaded.fastutil.objects.Object2FloatFunction;
import ac.grim.grimac.shaded.fastutil.objects.Reference2DoubleFunction;
import ac.grim.grimac.shaded.fastutil.objects.Reference2FloatFunction;
import ac.grim.grimac.shaded.fastutil.shorts.Short2DoubleFunction;
import ac.grim.grimac.shaded.fastutil.shorts.Short2FloatFunction;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;

@FunctionalInterface
public interface Float2DoubleFunction extends ac.grim.grimac.shaded.fastutil.Function<Float, Double>, DoubleUnaryOperator {
   @Deprecated
   @Override
   default double applyAsDouble(double operand) {
      return this.get(SafeMath.safeDoubleToFloat(operand));
   }

   default double put(float key, double value) {
      throw new UnsupportedOperationException();
   }

   double get(float var1);

   default double getOrDefault(float key, double defaultValue) {
      double v;
      return (v = this.get(key)) == this.defaultReturnValue() && !this.containsKey(key) ? defaultValue : v;
   }

   default double remove(float key) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   default Double put(Float key, Double value) {
      float k = key;
      boolean containsKey = this.containsKey(k);
      double v = this.put(k, value.doubleValue());
      return containsKey ? v : null;
   }

   @Deprecated
   default Double get(Object key) {
      if (key == null) {
         return null;
      } else {
         float k = (Float)key;
         double v;
         return (v = this.get(k)) == this.defaultReturnValue() && !this.containsKey(k) ? null : v;
      }
   }

   @Deprecated
   default Double getOrDefault(Object key, Double defaultValue) {
      if (key == null) {
         return defaultValue;
      } else {
         float k = (Float)key;
         double v = this.get(k);
         return v == this.defaultReturnValue() && !this.containsKey(k) ? defaultValue : v;
      }
   }

   @Deprecated
   default Double remove(Object key) {
      if (key == null) {
         return null;
      } else {
         float k = (Float)key;
         return this.containsKey(k) ? this.remove(k) : null;
      }
   }

   default boolean containsKey(float key) {
      return true;
   }

   @Deprecated
   @Override
   default boolean containsKey(Object key) {
      return key == null ? false : this.containsKey(((Float)key).floatValue());
   }

   default void defaultReturnValue(double rv) {
      throw new UnsupportedOperationException();
   }

   default double defaultReturnValue() {
      return 0.0;
   }

   @Deprecated
   @Override
   default <T> Function<T, Double> compose(Function<? super T, ? extends Float> before) {
      return ac.grim.grimac.shaded.fastutil.Function.super.compose(before);
   }

   @Deprecated
   @Override
   default <T> Function<Float, T> andThen(Function<? super Double, ? extends T> after) {
      return ac.grim.grimac.shaded.fastutil.Function.super.andThen(after);
   }

   default Float2ByteFunction andThenByte(Double2ByteFunction after) {
      return k -> after.get(this.get(k));
   }

   default Byte2DoubleFunction composeByte(Byte2FloatFunction before) {
      return k -> this.get(before.get(k));
   }

   default Float2ShortFunction andThenShort(Double2ShortFunction after) {
      return k -> after.get(this.get(k));
   }

   default Short2DoubleFunction composeShort(Short2FloatFunction before) {
      return k -> this.get(before.get(k));
   }

   default Float2IntFunction andThenInt(Double2IntFunction after) {
      return k -> after.get(this.get(k));
   }

   default Int2DoubleFunction composeInt(Int2FloatFunction before) {
      return k -> this.get(before.get(k));
   }

   default Float2LongFunction andThenLong(Double2LongFunction after) {
      return k -> after.get(this.get(k));
   }

   default Long2DoubleFunction composeLong(Long2FloatFunction before) {
      return k -> this.get(before.get(k));
   }

   default Float2CharFunction andThenChar(Double2CharFunction after) {
      return k -> after.get(this.get(k));
   }

   default Char2DoubleFunction composeChar(Char2FloatFunction before) {
      return k -> this.get(before.get(k));
   }

   default Float2FloatFunction andThenFloat(Double2FloatFunction after) {
      return k -> after.get(this.get(k));
   }

   default Float2DoubleFunction composeFloat(Float2FloatFunction before) {
      return k -> this.get(before.get(k));
   }

   default Float2DoubleFunction andThenDouble(Double2DoubleFunction after) {
      return k -> after.get(this.get(k));
   }

   default Double2DoubleFunction composeDouble(Double2FloatFunction before) {
      return k -> this.get(before.get(k));
   }

   default <T> Float2ObjectFunction<T> andThenObject(Double2ObjectFunction<? extends T> after) {
      return k -> (T)after.get(this.get(k));
   }

   default <T> Object2DoubleFunction<T> composeObject(Object2FloatFunction<? super T> before) {
      return k -> this.get(before.getFloat(k));
   }

   default <T> Float2ReferenceFunction<T> andThenReference(Double2ReferenceFunction<? extends T> after) {
      return k -> (T)after.get(this.get(k));
   }

   default <T> Reference2DoubleFunction<T> composeReference(Reference2FloatFunction<? super T> before) {
      return k -> this.get(before.getFloat(k));
   }
}
