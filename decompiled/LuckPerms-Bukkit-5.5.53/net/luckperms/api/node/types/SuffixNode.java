package net.luckperms.api.node.types;

import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.node.NodeType;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface SuffixNode extends ChatMetaNode<SuffixNode, SuffixNode.Builder> {
   @NonNull
   @Override
   default NodeType<SuffixNode> getType() {
      return NodeType.SUFFIX;
   }

   @NonNull
   static SuffixNode.Builder builder() {
      return LuckPermsProvider.get().getNodeBuilderRegistry().forSuffix();
   }

   @NonNull
   static SuffixNode.Builder builder(@NonNull String suffix, int priority) {
      return builder().suffix(suffix).priority(priority);
   }

   public interface Builder extends ChatMetaNode.Builder<SuffixNode, SuffixNode.Builder> {
      @NonNull
      SuffixNode.Builder suffix(@NonNull String var1);
   }
}
