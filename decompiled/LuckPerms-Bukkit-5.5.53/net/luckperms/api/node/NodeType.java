package net.luckperms.api.node;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import net.luckperms.api.node.types.ChatMetaNode;
import net.luckperms.api.node.types.DisplayNameNode;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.node.types.MetaNode;
import net.luckperms.api.node.types.PermissionNode;
import net.luckperms.api.node.types.PrefixNode;
import net.luckperms.api.node.types.RegexPermissionNode;
import net.luckperms.api.node.types.SuffixNode;
import net.luckperms.api.node.types.WeightNode;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface NodeType<T extends Node> {
   NodeType<PermissionNode> PERMISSION = new SimpleNodeType<>("PERMISSION", n -> n instanceof PermissionNode, n -> (PermissionNode)n);
   NodeType<RegexPermissionNode> REGEX_PERMISSION = new SimpleNodeType<>("REGEX_PERMISSION", n -> n instanceof RegexPermissionNode, n -> (RegexPermissionNode)n);
   NodeType<InheritanceNode> INHERITANCE = new SimpleNodeType<>("INHERITANCE", n -> n instanceof InheritanceNode, n -> (InheritanceNode)n);
   NodeType<PrefixNode> PREFIX = new SimpleNodeType<>("PREFIX", n -> n instanceof PrefixNode, n -> (PrefixNode)n);
   NodeType<SuffixNode> SUFFIX = new SimpleNodeType<>("SUFFIX", n -> n instanceof SuffixNode, n -> (SuffixNode)n);
   NodeType<MetaNode> META = new SimpleNodeType<>("META", n -> n instanceof MetaNode, n -> (MetaNode)n);
   NodeType<WeightNode> WEIGHT = new SimpleNodeType<>("WEIGHT", n -> n instanceof WeightNode, n -> (WeightNode)n);
   NodeType<DisplayNameNode> DISPLAY_NAME = new SimpleNodeType<>("DISPLAY_NAME", n -> n instanceof DisplayNameNode, n -> (DisplayNameNode)n);
   NodeType<ChatMetaNode<?, ?>> CHAT_META = new SimpleNodeType<>("CHAT_META", n -> n instanceof ChatMetaNode, n -> (ChatMetaNode<?, ?>)n);
   NodeType<Node> META_OR_CHAT_META = new SimpleNodeType<>("META_OR_CHAT_META", n -> META.matches(n) || CHAT_META.matches(n), Function.identity());

   @NonNull
   String name();

   boolean matches(@NonNull Node var1);

   @NonNull
   T cast(@NonNull Node var1);

   @NonNull
   default Optional<T> tryCast(@NonNull Node node) {
      Objects.requireNonNull(node, "node");
      return !this.matches(node) ? Optional.empty() : Optional.of(this.cast(node));
   }

   @NonNull
   default Predicate<Node> predicate() {
      return this::matches;
   }

   @NonNull
   default Predicate<Node> predicate(@NonNull Predicate<? super T> and) {
      return node -> this.matches(node) && and.test(this.cast(node));
   }
}
