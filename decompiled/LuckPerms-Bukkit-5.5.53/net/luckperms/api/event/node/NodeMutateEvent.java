package net.luckperms.api.event.node;

import java.util.Set;
import net.luckperms.api.event.LuckPermsEvent;
import net.luckperms.api.event.util.Param;
import net.luckperms.api.model.PermissionHolder;
import net.luckperms.api.model.data.DataType;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.Unmodifiable;

public interface NodeMutateEvent extends LuckPermsEvent {
   @Param(0)
   @NonNull
   PermissionHolder getTarget();

   @Param(1)
   @NonNull
   DataType getDataType();

   @NonNull
   @Unmodifiable
   Set<Node> getDataBefore();

   @Param(2)
   @NonNull
   @Unmodifiable
   Set<Node> getDataAfter();

   default boolean isUser() {
      return this.getTarget() instanceof User;
   }

   default boolean isGroup() {
      return this.getTarget() instanceof Group;
   }
}
