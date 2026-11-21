package com.ranull.graves.bundledlibraries.scheduler;

import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask.ExecutionState;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

class FoliaScheduler implements GlobalScheduler {
   private final ConcurrentHashMap<Integer, FoliaScheduler.FoliaTask> tasks = new ConcurrentHashMap<>();
   private final Random random = new Random();
   private final RegionScheduler regionScheduler = Bukkit.getServer().getRegionScheduler();
   private final GlobalRegionScheduler globalRegionScheduler = Bukkit.getServer().getGlobalRegionScheduler();
   private final AsyncScheduler asyncScheduler = Bukkit.getServer().getAsyncScheduler();
   private final Plugin plugin;

   private synchronized int nextId() {
      this.tasks.values().removeIf(t -> t == null || t.isFinished());

      int taskId;
      do {
         taskId = this.random.nextInt();
      } while (this.tasks.containsKey(taskId));

      return taskId;
   }

   @Override
   public boolean isGlobalThread() {
      return Bukkit.getServer().isGlobalTickThread();
   }

   @Override
   public boolean isEntityThread(Entity entity) {
      return Bukkit.getServer().isOwnedByCurrentRegion(entity);
   }

   @Override
   public boolean isRegionThread(Location location) {
      return Bukkit.getServer().isOwnedByCurrentRegion(location);
   }

   @Override
   public GlobalTask runTask(Runnable runnable) {
      return this.runTask(this.plugin, runnable);
   }

   @Override
   public GlobalTask runTaskLater(Runnable runnable, long delay) {
      return this.runTaskLater(this.plugin, runnable, delay);
   }

   @Override
   public GlobalTask runTaskTimer(Runnable runnable, long delay, long period) {
      return this.runTaskTimer(this.plugin, runnable, delay, period);
   }

   @Override
   public GlobalTask runTask(Plugin plugin, Runnable runnable) {
      return new FoliaScheduler.FoliaTask(this.globalRegionScheduler.run(plugin, task -> runnable.run()), this.nextId());
   }

   @Override
   public GlobalTask runTaskLater(Plugin plugin, Runnable runnable, long delay) {
      return (GlobalTask)(delay <= 0L
         ? this.runTask(runnable)
         : new FoliaScheduler.FoliaTask(this.globalRegionScheduler.runDelayed(plugin, task -> runnable.run(), delay), this.nextId()));
   }

   @Override
   public GlobalTask runTaskTimer(Plugin plugin, Runnable runnable, long delay, long period) {
      return new FoliaScheduler.FoliaTask(
         this.globalRegionScheduler.runAtFixedRate(plugin, task -> runnable.run(), delay <= 0L ? 1L : delay, period), this.nextId()
      );
   }

   @Override
   public GlobalTask runTaskAsynchronously(Runnable runnable) {
      return this.runTaskAsynchronously(this.plugin, runnable);
   }

   @Override
   public GlobalTask runTaskLaterAsynchronously(Runnable runnable, long delay) {
      return this.runTaskLaterAsynchronously(this.plugin, runnable, delay);
   }

   @Override
   public GlobalTask runTaskTimerAsynchronously(Runnable runnable, long delay, long period) {
      return this.runTaskTimerAsynchronously(this.plugin, runnable, delay, period);
   }

   @Override
   public GlobalTask runTaskAsynchronously(Plugin plugin, Runnable runnable) {
      return new FoliaScheduler.FoliaTask(this.asyncScheduler.runNow(plugin, task -> runnable.run()), this.nextId());
   }

   @Override
   public GlobalTask runTaskLaterAsynchronously(Plugin plugin, Runnable runnable, long delay) {
      return new FoliaScheduler.FoliaTask(
         this.asyncScheduler.runDelayed(plugin, task -> runnable.run(), (delay <= 0L ? 1L : delay) * 50L, TimeUnit.MILLISECONDS), this.nextId()
      );
   }

   @Override
   public GlobalTask runTaskTimerAsynchronously(Plugin plugin, Runnable runnable, long delay, long period) {
      return new FoliaScheduler.FoliaTask(
         this.asyncScheduler.runAtFixedRate(plugin, task -> runnable.run(), (delay <= 0L ? 1L : delay) * 50L, period * 50L, TimeUnit.MILLISECONDS),
         this.nextId()
      );
   }

