package net.luckperms.api.node.types;

import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.node.NodeBuilder;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.ScopedNode;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface InheritanceNode extends ScopedNode<InheritanceNode, InheritanceNode.Builder> {
   @NonNull
   @Override
   default NodeType<InheritanceNode> getType() {
      return NodeType.INHERITANCE;
   }

   @NonNull
   String getGroupName();

   @NonNull
   static InheritanceNode.Builder builder() {
      return LuckPermsProvider.get().getNodeBuilderRegistry().forInheritance();
   }

   @NonNull
   static InheritanceNode.Builder builder(@NonNull String group) {
      return builder().group(group);
   }

   @NonNull
   static InheritanceNode.Builder builder(@NonNull Group group) {
      return builder().group(group);
   }

   public interface Builder extends NodeBuilder<InheritanceNode, InheritanceNode.Builder> {
      @NonNull
      InheritanceNode.Builder group(@NonNull String var1);

      @NonNull
      InheritanceNode.Builder group(@NonNull Group var1);
   }
}
