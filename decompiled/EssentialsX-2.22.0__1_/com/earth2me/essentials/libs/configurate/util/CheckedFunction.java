package com.earth2me.essentials.libs.configurate.util;

import java.util.Objects;
import java.util.function.Function;

@FunctionalInterface
public interface CheckedFunction<I, O, E extends Exception> {
   O apply(I one) throws E;

   static <I, O> CheckedFunction<I, O, RuntimeException> from(Function<I, O> func) {
      return Objects.requireNonNull(func, "func")::apply;
   }
}
