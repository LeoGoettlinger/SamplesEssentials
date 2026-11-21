package de.oliver.fancylib.serverSoftware.schedulers;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import java.util.concurrent.TimeUnit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class FoliaScheduler implements FancyScheduler {
   private static final long NANOSECONDS_PER_TICK = 50000000L;
   private final JavaPlugin plugin;
   private ScheduledTask scheduledTask;

   public FoliaScheduler(JavaPlugin plugin) {
      this.plugin = plugin;
   }

   @NotNull
   @Override
   public FancyScheduler runTask(Location location, Runnable task) {
      if (location != null) {
         this.scheduledTask = this.plugin.getServer().getRegionScheduler().run(this.plugin, location, scheduledTask1 -> task.run());
      } else {
         this.scheduledTask = this.plugin.getServer().getGlobalRegionScheduler().run(this.plugin, scheduledTask1 -> task.run());
      }

      return this;
   }

   @NotNull
   @Override
   public FancyScheduler runTaskAsynchronously(Runnable task) {
      this.scheduledTask = this.plugin.getServer().getAsyncScheduler().runNow(this.plugin, scheduledTask1 -> task.run());
      return this;
   }

   @NotNull
   @Override
   public FancyScheduler runTaskLater(Location location, long delay, Runnable task) {
      if (location != null) {
         this.scheduledTask = this.plugin.getServer().getRegionScheduler().runDelayed(this.plugin, location, scheduledTask1 -> task.run(), delay);
      } else {
         this.scheduledTask = this.plugin.getServer().getGlobalRegionScheduler().runDelayed(this.plugin, scheduledTask1 -> task.run(), delay);
      }

      return this;
   }

   @NotNull
   @Override
   public FancyScheduler runTaskLaterAsynchronously(long delay, Runnable task) {
      this.scheduledTask = this.plugin
         .getServer()
         .getAsyncScheduler()
         .runDelayed(this.plugin, scheduledTask1 -> task.run(), delay * 50000000L, TimeUnit.NANOSECONDS);
      return this;
   }

   @NotNull
   @Override
   public FancyScheduler runTaskTimer(Location location, long delay, long period, Runnable task) {
      this.scheduledTask = this.plugin.getServer().getRegionScheduler().runAtFixedRate(this.plugin, location, scheduledTask1 -> task.run(), delay, period);
      return this;
   }

   @NotNull
   @Override
   public FancyScheduler runTaskTimerAsynchronously(long delay, long period, Runnable task) {
      this.scheduledTask = this.plugin
         .getServer()
         .getAsyncScheduler()
         .runAtFixedRate(this.plugin, scheduledTask1 -> task.run(), delay * 50000000L, period * 50000000L, TimeUnit.NANOSECONDS);
      return this;
   }

   @Override
   public void cancel() {
      if (!this.scheduledTask.isCancelled()) {
         this.scheduledTask.cancel();
      }
   }
}
