package de.oliver.cloud.brigadier.node;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.CommandNode;
import de.oliver.cloud.brigadier.permission.BrigadierPermissionChecker;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = Status.INTERNAL,
   consumers = {"de.oliver.cloud.brigadier.*"}
)
public interface BrigadierNodeFactory<C, S, N extends CommandNode<S>> {
   @NonNull
   N createNode(
      @NonNull String label,
      @NonNull de.oliver.cloud.internal.CommandNode<C> cloudCommand,
      @NonNull Command<S> executor,
      @NonNull BrigadierPermissionChecker<C> permissionChecker
   );

   @NonNull
   N createNode(
      @NonNull String label,
      @NonNull de.oliver.cloud.Command<C> cloudCommand,
      @NonNull Command<S> executor,
      @NonNull BrigadierPermissionChecker<C> permissionChecker
   );

   @NonNull
   N createNode(@NonNull String label, @NonNull de.oliver.cloud.Command<C> cloudCommand, @NonNull Command<S> executor);
}
