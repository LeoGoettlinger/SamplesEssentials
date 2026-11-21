package net.luckperms.api.event.sync;

import java.util.UUID;
import net.luckperms.api.event.LuckPermsEvent;
import net.luckperms.api.event.util.Param;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface PostNetworkSyncEvent extends LuckPermsEvent {
   @Param(0)
   @NonNull
   UUID getSyncId();

   @Param(1)
   @NonNull
   SyncType getType();

   @Param(2)
   boolean didSyncOccur();

   @Param(3)
   @Nullable
   UUID getSpecificUserUniqueId();
}
