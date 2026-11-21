package org.jdbi.v3.core.internal.exceptions;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Function;

public final class ThrowableSuppressor {
   private final Deque<Throwable> throwables = new ArrayDeque<>();

   public <T> T suppressAppend(CheckedSupplier<T> supplier, T defaultValue) {
      try {
         return supplier.get();
      } catch (Throwable var4) {
         this.throwables.add(var4);
         return defaultValue;
      }
   }

   public void suppressAppend(Unchecked.CheckedRunnable runnable) {
      try {
         runnable.run();
      } catch (Throwable var3) {
         this.throwables.add(var3);
      }
   }

   public <T> T suppressPrepend(CheckedSupplier<T> supplier, T defaultValue) {
      try {
         return supplier.get();
      } catch (Throwable var4) {
         this.throwables.addFirst(var4);
         return defaultValue;
      }
   }

   public void suppressPrepend(Unchecked.CheckedRunnable runnable) {
      try {
         runnable.run();
      } catch (Throwable var3) {
         this.throwables.addFirst(var3);
      }
   }

   public void attachToThrowable(Throwable throwable) {
      for (Throwable t : this.throwables) {
         throwable.addSuppressed(t);
      }

      this.throwables.clear();
   }

   public <T extends Throwable> void throwIfNecessary(Function<Throwable, T> function) throws T {
      if (!this.throwables.isEmpty()) {
         T result = (T)function.apply(this.throwables.removeFirst());
         this.attachToThrowable(result);
         throw result;
      }
   }

   public void throwIfNecessary() {
      if (!this.throwables.isEmpty()) {
         Throwable first = this.throwables.removeFirst();
         this.attachToThrowable(first);
         throw Sneaky.throwAnyway(first);
      }
   }
}
