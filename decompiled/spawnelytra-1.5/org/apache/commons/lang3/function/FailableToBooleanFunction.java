package org.apache.commons.lang3.function;

@FunctionalInterface
public interface FailableToBooleanFunction<T, E extends Throwable> {
   FailableToBooleanFunction NOP = t -> false;

   static <T, E extends Throwable> FailableToBooleanFunction<T, E> nop() {
      return NOP;
   }

   boolean applyAsBoolean(T var1) throws E;
}
