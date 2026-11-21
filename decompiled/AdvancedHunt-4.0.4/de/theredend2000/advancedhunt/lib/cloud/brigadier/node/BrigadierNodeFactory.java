package de.theredend2000.advancedhunt.lib.cloud.brigadier.node;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.CommandNode;
import de.theredend2000.advancedhunt.lib.cloud.brigadier.permission.BrigadierPermissionChecker;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = API.Status.INTERNAL,
   consumers = {"de.theredend2000.advancedhunt.lib.cloud.brigadier.*"}
)
public interface BrigadierNodeFactory<C, S, N extends CommandNode<S>> {
   @NonNull
   N createNode(
      @NonNull String label,
      @NonNull de.theredend2000.advancedhunt.lib.cloud.internal.CommandNode<C> cloudCommand,
      @NonNull Command<S> executor,
      @NonNull BrigadierPermissionChecker<C> permissionChecker
   );

   @NonNull
   N createNode(
      @NonNull String label,
      @NonNull de.theredend2000.advancedhunt.lib.cloud.Command<C> cloudCommand,
      @NonNull Command<S> executor,
      @NonNull BrigadierPermissionChecker<C> permissionChecker
   );

   @NonNull
   N createNode(@NonNull String label, @NonNull de.theredend2000.advancedhunt.lib.cloud.Command<C> cloudCommand, @NonNull Command<S> executor);
}
