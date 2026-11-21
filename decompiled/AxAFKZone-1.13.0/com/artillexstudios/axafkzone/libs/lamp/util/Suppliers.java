package com.artillexstudios.axafkzone.libs.lamp.util;

import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Suppliers {
   private Suppliers() {
   }

   @NotNull
   public static <T> Supplier<T> lazy(@NotNull Supplier<T> fetch) {
      Preconditions.notNull(fetch, "fetch supplier");
      return new Suppliers.LazySupplier<>(fetch);
   }

   static final class LazySupplier<T> implements Supplier<T> {
      final Supplier<T> delegate;
      transient volatile boolean initialized;
      @Nullable
      transient T value;

      LazySupplier(Supplier<T> delegate) {
         this.delegate = delegate;
      }

      @Override
      public T get() {
         if (!this.initialized) {
            synchronized (this) {
               if (!this.initialized) {
                  T t = this.delegate.get();
                  this.value = t;
                  this.initialized = true;
                  return t;
               }
            }
         }

         return this.value;
      }

      @Override
      public String toString() {
         return "Suppliers.lazy(" + (this.initialized ? "<supplier that returned " + this.value + ">" : this.delegate) + ")";
      }
   }
}
