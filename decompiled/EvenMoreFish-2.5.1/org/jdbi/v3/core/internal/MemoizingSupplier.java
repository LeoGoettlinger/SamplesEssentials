package org.jdbi.v3.core.internal;

import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class MemoizingSupplier<T> implements Supplier<T> {
   private final Supplier<T> create;
   private Supplier<T> delegate = this::init;
   private volatile boolean initialized;
   private T value;
   private final ReentrantLock initializationLock;

   private MemoizingSupplier(Supplier<T> create) {
      this.create = create;
      this.initializationLock = new ReentrantLock();
   }

   public static <T> MemoizingSupplier<T> of(Supplier<T> supplier) {
      return new MemoizingSupplier<>(supplier);
   }

   @Override
   public T get() {
      return this.delegate.get();
   }

   private T internalGet() {
      return this.value;
   }

   public void ifInitialized(Consumer<T> consumer) {
      if (this.initialized) {
         consumer.accept(this.get());
      }
   }

   private T init() {
      this.initializationLock.lock();

      Object var1;
      try {
         if (!this.initialized) {
            this.value = this.create.get();
            this.initialized = true;
            this.delegate = this::internalGet;
         }

         var1 = this.delegate.get();
      } finally {
         this.initializationLock.unlock();
      }

      return (T)var1;
   }
}
