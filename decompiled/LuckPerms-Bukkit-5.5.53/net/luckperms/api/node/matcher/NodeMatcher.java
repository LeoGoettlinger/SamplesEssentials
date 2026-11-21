package net.luckperms.api.node.matcher;

import java.util.function.Predicate;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeEqualityPredicate;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.MetaNode;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.ApiStatus.NonExtendable;

@NonExtendable
public interface NodeMatcher<T extends Node> extends Predicate<Node> {
   @NonNull
   static NodeMatcher<Node> key(@NonNull String key) {
      return LuckPermsProvider.get().getNodeMatcherFactory().key(key);
   }

   @NonNull
   static <T extends Node> NodeMatcher<T> key(@NonNull T node) {
      return LuckPermsProvider.get().getNodeMatcherFactory().key(node);
   }

   @NonNull
   static NodeMatcher<Node> keyStartsWith(@NonNull String startingWith) {
      return LuckPermsProvider.get().getNodeMatcherFactory().keyStartsWith(startingWith);
   }

   @NonNull
   static <T extends Node> NodeMatcher<T> equals(@NonNull T other, @NonNull NodeEqualityPredicate equalityPredicate) {
      return LuckPermsProvider.get().getNodeMatcherFactory().equals(other, equalityPredicate);
   }

   @NonNull
   static NodeMatcher<MetaNode> metaKey(@NonNull String metaKey) {
      return LuckPermsProvider.get().getNodeMatcherFactory().metaKey(metaKey);
   }

   @NonNull
   static NodeMatcher<MetaNode> metaKey(@NonNull MetaNode metaNode) {
      return metaKey(metaNode.getMetaKey());
   }

   @NonNull
   static <T extends Node> NodeMatcher<T> type(@NonNull NodeType<? extends T> type) {
      return LuckPermsProvider.get().getNodeMatcherFactory().type(type);
   }

   boolean test(@NonNull Node var1);
}
