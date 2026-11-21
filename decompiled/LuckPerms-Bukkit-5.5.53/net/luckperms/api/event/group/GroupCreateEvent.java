package net.luckperms.api.event.group;

import net.luckperms.api.event.LuckPermsEvent;
import net.luckperms.api.event.cause.CreationCause;
import net.luckperms.api.event.util.Param;
import net.luckperms.api.model.group.Group;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface GroupCreateEvent extends LuckPermsEvent {
   @Param(0)
   @NonNull
   Group getGroup();

   @Param(1)
   @NonNull
   CreationCause getCause();
}
