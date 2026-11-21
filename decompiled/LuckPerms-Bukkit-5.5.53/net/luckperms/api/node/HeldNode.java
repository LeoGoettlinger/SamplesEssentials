package net.luckperms.api.node;

import org.checkerframework.checker.nullness.qual.NonNull;

@Deprecated
public interface HeldNode<T> {
   @NonNull
   T getHolder();

   @NonNull
   Node getNode();
}
