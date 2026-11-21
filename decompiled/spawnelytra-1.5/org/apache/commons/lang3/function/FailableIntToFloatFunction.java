package org.apache.commons.lang3.function;

@FunctionalInterface
public interface FailableIntToFloatFunction<E extends Throwable> {
   FailableIntToFloatFunction NOP = t -> 0.0F;

   static <E extends Throwable> FailableIntToFloatFunction<E> nop() {
      return NOP;
   }

   float applyAsFloat(int var1) throws E;
}
