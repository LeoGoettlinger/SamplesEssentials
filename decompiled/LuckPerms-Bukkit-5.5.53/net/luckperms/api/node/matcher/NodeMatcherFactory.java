package net.luckperms.api.node.matcher;

import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeEqualityPredicate;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.MetaNode;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public interface NodeMatcherFactory {
   @NonNull
   NodeMatcher<Node> key(@NonNull String var1);

   @NonNull
   <T extends Node> NodeMatcher<T> key(@NonNull T var1);

   @NonNull
   NodeMatcher<Node> keyStartsWith(@NonNull String var1);

   @NonNull
   <T extends Node> NodeMatcher<T> equals(@NonNull T var1, @NonNull NodeEqualityPredicate var2);

   @NonNull
   NodeMatcher<MetaNode> metaKey(@NonNull String var1);

   @NonNull
   <T extends Node> NodeMatcher<T> type(@NonNull NodeType<? extends T> var1);
}
