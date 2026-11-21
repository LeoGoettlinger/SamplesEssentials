package com.github.retrooper.packetevents.util;

import java.util.function.Supplier;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
@Internal
public final class MemoizedSupplier<T> implements Supplier<T> {
   @Nullable
   private Supplier<T> delegate;
   @MonotonicNonNull
   private volatile T value;

   public MemoizedSupplier(Supplier<T> delegate) {
      this.delegate = delegate;
   }

   @Override
   public T get() {
      T v = this.value;
      if (v == null) {
         synchronized (this) {
            v = this.value;
            if (this.value == null) {
               assert this.delegate != null;

               this.value = v = this.delegate.get();
               this.delegate = null;
            }
         }
      }

      return v;
   }
}
