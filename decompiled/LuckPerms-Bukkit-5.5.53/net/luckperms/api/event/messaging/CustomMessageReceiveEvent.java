package net.luckperms.api.event.messaging;

import net.luckperms.api.event.LuckPermsEvent;
import net.luckperms.api.event.util.Param;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface CustomMessageReceiveEvent extends LuckPermsEvent {
   @Param(0)
   @NonNull
   String getChannelId();

   @Param(1)
   @NonNull
   String getPayload();
}
