package net.luckperms.api.node.types;

import net.luckperms.api.node.ChatMetaType;
import net.luckperms.api.node.NodeBuilder;
import net.luckperms.api.node.ScopedNode;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface ChatMetaNode<N extends ChatMetaNode<N, B>, B extends ChatMetaNode.Builder<N, B>> extends ScopedNode<N, B> {
   int getPriority();

   String getMetaValue();

   @NonNull
   ChatMetaType getMetaType();

   public interface Builder<N extends ChatMetaNode<N, B>, B extends ChatMetaNode.Builder<N, B>> extends NodeBuilder<N, B> {
      @NonNull
      B priority(int var1);
   }
}
