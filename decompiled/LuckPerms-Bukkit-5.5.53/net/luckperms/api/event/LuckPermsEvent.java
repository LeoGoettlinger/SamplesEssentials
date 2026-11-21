package net.luckperms.api.event;

import net.luckperms.api.LuckPerms;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface LuckPermsEvent {
   @NonNull
   LuckPerms getLuckPerms();

   @NonNull
   Class<? extends LuckPermsEvent> getEventType();
}
