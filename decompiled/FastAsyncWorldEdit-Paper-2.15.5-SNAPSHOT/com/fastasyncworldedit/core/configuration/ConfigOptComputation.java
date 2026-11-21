package com.fastasyncworldedit.core.configuration;

abstract sealed class ConfigOptComputation<R> permits ConfigOptComputation.THREAD_TARGET_SIZE_COMPUTATION {
   public abstract R apply(Object val);

   static final class THREAD_TARGET_SIZE_COMPUTATION extends ConfigOptComputation<Integer> {
      public Integer apply(Object val) {
         return 200 / (Integer)val;
      }
   }
}