   @Override
   public GlobalTask runTask(Location location, Runnable runnable) {
      return new FoliaScheduler.FoliaTask(this.regionScheduler.run(this.plugin, location, task -> runnable.run()), this.nextId());
   }

   @Override
   public GlobalTask runTaskLater(Location location, Runnable runnable, long delay) {
      return (GlobalTask)(delay <= 0L
         ? this.runTask(runnable)
         : new FoliaScheduler.FoliaTask(this.regionScheduler.runDelayed(this.plugin, location, task -> runnable.run(), delay), this.nextId()));
   }

   @Override
   public GlobalTask runTaskTimer(Location location, Runnable runnable, long delay, long period) {
      return new FoliaScheduler.FoliaTask(
         this.regionScheduler.runAtFixedRate(this.plugin, location, task -> runnable.run(), delay <= 0L ? 1L : delay, period), this.nextId()
      );
   }

   @Override
   public GlobalTask runTask(Entity entity, Runnable runnable) {
      return new FoliaScheduler.FoliaTask(entity.getScheduler().run(this.plugin, task -> runnable.run(), null), this.nextId());
   }

   @Override
   public GlobalTask runTaskLater(Entity entity, Runnable runnable, long delay) {
      return (GlobalTask)(delay <= 0L
         ? this.runTask(entity, runnable)
         : new FoliaScheduler.FoliaTask(entity.getScheduler().runDelayed(this.plugin, task -> runnable.run(), null, delay), this.nextId()));
   }

   @Override
   public GlobalTask runTaskTimer(Entity entity, Runnable runnable, long delay, long period) {
      return new FoliaScheduler.FoliaTask(
         entity.getScheduler().runAtFixedRate(this.plugin, task -> runnable.run(), null, delay <= 0L ? 1L : delay, period), this.nextId()
      );
   }

   @Override
   public void execute(Runnable runnable) {
      this.globalRegionScheduler.execute(this.plugin, runnable);
   }

   @Override
   public void cancel(int taskId) {
      try {
         this.tasks.get(taskId).cancel();
      } catch (Exception var3) {
      }
   }

   @Override
   public void execute(Location location, Runnable runnable) {
      this.regionScheduler.execute(this.plugin, location, runnable);
   }

   @Override
   public void execute(Entity entity, Runnable runnable) {
      entity.getScheduler().execute(this.plugin, runnable, null, 1L);
   }

   @Override
   public void cancelAll() {
      this.cancelAll(this.plugin);
   }

   @Override
   public void cancelAll(Plugin plugin) {
      this.globalRegionScheduler.cancelTasks(plugin);
      this.asyncScheduler.cancelTasks(plugin);
   }

   @Override
   public String toString() {
      return "FoliaScheduler{plugin=" + this.plugin + '}';
   }

   @Generated
   FoliaScheduler(Plugin plugin) {
      this.plugin = plugin;
   }

   private final class FoliaTask implements GlobalTask {
      private final ScheduledTask task;
      private final int taskId;

      private FoliaTask(ScheduledTask task, int taskId) {
         this.task = task;
         this.taskId = taskId;
         FoliaScheduler.this.tasks.put(taskId, this);
      }

      @Override
      public void cancel() {
         this.task.cancel();
         FoliaScheduler.this.tasks.remove(this.taskId);
      }

      @Override
      public boolean isCancelled() {
         return this.task.isCancelled();
      }

      @Override
      public Plugin getPlugin() {
         return this.task.getOwningPlugin();
      }

      @Override
      public boolean isRunning() {
         switch (this.task.getExecutionState()) {
            case RUNNING:
            case CANCELLED_RUNNING:
               return true;
            default:
               return false;
         }
      }

      @Override
      public boolean isRepeating() {
         return this.task.isRepeatingTask();
      }

      boolean isFinished() {
         return this.task.getExecutionState() == ExecutionState.FINISHED;
      }

      @Generated
      @Override
      public int getTaskId() {
         return this.taskId;
      }
   }
}
