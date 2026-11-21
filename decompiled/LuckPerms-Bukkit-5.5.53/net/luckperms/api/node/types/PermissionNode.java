package net.luckperms.api.node.types;

import java.util.OptionalInt;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.node.NodeBuilder;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.ScopedNode;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface PermissionNode extends ScopedNode<PermissionNode, PermissionNode.Builder> {
   @NonNull
   @Override
   default NodeType<PermissionNode> getType() {
      return NodeType.PERMISSION;
   }

   @NonNull
   String getPermission();

   boolean isWildcard();

   @NonNull
   OptionalInt getWildcardLevel();

   @NonNull
   static PermissionNode.Builder builder() {
      return LuckPermsProvider.get().getNodeBuilderRegistry().forPermission();
   }

   @NonNull
   static PermissionNode.Builder builder(@NonNull String permission) {
      return builder().permission(permission);
   }

   public interface Builder extends NodeBuilder<PermissionNode, PermissionNode.Builder> {
      @NonNull
      PermissionNode.Builder permission(@NonNull String var1);
   }
}
