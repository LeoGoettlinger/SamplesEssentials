package net.luckperms.api.cacheddata;

import net.luckperms.api.node.Node;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface Result<T, N extends Node> {
   T result();

   @Nullable
   N node();
}
