package com.magmaguy.elitemobs.items.customitems;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

final class CacheRegenerationLifecycle {
   private final ReentrantLock lifecycleLock = new ReentrantLock(true);
   private long generation;

   <T> CacheRegenerationLifecycle.Attempt<T> beginIf(BooleanSupplier allowed, Supplier<? extends Iterable<T>> snapshotSource) {
      this.lifecycleLock.lock();

      List<T> snapshot;
      try {
         if (allowed.getAsBoolean()) {
            snapshot = new ArrayList<>();

            for (T value : snapshotSource.get()) {
               snapshot.add(value);
            }

            return new CacheRegenerationLifecycle.Attempt<>(++this.generation, List.copyOf(snapshot));
         }

         snapshot = null;
      } finally {
         this.lifecycleLock.unlock();
      }

      return snapshot;
   }

   boolean runIfCurrent(long expectedGeneration, Runnable action) {
      this.lifecycleLock.lock();

      boolean var4;
      try {
         if (this.generation == expectedGeneration) {
            action.run();
            return true;
         }

         var4 = false;
      } finally {
         this.lifecycleLock.unlock();
      }

      return var4;
   }

   boolean isCurrent(long expectedGeneration) {
      this.lifecycleLock.lock();

      boolean var3;
      try {
         var3 = this.generation == expectedGeneration;
      } finally {
         this.lifecycleLock.unlock();
      }

      return var3;
   }

   void cancel() {
      this.lifecycleLock.lock();

      try {
         this.generation++;
      } finally {
         this.lifecycleLock.unlock();
      }
   }

   record Attempt<T>(long generation, List<T> snapshot) {
   }
}
