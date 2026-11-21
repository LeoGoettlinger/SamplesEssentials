package net.luckperms.api.event.log;

import net.luckperms.api.actionlog.Action;
import net.luckperms.api.event.LuckPermsEvent;
import net.luckperms.api.event.type.Cancellable;
import net.luckperms.api.event.util.Param;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface LogBroadcastEvent extends LuckPermsEvent, Cancellable {
   @Param(0)
   @NonNull
   Action getEntry();

   @Param(1)
   @NonNull
   LogBroadcastEvent.Origin getOrigin();

   public static enum Origin {
      LOCAL,
      LOCAL_API,
      REMOTE;
   }
}
