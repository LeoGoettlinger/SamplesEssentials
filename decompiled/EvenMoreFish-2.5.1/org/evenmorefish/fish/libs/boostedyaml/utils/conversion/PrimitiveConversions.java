package org.evenmorefish.fish.libs.boostedyaml.utils.conversion;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PrimitiveConversions {
   public static final Map<Class<?>, Class<?>> NUMERIC_PRIMITIVES = Collections.unmodifiableMap(new HashMap<Class<?>, Class<?>>() {
      {
         this.put(int.class, Integer.class);
         this.put(byte.class, Byte.class);
         this.put(short.class, Short.class);
         this.put(long.class, Long.class);
         this.put(float.class, Float.class);
         this.put(double.class, Double.class);
      }
   });
   public static final Map<Class<?>, Class<?>> PRIMITIVES_TO_OBJECTS = Collections.unmodifiableMap(new HashMap<Class<?>, Class<?>>() {
      {
         this.putAll(PrimitiveConversions.NUMERIC_PRIMITIVES);
         this.put(boolean.class, Boolean.class);
         this.put(char.class, Character.class);
      }
   });
   public static final Map<Class<?>, Class<?>> NON_NUMERIC_CONVERSIONS = Collections.unmodifiableMap(new HashMap<Class<?>, Class<?>>() {
      {
         this.put(boolean.class, Boolean.class);
         this.put(char.class, Character.class);
         this.put(Boolean.class, boolean.class);
         this.put(Character.class, char.class);
      }
   });
   public static final Set<Class<?>> NUMERIC_CLASSES = Collections.unmodifiableSet(new HashSet<Class<?>>() {
      {
         this.add(int.class);
         this.add(byte.class);
         this.add(short.class);
         this.add(long.class);
         this.add(float.class);
         this.add(double.class);
         this.add(Integer.class);
         this.add(Byte.class);
         this.add(Short.class);
         this.add(Long.class);
         this.add(Float.class);
         this.add(Double.class);
      }
   });

   public static boolean isNumber(@NotNull Class<?> clazz) {
      return NUMERIC_CLASSES.contains(clazz);
   }

   public static Object convertNumber(@NotNull Object value, @NotNull Class<?> target) {
      Number number = (Number)value;
      boolean primitive = target.isPrimitive();
      if (primitive) {
         target = NUMERIC_PRIMITIVES.get(target);
      }

      if (target == Integer.class) {
         return number.intValue();
      } else if (target == Byte.class) {
         return number.byteValue();
      } else if (target == Short.class) {
         return number.shortValue();
      } else if (target == Long.class) {
         return number.longValue();
      } else {
         return target == Float.class ? number.floatValue() : number.doubleValue();
      }
   }

   public static Optional<Integer> toInt(@Nullable Object value) {
      if (value == null) {
         return Optional.empty();
      } else if (value instanceof Number) {
         return Optional.of(((Number)value).intValue());
      } else {
         try {
            return Optional.of(Integer.valueOf(value.toString()));
         } catch (NumberFormatException var2) {
            return Optional.empty();
         }
      }
   }

   public static Optional<Byte> toByte(@Nullable Object value) {
      if (value == null) {
         return Optional.empty();
      } else if (value instanceof Number) {
         return Optional.of(((Number)value).byteValue());
      } else {
         try {
            return Optional.of(Byte.valueOf(value.toString()));
         } catch (NumberFormatException var2) {
            return Optional.empty();
         }
      }
   }

   public static Optional<Long> toLong(@Nullable Object value) {
      if (value == null) {
         return Optional.empty();
      } else if (value instanceof Number) {
         return Optional.of(((Number)value).longValue());
      } else {
         try {
            return Optional.of(Long.valueOf(value.toString()));
         } catch (NumberFormatException var2) {
            return Optional.empty();
         }
      }
   }

   public static Optional<Double> toDouble(@Nullable Object value) {
      if (value == null) {
         return Optional.empty();
      } else if (value instanceof Number) {
         return Optional.of(((Number)value).doubleValue());
      } else {
         try {
            return Optional.of(Double.valueOf(value.toString()));
         } catch (NumberFormatException var2) {
            return Optional.empty();
         }
      }
   }

   public static Optional<Float> toFloat(@Nullable Object value) {
      if (value == null) {
         return Optional.empty();
      } else if (value instanceof Number) {
         return Optional.of(((Number)value).floatValue());
      } else {
         try {
            return Optional.of(Float.valueOf(value.toString()));
         } catch (NumberFormatException var2) {
            return Optional.empty();
         }
      }
   }

   public static Optional<Short> toShort(@Nullable Object value) {
      if (value == null) {
         return Optional.empty();
      } else if (value instanceof Number) {
         return Optional.of(((Number)value).shortValue());
      } else {
         try {
            return Optional.of(Short.valueOf(value.toString()));
         } catch (NumberFormatException var2) {
            return Optional.empty();
         }
      }
   }

   public static Optional<BigInteger> toBigInt(@Nullable Object value) {
      if (value == null) {
         return Optional.empty();
      } else if (value instanceof BigInteger) {
         return Optional.of((BigInteger)value);
      } else if (value instanceof Number) {
         return Optional.of(BigInteger.valueOf(((Number)value).longValue()));
      } else {
         try {
            return Optional.of(new BigInteger(value.toString()));
         } catch (NumberFormatException var2) {
            return Optional.empty();
         }
      }
   }
}
