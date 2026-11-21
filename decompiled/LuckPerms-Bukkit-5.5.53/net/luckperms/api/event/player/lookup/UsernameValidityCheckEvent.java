package net.luckperms.api.event.player.lookup;

import java.util.concurrent.atomic.AtomicBoolean;
import net.luckperms.api.event.LuckPermsEvent;
import net.luckperms.api.event.util.Param;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface UsernameValidityCheckEvent extends LuckPermsEvent {
   @Param(0)
   @NonNull
   String getUsername();

   @Param(1)
   @NonNull
   AtomicBoolean validityState();

   default boolean isValid() {
      return this.validityState().get();
   }

   default void setValid(boolean valid) {
      this.validityState().set(valid);
   }
}
