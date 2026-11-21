package net.luckperms.api.node.types;

import java.util.Optional;
import java.util.regex.Pattern;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.node.NodeBuilder;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.ScopedNode;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface RegexPermissionNode extends ScopedNode<RegexPermissionNode, RegexPermissionNode.Builder> {
   @NonNull
   @Override
   default NodeType<RegexPermissionNode> getType() {
      return NodeType.REGEX_PERMISSION;
   }

   @NonNull
   String getPatternString();

   @NonNull
   Optional<Pattern> getPattern();

   @NonNull
   static RegexPermissionNode.Builder builder() {
      return LuckPermsProvider.get().getNodeBuilderRegistry().forRegexPermission();
   }

   @NonNull
   static RegexPermissionNode.Builder builder(@NonNull String pattern) {
      return builder().pattern(pattern);
   }

   @NonNull
   static RegexPermissionNode.Builder builder(@NonNull Pattern pattern) {
      return builder().pattern(pattern);
   }

   public interface Builder extends NodeBuilder<RegexPermissionNode, RegexPermissionNode.Builder> {
      @NonNull
      RegexPermissionNode.Builder pattern(@NonNull String var1);

      @NonNull
      RegexPermissionNode.Builder pattern(@NonNull Pattern var1);
   }
}
