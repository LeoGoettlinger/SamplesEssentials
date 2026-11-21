package xyz.lychee.lagfixer.support;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lychee.lagfixer.objects.AbstractFork;

public class PaperSupport extends AbstractFork {
   public PaperSupport(Plugin plugin) {
      super(plugin);
   }

   @Override
   public boolean isSupported() {
      try {
         Class.forName("io.papermc.paper.threadedregions.scheduler.AsyncScheduler");
         Bukkit.getAsyncScheduler();
         Bukkit.getGlobalRegionScheduler();
         return true;
      } catch (Throwable var2) {
         return false;
      }
   }

   @Override
   public int getPriority() {
      return 1;
   }

   @Override
   public double getMspt() {
      return Bukkit.getAverageTickTime();
   }

   @Override
   public boolean isSupportMspt() {
      return true;
   }

   @Override
   public PluginCommand registerCommand(Plugin plugin, String name, List<String> alliases, CommandExecutor executor) {
      try {
         Constructor<PluginCommand> declaredConstructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
         declaredConstructor.setAccessible(true);
         PluginCommand cmd = declaredConstructor.newInstance(name, plugin);
         cmd.setAliases(alliases);
         cmd.setExecutor(executor);
         if (executor instanceof TabCompleter) {
            cmd.setTabCompleter((TabCompleter)executor);
         }

         Bukkit.getCommandMap().register(name, cmd);
         return cmd;
      } catch (Exception var7) {
         return null;
      }
   }

   @Override
   public void unregisterCommand(String name) {
      PluginCommand cmd = Bukkit.getPluginCommand(name);
      if (cmd != null) {
         cmd.unregister(Bukkit.getCommandMap());
      }
   }

   @Override
   public BukkitTask runNow(boolean async, @Nullable Location loc, Runnable run) {
      if (!this.getPlugin().isEnabled()) {
         return new PaperSupport.FoliaTask(this.getPlugin(), null);
      } else {
         Plugin plugin = this.getPlugin();
         ScheduledTask task;
         if (async) {
            task = Bukkit.getAsyncScheduler().runNow(this.getPlugin(), s -> run.run());
         } else if (loc == null) {
            task = Bukkit.getGlobalRegionScheduler().run(this.getPlugin(), s -> run.run());
         } else {
            task = Bukkit.getRegionScheduler().run(this.getPlugin(), loc, s -> run.run());
         }

         return new PaperSupport.FoliaTask(plugin, task);
      }
   }

   @Override
   public BukkitTask runLater(boolean async, Runnable run, long delayInMs) {
      return this.checkTask(delayInMs)
         ? new PaperSupport.FoliaTask(this.getPlugin(), null)
         : new PaperSupport.FoliaTask(
            this.getPlugin(),
            async
               ? Bukkit.getAsyncScheduler().runDelayed(this.getPlugin(), s -> run.run(), delayInMs, TimeUnit.MILLISECONDS)
               : Bukkit.getGlobalRegionScheduler().runDelayed(this.getPlugin(), s -> run.run(), delayInMs / 50L)
         );
   }

   @Override
   public BukkitTask runTimer(boolean async, Runnable run, long initialDelayInMs, long delayInMs) {
      return this.checkTask(initialDelayInMs, delayInMs)
         ? new PaperSupport.FoliaTask(this.getPlugin(), null)
         : new PaperSupport.FoliaTask(
            this.getPlugin(),
            async
               ? Bukkit.getAsyncScheduler().runAtFixedRate(this.getPlugin(), s -> run.run(), initialDelayInMs, delayInMs, TimeUnit.MILLISECONDS)
               : Bukkit.getGlobalRegionScheduler().runAtFixedRate(this.getPlugin(), s -> run.run(), initialDelayInMs / 50L, delayInMs / 50L)
         );
   }

   @Override
   public BukkitTask runLater(boolean async, Runnable run, long delay, TimeUnit unit) {
      return this.checkTask(delay)
         ? new PaperSupport.FoliaTask(this.getPlugin(), null)
         : new PaperSupport.FoliaTask(
            this.getPlugin(),
            async
               ? Bukkit.getAsyncScheduler().runDelayed(this.getPlugin(), s -> run.run(), delay, unit)
               : Bukkit.getGlobalRegionScheduler().runDelayed(this.getPlugin(), s -> run.run(), unit.toMillis(delay) / 50L)
         );
   }

   @Override
   public BukkitTask runTimer(boolean async, Runnable run, long initialDelay, long delay, TimeUnit unit) {
      return this.checkTask(initialDelay, delay)
         ? new PaperSupport.FoliaTask(this.getPlugin(), null)
         : new PaperSupport.FoliaTask(
            this.getPlugin(),
            async
               ? Bukkit.getAsyncScheduler().runAtFixedRate(this.getPlugin(), s -> run.run(), initialDelay, delay, unit)
               : Bukkit.getGlobalRegionScheduler()
                  .runAtFixedRate(this.getPlugin(), s -> run.run(), unit.toMillis(initialDelay) / 50L, unit.toMillis(delay) / 50L)
         );
   }

   public static class FoliaTask implements BukkitTask {
      @Nullable
      private final ScheduledTask task;
      private final Plugin plugin;

      public FoliaTask(Plugin plugin, @Nullable ScheduledTask task) {
         this.task = task;
         this.plugin = plugin;
      }

      public int getTaskId() {
         return this.task == null ? -1 : this.task.hashCode();
      }

      @NotNull
      public Plugin getOwner() {
         return this.plugin;
      }

      public boolean isSync() {
         return false;
      }

      public boolean isCancelled() {
         return this.task == null || this.task.isCancelled();
      }

      public void cancel() {
         if (this.task != null && !this.task.isCancelled()) {
            this.task.cancel();
         }
      }
   }
}
