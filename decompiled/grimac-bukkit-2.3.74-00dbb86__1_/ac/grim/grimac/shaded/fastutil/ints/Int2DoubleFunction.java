package ac.grim.grimac.shaded.fastutil.ints;

import ac.grim.grimac.shaded.fastutil.bytes.Byte2DoubleFunction;
import ac.grim.grimac.shaded.fastutil.bytes.Byte2IntFunction;
import ac.grim.grimac.shaded.fastutil.chars.Char2DoubleFunction;
import ac.grim.grimac.shaded.fastutil.chars.Char2IntFunction;
import ac.grim.grimac.shaded.fastutil.doubles.Double2ByteFunction;
import ac.grim.grimac.shaded.fastutil.doubles.Double2CharFunction;
import ac.grim.grimac.shaded.fastutil.doubles.Double2DoubleFunction;
import ac.grim.grimac.shaded.fastutil.doubles.Double2FloatFunction;
import ac.grim.grimac.shaded.fastutil.doubles.Double2IntFunction;
import ac.grim.grimac.shaded.fastutil.doubles.Double2LongFunction;
import ac.grim.grimac.shaded.fastutil.doubles.Double2ObjectFunction;
import ac.grim.grimac.shaded.fastutil.doubles.Double2ReferenceFunction;
import ac.grim.grimac.shaded.fastutil.doubles.Double2ShortFunction;
import ac.grim.grimac.shaded.fastutil.floats.Float2DoubleFunction;
import ac.grim.grimac.shaded.fastutil.floats.Float2IntFunction;
import ac.grim.grimac.shaded.fastutil.longs.Long2DoubleFunction;
import ac.grim.grimac.shaded.fastutil.longs.Long2IntFunction;
import ac.grim.grimac.shaded.fastutil.objects.Object2DoubleFunction;
import ac.grim.grimac.shaded.fastutil.objects.Object2IntFunction;
import ac.grim.grimac.shaded.fastutil.objects.Reference2DoubleFunction;
import ac.grim.grimac.shaded.fastutil.objects.Reference2IntFunction;
import ac.grim.grimac.shaded.fastutil.shorts.Short2DoubleFunction;
import ac.grim.grimac.shaded.fastutil.shorts.Short2IntFunction;
import java.util.function.Function;
import java.util.function.IntToDoubleFunction;

@FunctionalInterface
public interface Int2DoubleFunction extends ac.grim.grimac.shaded.fastutil.Function<Integer, Double>, IntToDoubleFunction {
   @Override
   default double applyAsDouble(int operand) {
      return this.get(operand);
   }

   default double put(int key, double value) {
      throw new UnsupportedOperationException();
   }

   double get(int var1);

   default double getOrDefault(int key, double defaultValue) {
      double v;
      return (v = this.get(key)) == this.defaultReturnValue() && !this.containsKey(key) ? defaultValue : v;
   }

   default double remove(int key) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   default Double put(Integer key, Double value) {
      int k = key;
      boolean containsKey = this.containsKey(k);
      double v = this.put(k, value.doubleValue());
      return containsKey ? v : null;
   }

   @Deprecated
   default Double get(Object key) {
      if (key == null) {
         return null;
      } else {
         int k = (Integer)key;
         double v;
         return (v = this.get(k)) == this.defaultReturnValue() && !this.containsKey(k) ? null : v;
      }
   }

   @Deprecated
   default Double getOrDefault(Object key, Double defaultValue) {
      if (key == null) {
         return defaultValue;
      } else {
         int k = (Integer)key;
         double v = this.get(k);
         return v == this.defaultReturnValue() && !this.containsKey(k) ? defaultValue : v;
      }
   }

   @Deprecated
   default Double remove(Object key) {
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

   default void defaultReturnValue(double rv) {
      throw new UnsupportedOperationException();
   }

   default double defaultReturnValue() {
      return 0.0;
   }

   @Deprecated
   @Override
   default <T> Function<T, Double> compose(Function<? super T, ? extends Integer> before) {
      return ac.grim.grimac.shaded.fastutil.Function.super.compose(before);
   }

   @Deprecated
   @Override
   default <T> Function<Integer, T> andThen(Function<? super Double, ? extends T> after) {
      return ac.grim.grimac.shaded.fastutil.Function.super.andThen(after);
   }

   default Int2ByteFunction andThenByte(Double2ByteFunction after) {
      return k -> after.get(this.get(k));
   }

   default Byte2DoubleFunction composeByte(Byte2IntFunction before) {
      return k -> this.get(before.get(k));
   }

   default Int2ShortFunction andThenShort(Double2ShortFunction after) {
      return k -> after.get(this.get(k));
   }

   default Short2DoubleFunction composeShort(Short2IntFunction before) {
      return k -> this.get(before.get(k));
   }

   default Int2IntFunction andThenInt(Double2IntFunction after) {
      return k -> after.get(this.get(k));
   }

   default Int2DoubleFunction composeInt(Int2IntFunction before) {
      return k -> this.get(before.get(k));
   }

   default Int2LongFunction andThenLong(Double2LongFunction after) {
      return k -> after.get(this.get(k));
   }

   default Long2DoubleFunction composeLong(Long2IntFunction before) {
      return k -> this.get(before.get(k));
   }

   default Int2CharFunction andThenChar(Double2CharFunction after) {
      return k -> after.get(this.get(k));
   }

   default Char2DoubleFunction composeChar(Char2IntFunction before) {
      return k -> this.get(before.get(k));
   }

   default Int2FloatFunction andThenFloat(Double2FloatFunction after) {
      return k -> after.get(this.get(k));
   }

   default Float2DoubleFunction composeFloat(Float2IntFunction before) {
      return k -> this.get(before.get(k));
   }

   default Int2DoubleFunction andThenDouble(Double2DoubleFunction after) {
      return k -> after.get(this.get(k));
   }

   default Double2DoubleFunction composeDouble(Double2IntFunction before) {
      return k -> this.get(before.get(k));
   }

   default <T> Int2ObjectFunction<T> andThenObject(Double2ObjectFunction<? extends T> after) {
      return k -> (T)after.get(this.get(k));
   }

   default <T> Object2DoubleFunction<T> composeObject(Object2IntFunction<? super T> before) {
      return k -> this.get(before.getInt(k));
   }

   default <T> Int2ReferenceFunction<T> andThenReference(Double2ReferenceFunction<? extends T> after) {
      return k -> (T)after.get(this.get(k));
   }

   default <T> Reference2DoubleFunction<T> composeReference(Reference2IntFunction<? super T> before) {
      return k -> this.get(before.getInt(k));
   }
}
