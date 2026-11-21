package vrx.economy.vRXClans.libs.cloud.brigadier.node;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.CommandNode;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import vrx.economy.vRXClans.libs.cloud.brigadier.permission.BrigadierPermissionChecker;

@API(
   status = API.Status.INTERNAL,
   consumers = {"vrx.economy.vRXClans.libs.cloud.brigadier.*"}
)
public interface BrigadierNodeFactory<C, S, N extends CommandNode<S>> {
   @NonNull
   N createNode(
      @NonNull String label,
      @NonNull vrx.economy.vRXClans.libs.cloud.internal.CommandNode<C> cloudCommand,
      @NonNull Command<S> executor,
      @NonNull BrigadierPermissionChecker<C> permissionChecker
   );

   @NonNull
   N createNode(
      @NonNull String label,
      @NonNull vrx.economy.vRXClans.libs.cloud.Command<C> cloudCommand,
      @NonNull Command<S> executor,
      @NonNull BrigadierPermissionChecker<C> permissionChecker
   );

   @NonNull
   N createNode(@NonNull String label, @NonNull vrx.economy.vRXClans.libs.cloud.Command<C> cloudCommand, @NonNull Command<S> executor);
}
