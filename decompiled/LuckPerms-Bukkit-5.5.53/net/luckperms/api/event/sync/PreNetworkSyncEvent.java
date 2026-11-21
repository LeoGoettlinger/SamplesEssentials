package net.luckperms.api.event.sync;

import java.util.UUID;
import net.luckperms.api.event.LuckPermsEvent;
import net.luckperms.api.event.type.Cancellable;
import net.luckperms.api.event.util.Param;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface PreNetworkSyncEvent extends LuckPermsEvent, Cancellable {
   @Param(0)
   @NonNull
   UUID getSyncId();

   @Param(1)
   @NonNull
   SyncType getType();

   @Param(2)
   @Nullable
   UUID getSpecificUserUniqueId();
}
