package net.luckperms.api.node;

import org.checkerframework.checker.nullness.qual.NonNull;

public interface ScopedNode<N extends ScopedNode<N, B>, B extends NodeBuilder<N, B>> extends Node {
   @NonNull
   @Override
   NodeType<N> getType();

   @NonNull
   @Override
   B toBuilder();
}
