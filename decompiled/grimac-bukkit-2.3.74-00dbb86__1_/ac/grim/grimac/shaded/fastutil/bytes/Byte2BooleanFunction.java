package ac.grim.grimac.shaded.fastutil.bytes;

import ac.grim.grimac.shaded.fastutil.SafeMath;
import ac.grim.grimac.shaded.fastutil.booleans.Boolean2ByteFunction;
import ac.grim.grimac.shaded.fastutil.booleans.Boolean2CharFunction;
import ac.grim.grimac.shaded.fastutil.booleans.Boolean2DoubleFunction;
import ac.grim.grimac.shaded.fastutil.booleans.Boolean2FloatFunction;
import ac.grim.grimac.shaded.fastutil.booleans.Boolean2IntFunction;
import ac.grim.grimac.shaded.fastutil.booleans.Boolean2LongFunction;
import ac.grim.grimac.shaded.fastutil.booleans.Boolean2ObjectFunction;
import ac.grim.grimac.shaded.fastutil.booleans.Boolean2ReferenceFunction;
import ac.grim.grimac.shaded.fastutil.booleans.Boolean2ShortFunction;
import ac.grim.grimac.shaded.fastutil.chars.Char2BooleanFunction;
import ac.grim.grimac.shaded.fastutil.chars.Char2ByteFunction;
import ac.grim.grimac.shaded.fastutil.doubles.Double2BooleanFunction;
import ac.grim.grimac.shaded.fastutil.doubles.Double2ByteFunction;
import ac.grim.grimac.shaded.fastutil.floats.Float2BooleanFunction;
import ac.grim.grimac.shaded.fastutil.floats.Float2ByteFunction;
import ac.grim.grimac.shaded.fastutil.ints.Int2BooleanFunction;
import ac.grim.grimac.shaded.fastutil.ints.Int2ByteFunction;
import ac.grim.grimac.shaded.fastutil.longs.Long2BooleanFunction;
import ac.grim.grimac.shaded.fastutil.longs.Long2ByteFunction;
import ac.grim.grimac.shaded.fastutil.objects.Object2BooleanFunction;
import ac.grim.grimac.shaded.fastutil.objects.Object2ByteFunction;
import ac.grim.grimac.shaded.fastutil.objects.Reference2BooleanFunction;
import ac.grim.grimac.shaded.fastutil.objects.Reference2ByteFunction;
import ac.grim.grimac.shaded.fastutil.shorts.Short2BooleanFunction;
import ac.grim.grimac.shaded.fastutil.shorts.Short2ByteFunction;
import java.util.function.Function;
import java.util.function.IntPredicate;

@FunctionalInterface
public interface Byte2BooleanFunction extends ac.grim.grimac.shaded.fastutil.Function<Byte, Boolean>, IntPredicate {
   @Deprecated
   @Override
   default boolean test(int operand) {
      return this.get(SafeMath.safeIntToByte(operand));
   }

   default boolean put(byte key, boolean value) {
      throw new UnsupportedOperationException();
   }

   boolean get(byte var1);

   default boolean getOrDefault(byte key, boolean defaultValue) {
      boolean v;
      return (v = this.get(key)) == this.defaultReturnValue() && !this.containsKey(key) ? defaultValue : v;
   }

   default boolean remove(byte key) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   default Boolean put(Byte key, Boolean value) {
      byte k = key;
      boolean containsKey = this.containsKey(k);
      boolean v = this.put(k, value.booleanValue());
      return containsKey ? v : null;
   }

   @Deprecated
   default Boolean get(Object key) {
      if (key == null) {
         return null;
      } else {
         byte k = (Byte)key;
         boolean v;
         return (v = this.get(k)) == this.defaultReturnValue() && !this.containsKey(k) ? null : v;
      }
   }

   @Deprecated
   default Boolean getOrDefault(Object key, Boolean defaultValue) {
      if (key == null) {
         return defaultValue;
      } else {
         byte k = (Byte)key;
         boolean v = this.get(k);
         return v == this.defaultReturnValue() && !this.containsKey(k) ? defaultValue : v;
      }
   }

   @Deprecated
   default Boolean remove(Object key) {
      if (key == null) {
         return null;
      } else {
         byte k = (Byte)key;
         return this.containsKey(k) ? this.remove(k) : null;
      }
   }

   default boolean containsKey(byte key) {
      return true;
   }

   @Deprecated
   @Override
   default boolean containsKey(Object key) {
      return key == null ? false : this.containsKey(((Byte)key).byteValue());
   }

   default void defaultReturnValue(boolean rv) {
      throw new UnsupportedOperationException();
   }

   default boolean defaultReturnValue() {
      return false;
   }

   @Deprecated
   @Override
   default <T> Function<T, Boolean> compose(Function<? super T, ? extends Byte> before) {
      return ac.grim.grimac.shaded.fastutil.Function.super.compose(before);
   }

   @Deprecated
   @Override
   default <T> Function<Byte, T> andThen(Function<? super Boolean, ? extends T> after) {
      return ac.grim.grimac.shaded.fastutil.Function.super.andThen(after);
   }

   default Byte2ByteFunction andThenByte(Boolean2ByteFunction after) {
      return k -> after.get(this.get(k));
   }

   default Byte2BooleanFunction composeByte(Byte2ByteFunction before) {
      return k -> this.get(before.get(k));
   }

   default Byte2ShortFunction andThenShort(Boolean2ShortFunction after) {
      return k -> after.get(this.get(k));
   }

   default Short2BooleanFunction composeShort(Short2ByteFunction before) {
      return k -> this.get(before.get(k));
   }

   default Byte2IntFunction andThenInt(Boolean2IntFunction after) {
      return k -> after.get(this.get(k));
   }

   default Int2BooleanFunction composeInt(Int2ByteFunction before) {
      return k -> this.get(before.get(k));
   }

   default Byte2LongFunction andThenLong(Boolean2LongFunction after) {
      return k -> after.get(this.get(k));
   }

   default Long2BooleanFunction composeLong(Long2ByteFunction before) {
      return k -> this.get(before.get(k));
   }

   default Byte2CharFunction andThenChar(Boolean2CharFunction after) {
      return k -> after.get(this.get(k));
   }

   default Char2BooleanFunction composeChar(Char2ByteFunction before) {
      return k -> this.get(before.get(k));
   }

   default Byte2FloatFunction andThenFloat(Boolean2FloatFunction after) {
      return k -> after.get(this.get(k));
   }

   default Float2BooleanFunction composeFloat(Float2ByteFunction before) {
      return k -> this.get(before.get(k));
   }

   default Byte2DoubleFunction andThenDouble(Boolean2DoubleFunction after) {
      return k -> after.get(this.get(k));
   }

   default Double2BooleanFunction composeDouble(Double2ByteFunction before) {
      return k -> this.get(before.get(k));
   }

   default <T> Byte2ObjectFunction<T> andThenObject(Boolean2ObjectFunction<? extends T> after) {
      return k -> (T)after.get(this.get(k));
   }

   default <T> Object2BooleanFunction<T> composeObject(Object2ByteFunction<? super T> before) {
      return k -> this.get(before.getByte(k));
   }

   default <T> Byte2ReferenceFunction<T> andThenReference(Boolean2ReferenceFunction<? extends T> after) {
      return k -> (T)after.get(this.get(k));
   }

   default <T> Reference2BooleanFunction<T> composeReference(Reference2ByteFunction<? super T> before) {
      return k -> this.get(before.getByte(k));
   }
}
