package net.luckperms.api.event.user;

import java.util.UUID;
import net.luckperms.api.event.LuckPermsEvent;
import net.luckperms.api.event.util.Param;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface UserFirstLoginEvent extends LuckPermsEvent {
   @Param(0)
   @NonNull
   UUID getUniqueId();

   @Param(1)
   @NonNull
   String getUsername();
}
