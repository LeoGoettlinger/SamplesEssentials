package net.luckperms.api.node.types;

import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.node.NodeType;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface PrefixNode extends ChatMetaNode<PrefixNode, PrefixNode.Builder> {
   @NonNull
   @Override
   default NodeType<PrefixNode> getType() {
      return NodeType.PREFIX;
   }

   @NonNull
   static PrefixNode.Builder builder() {
      return LuckPermsProvider.get().getNodeBuilderRegistry().forPrefix();
   }

   @NonNull
   static PrefixNode.Builder builder(@NonNull String prefix, int priority) {
      return builder().prefix(prefix).priority(priority);
   }

   public interface Builder extends ChatMetaNode.Builder<PrefixNode, PrefixNode.Builder> {
      @NonNull
      PrefixNode.Builder prefix(@NonNull String var1);
   }
}
