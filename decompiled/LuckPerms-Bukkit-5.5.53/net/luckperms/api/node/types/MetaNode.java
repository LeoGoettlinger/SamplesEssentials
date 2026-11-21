package net.luckperms.api.node.types;

import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.node.NodeBuilder;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.ScopedNode;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface MetaNode extends ScopedNode<MetaNode, MetaNode.Builder> {
   @NonNull
   @Override
   default NodeType<MetaNode> getType() {
      return NodeType.META;
   }

   @NonNull
   String getMetaKey();

   @NonNull
   String getMetaValue();

   @NonNull
   static MetaNode.Builder builder() {
      return LuckPermsProvider.get().getNodeBuilderRegistry().forMeta();
   }

   @NonNull
   static MetaNode.Builder builder(@NonNull String key, @NonNull String value) {
      return builder().key(key).value(value);
   }

   public interface Builder extends NodeBuilder<MetaNode, MetaNode.Builder> {
      @NonNull
      MetaNode.Builder key(@NonNull String var1);

      @NonNull
      MetaNode.Builder value(@NonNull String var1);
   }
}
