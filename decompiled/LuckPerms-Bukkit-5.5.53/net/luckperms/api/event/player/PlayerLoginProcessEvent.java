package net.luckperms.api.event.player;

import java.util.UUID;
import net.luckperms.api.event.LuckPermsEvent;
import net.luckperms.api.event.util.Param;
import net.luckperms.api.model.user.User;
import net.luckperms.api.util.Result;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface PlayerLoginProcessEvent extends LuckPermsEvent, Result {
   @Param(0)
   @NonNull
   UUID getUniqueId();

   @Param(1)
   @NonNull
   String getUsername();

   @Override
   default boolean wasSuccessful() {
      return this.getUser() != null;
   }

   @Param(2)
   @Nullable
   User getUser();
}
