package com.sk89q.worldedit.util.concurrency;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public final class LazyReference<T> {
   private Object value;

   public static <T> LazyReference<T> from(Supplier<T> valueComputation) {
      return new LazyReference<>(valueComputation);
   }

   public static <T> LazyReference<T> computed(T value) {
      return new LazyReference<>(value);
   }

   private LazyReference(Supplier<T> valueComputation) {
      this.value = new LazyReference.RefInfo<>(valueComputation);
   }

   private LazyReference(T value) {
      this.value = value;
   }

   public T getValue() {
      Object v = this.value;
      if (!(v instanceof LazyReference.RefInfo<T> refInfo)) {
         return (T)v;
      } else {
         refInfo.lock.lock();

         Object var3;
         try {
            v = this.value;
            if (v instanceof LazyReference.RefInfo) {
               this.value = v = refInfo.valueComputation.get();
               return (T)v;
            }

            var3 = v;
         } finally {
            refInfo.lock.unlock();
         }

         return (T)var3;
      }
   }

   private static final class RefInfo<T> {
      private final Lock lock = new ReentrantLock();
      private final Supplier<T> valueComputation;

      private RefInfo(Supplier<T> valueComputation) {
         this.valueComputation = valueComputation;
      }
   }
}
