package net.luckperms.api.node.types;

import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.node.NodeBuilder;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.ScopedNode;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface DisplayNameNode extends ScopedNode<DisplayNameNode, DisplayNameNode.Builder> {
   @NonNull
   @Override
   default NodeType<DisplayNameNode> getType() {
      return NodeType.DISPLAY_NAME;
   }

   @NonNull
   String getDisplayName();

   @NonNull
   static DisplayNameNode.Builder builder() {
      return LuckPermsProvider.get().getNodeBuilderRegistry().forDisplayName();
   }

   @NonNull
   static DisplayNameNode.Builder builder(@NonNull String displayName) {
      return builder().displayName(displayName);
   }

   public interface Builder extends NodeBuilder<DisplayNameNode, DisplayNameNode.Builder> {
      @NonNull
      DisplayNameNode.Builder displayName(@NonNull String var1);
   }
}
