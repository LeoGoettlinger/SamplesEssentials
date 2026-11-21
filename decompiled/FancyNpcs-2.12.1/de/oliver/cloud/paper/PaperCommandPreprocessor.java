package de.oliver.cloud.paper;

import de.oliver.cloud.SenderMapper;
import de.oliver.cloud.bukkit.BukkitCommandContextKeys;
import de.oliver.cloud.bukkit.BukkitCommandManager;
import de.oliver.cloud.bukkit.PluginHolder;
import de.oliver.cloud.bukkit.internal.BukkitHelper;
import de.oliver.cloud.bukkit.internal.CraftBukkitReflection;
import de.oliver.cloud.execution.preprocessor.CommandPreprocessingContext;
import de.oliver.cloud.execution.preprocessor.CommandPreprocessor;
import java.util.concurrent.Executor;
import java.util.function.Function;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
final class PaperCommandPreprocessor<B, C> implements CommandPreprocessor<C> {
   private static final boolean FOLIA = CraftBukkitReflection.classExists("io.papermc.paper.threadedregions.RegionizedServer");
   private final PluginHolder pluginHolder;
   private final SenderMapper<B, C> mapper;
   private final Function<B, CommandSender> senderExtractor;

   PaperCommandPreprocessor(final PluginHolder pluginHolder, final SenderMapper<B, C> mapper, final Function<B, CommandSender> senderExtractor) {
      this.pluginHolder = pluginHolder;
      this.mapper = mapper;
      this.senderExtractor = senderExtractor;
   }

   public void accept(final CommandPreprocessingContext<C> ctx) {
      if (FOLIA) {
         ctx.commandContext().store(BukkitCommandContextKeys.SENDER_SCHEDULER_EXECUTOR, this.foliaExecutorFor(ctx.commandContext().sender()));
      } else if (!(this.pluginHolder instanceof BukkitCommandManager)) {
         ctx.commandContext().store(BukkitCommandContextKeys.SENDER_SCHEDULER_EXECUTOR, BukkitHelper.mainThreadExecutor(this.pluginHolder));
      }
   }

   private Executor foliaExecutorFor(final C sender) {
      CommandSender commandSender = this.senderExtractor.apply(this.mapper.reverse(sender));
      Plugin plugin = this.pluginHolder.owningPlugin();
      if (commandSender instanceof Entity) {
         return task -> ((Entity)commandSender).getScheduler().run(plugin, handle -> task.run(), null);
      } else if (commandSender instanceof BlockCommandSender) {
         BlockCommandSender blockSender = (BlockCommandSender)commandSender;
         return task -> blockSender.getServer().getRegionScheduler().run(plugin, blockSender.getBlock().getLocation(), handle -> task.run());
      } else {
         return task -> plugin.getServer().getGlobalRegionScheduler().run(plugin, handle -> task.run());
      }
   }
}
