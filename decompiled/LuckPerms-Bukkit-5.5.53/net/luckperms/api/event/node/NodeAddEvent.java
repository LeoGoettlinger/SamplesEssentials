package net.luckperms.api.event.node;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import net.luckperms.api.event.util.Param;
import net.luckperms.api.node.Node;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.Unmodifiable;

public interface NodeAddEvent extends NodeMutateEvent {
   @Param(3)
   @NonNull
   Node getNode();

   @NonNull
   @Unmodifiable
   @Override
   default Set<Node> getDataBefore() {
      Set<Node> nodes = new HashSet<>(this.getDataAfter());
      nodes.remove(this.getNode());
      return Collections.unmodifiableSet(nodes);
   }
}
