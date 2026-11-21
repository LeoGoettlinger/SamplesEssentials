package de.theredend2000.advancedhunt.lib.cloud.bukkit;

import de.theredend2000.advancedhunt.lib.cloud.bukkit.internal.BukkitBackwardsBrigadierSenderMapper;
import de.theredend2000.advancedhunt.lib.cloud.bukkit.internal.BukkitHelper;
import de.theredend2000.advancedhunt.lib.cloud.execution.preprocessor.CommandPreprocessingContext;
import de.theredend2000.advancedhunt.lib.cloud.execution.preprocessor.CommandPreprocessor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

final class BukkitCommandPreprocessor<C> implements CommandPreprocessor<C> {
   private final BukkitCommandManager<C> commandManager;
   @Nullable
   private final BukkitBackwardsBrigadierSenderMapper<C, ?> mapper;

   BukkitCommandPreprocessor(@NonNull final BukkitCommandManager<C> commandManager) {
      this.commandManager = commandManager;
      if (this.commandManager.hasCapability(CloudBukkitCapabilities.BRIGADIER)) {
         this.mapper = new BukkitBackwardsBrigadierSenderMapper<>(this.commandManager.senderMapper());
      } else {
         this.mapper = null;
      }
   }

   public void accept(@NonNull final CommandPreprocessingContext<C> context) {
      if (this.mapper != null && !context.commandContext().contains("_cloud_brigadier_native_sender")) {
         context.commandContext().store("_cloud_brigadier_native_sender", this.mapper.apply(context.commandContext().sender()));
      }

      context.commandContext()
         .store(BukkitCommandContextKeys.BUKKIT_COMMAND_SENDER, this.commandManager.senderMapper().reverse(context.commandContext().sender()));
      context.commandContext().computeIfAbsent(BukkitCommandContextKeys.SENDER_SCHEDULER_EXECUTOR, $ -> BukkitHelper.mainThreadExecutor(this.commandManager));
   }
}
