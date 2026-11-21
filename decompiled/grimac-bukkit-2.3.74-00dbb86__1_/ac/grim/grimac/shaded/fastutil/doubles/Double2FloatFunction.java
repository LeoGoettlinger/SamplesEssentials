package ac.grim.grimac.shaded.fastutil.doubles;

import ac.grim.grimac.shaded.fastutil.bytes.Byte2DoubleFunction;
import ac.grim.grimac.shaded.fastutil.bytes.Byte2FloatFunction;
import ac.grim.grimac.shaded.fastutil.chars.Char2DoubleFunction;
import ac.grim.grimac.shaded.fastutil.chars.Char2FloatFunction;
import ac.grim.grimac.shaded.fastutil.floats.Float2ByteFunction;
import ac.grim.grimac.shaded.fastutil.floats.Float2CharFunction;
import ac.grim.grimac.shaded.fastutil.floats.Float2DoubleFunction;
import ac.grim.grimac.shaded.fastutil.floats.Float2FloatFunction;
import ac.grim.grimac.shaded.fastutil.floats.Float2IntFunction;
import ac.grim.grimac.shaded.fastutil.floats.Float2LongFunction;
import ac.grim.grimac.shaded.fastutil.floats.Float2ObjectFunction;
import ac.grim.grimac.shaded.fastutil.floats.Float2ReferenceFunction;
import ac.grim.grimac.shaded.fastutil.floats.Float2ShortFunction;
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
import java.util.function.Function;

@FunctionalInterface
public interface Double2FloatFunction extends ac.grim.grimac.shaded.fastutil.Function<Double, Float>, java.util.function.DoubleUnaryOperator {
   @Override
   default double applyAsDouble(double operand) {
      return this.get(operand);
   }

   default float put(double key, float value) {
      throw new UnsupportedOperationException();
   }

   float get(double var1);

   default float getOrDefault(double key, float defaultValue) {
      float v;
      return (v = this.get(key)) == this.defaultReturnValue() && !this.containsKey(key) ? defaultValue : v;
   }

   default float remove(double key) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   default Float put(Double key, Float value) {
      double k = key;
      boolean containsKey = this.containsKey(k);
      float v = this.put(k, value.floatValue());
      return containsKey ? v : null;
   }

   @Deprecated
   default Float get(Object key) {
      if (key == null) {
         return null;
      } else {
         double k = (Double)key;
         float v;
         return (v = this.get(k)) == this.defaultReturnValue() && !this.containsKey(k) ? null : v;
      }
   }

   @Deprecated
   default Float getOrDefault(Object key, Float defaultValue) {
      if (key == null) {
         return defaultValue;
      } else {
         double k = (Double)key;
         float v = this.get(k);
         return v == this.defaultReturnValue() && !this.containsKey(k) ? defaultValue : v;
      }
   }

   @Deprecated
   default Float remove(Object key) {
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

   default void defaultReturnValue(float rv) {
      throw new UnsupportedOperationException();
   }

   default float defaultReturnValue() {
      return 0.0F;
   }

   @Deprecated
   @Override
   default <T> Function<T, Float> compose(Function<? super T, ? extends Double> before) {
      return ac.grim.grimac.shaded.fastutil.Function.super.compose(before);
   }

   @Deprecated
   @Override
   default <T> Function<Double, T> andThen(Function<? super Float, ? extends T> after) {
      return ac.grim.grimac.shaded.fastutil.Function.super.andThen(after);
   }

   default Double2ByteFunction andThenByte(Float2ByteFunction after) {
      return k -> after.get(this.get(k));
   }

   default Byte2FloatFunction composeByte(Byte2DoubleFunction before) {
      return k -> this.get(before.get(k));
   }

   default Double2ShortFunction andThenShort(Float2ShortFunction after) {
      return k -> after.get(this.get(k));
   }

   default Short2FloatFunction composeShort(Short2DoubleFunction before) {
      return k -> this.get(before.get(k));
   }

   default Double2IntFunction andThenInt(Float2IntFunction after) {
      return k -> after.get(this.get(k));
   }

   default Int2FloatFunction composeInt(Int2DoubleFunction before) {
      return k -> this.get(before.get(k));
   }

   default Double2LongFunction andThenLong(Float2LongFunction after) {
      return k -> after.get(this.get(k));
   }

   default Long2FloatFunction composeLong(Long2DoubleFunction before) {
      return k -> this.get(before.get(k));
   }

   default Double2CharFunction andThenChar(Float2CharFunction after) {
      return k -> after.get(this.get(k));
   }

   default Char2FloatFunction composeChar(Char2DoubleFunction before) {
      return k -> this.get(before.get(k));
   }

   default Double2FloatFunction andThenFloat(Float2FloatFunction after) {
      return k -> after.get(this.get(k));
   }

   default Float2FloatFunction composeFloat(Float2DoubleFunction before) {
      return k -> this.get(before.get(k));
   }

   default Double2DoubleFunction andThenDouble(Float2DoubleFunction after) {
      return k -> after.get(this.get(k));
   }

   default Double2FloatFunction composeDouble(Double2DoubleFunction before) {
      return k -> this.get(before.get(k));
   }

   default <T> Double2ObjectFunction<T> andThenObject(Float2ObjectFunction<? extends T> after) {
      return k -> (T)after.get(this.get(k));
   }

   default <T> Object2FloatFunction<T> composeObject(Object2DoubleFunction<? super T> before) {
      return k -> this.get(before.getDouble(k));
   }

   default <T> Double2ReferenceFunction<T> andThenReference(Float2ReferenceFunction<? extends T> after) {
      return k -> (T)after.get(this.get(k));
   }

   default <T> Reference2FloatFunction<T> composeReference(Reference2DoubleFunction<? super T> before) {
      return k -> this.get(before.getDouble(k));
   }
}
