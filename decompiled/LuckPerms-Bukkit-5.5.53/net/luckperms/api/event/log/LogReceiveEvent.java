package net.luckperms.api.event.log;

import java.util.UUID;
import net.luckperms.api.actionlog.Action;
import net.luckperms.api.event.LuckPermsEvent;
import net.luckperms.api.event.util.Param;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface LogReceiveEvent extends LuckPermsEvent {
   @Param(0)
   @NonNull
   UUID getLogId();

   @Param(1)
   @NonNull
   Action getEntry();
}
