package net.luckperms.api.event.type;

import java.util.concurrent.atomic.AtomicBoolean;
import net.luckperms.api.event.util.Param;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface Cancellable {
   @Param(-1)
   @NonNull
   AtomicBoolean cancellationState();

   default boolean isCancelled() {
      return this.cancellationState().get();
   }

   default boolean isNotCancelled() {
      return !this.isCancelled();
   }

   default boolean setCancelled(boolean cancelled) {
      return this.cancellationState().getAndSet(cancelled);
   }
}
