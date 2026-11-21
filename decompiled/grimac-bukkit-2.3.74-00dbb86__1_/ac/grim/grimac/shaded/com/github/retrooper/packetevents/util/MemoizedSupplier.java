package ac.grim.grimac.shaded.com.github.retrooper.packetevents.util;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import java.util.function.Supplier;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
@ApiStatus.Internal
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
