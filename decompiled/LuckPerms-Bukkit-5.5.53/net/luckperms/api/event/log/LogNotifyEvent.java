package net.luckperms.api.event.log;

import net.luckperms.api.actionlog.Action;
import net.luckperms.api.event.LuckPermsEvent;
import net.luckperms.api.event.type.Cancellable;
import net.luckperms.api.event.util.Param;
import net.luckperms.api.platform.PlatformEntity;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface LogNotifyEvent extends LuckPermsEvent, Cancellable {
   @Param(0)
   @NonNull
   Action getEntry();

   @Param(1)
   @NonNull
   LogNotifyEvent.Origin getOrigin();

   @Param(2)
   @NonNull
   PlatformEntity getNotifiable();

   public static enum Origin {
      LOCAL,
      LOCAL_API,
      REMOTE;
   }
}
