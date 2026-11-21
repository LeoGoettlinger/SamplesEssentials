package net.luckperms.api.event.group;

import java.util.Set;
import net.luckperms.api.event.LuckPermsEvent;
import net.luckperms.api.event.cause.DeletionCause;
import net.luckperms.api.event.util.Param;
import net.luckperms.api.node.Node;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface GroupDeleteEvent extends LuckPermsEvent {
   @Param(0)
   @NonNull
   String getGroupName();

   @Param(1)
   @NonNull
   Set<Node> getExistingData();

   @Param(2)
   @NonNull
   DeletionCause getCause();
}
