package net.luckperms.api.node.types;

import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.node.NodeBuilder;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.ScopedNode;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface WeightNode extends ScopedNode<WeightNode, WeightNode.Builder> {
   @NonNull
   @Override
   default NodeType<WeightNode> getType() {
      return NodeType.WEIGHT;
   }

   int getWeight();

   @NonNull
   static WeightNode.Builder builder() {
      return LuckPermsProvider.get().getNodeBuilderRegistry().forWeight();
   }

   @NonNull
   static WeightNode.Builder builder(int weight) {
      return builder().weight(weight);
   }

   public interface Builder extends NodeBuilder<WeightNode, WeightNode.Builder> {
      @NonNull
      WeightNode.Builder weight(int var1);
   }
}
