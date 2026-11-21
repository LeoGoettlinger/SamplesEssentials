package com.artillexstudios.axtrade.libs.axapi.utils;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class Optionals {
   public static <T> void ifPresent(@Nullable T object, @NonNull Consumer<T> consumer) {
      if (object != null) {
         consumer.accept(object);
      }
   }

   public static <T, Z> T applyIfPresent(@Nullable Z value, @Nullable T object, @NonNull BiFunction<T, Z, T> function) {
      return value == null ? object : function.apply(object, value);
   }

   public static <T> T applyIf(@Nullable T object, boolean test, @NonNull UnaryOperator<T> operator) {
      return test ? operator.apply(object) : object;
   }

   @SafeVarargs
   public static <T> T orElse(Supplier<T> one, Supplier<T>... others) {
      T value = one.get();
      if (value != null) {
         return value;
      } else {
         for (Supplier<T> other : others) {
            value = other.get();
            if (value != null) {
               return value;
            }
         }

         return null;
      }
   }

   @SafeVarargs
   @Nullable
   public static <T> T orElse(@Nullable T one, @Nullable T... others) {
      if (one != null) {
         return one;
      } else if (others == null) {
         return null;
      } else {
         for (T other : others) {
            if (other != null) {
               return other;
            }
         }

         return null;
      }
   }
}
