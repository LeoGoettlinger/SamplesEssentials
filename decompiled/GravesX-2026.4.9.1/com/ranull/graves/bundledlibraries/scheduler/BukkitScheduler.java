package com.ranull.graves.bundledlibraries.scheduler;

import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

final class BukkitScheduler implements GlobalScheduler {
   @NotNull
   final Plugin plugin;

   @Override
   public boolean isGlobalThread() {
      return Bukkit.getServer().isPrimaryThread();
   }

   @Override
   public boolean isEntityThread(Entity entity) {
      return this.isGlobalThread();
   }

   @Override
   public boolean isRegionThread(Location location) {
      return this.isGlobalThread();
   }

   @Override
   public GlobalTask runTask(Runnable runnable) {
      return new BukkitScheduler.BukkitTask(Bukkit.getScheduler().runTask(this.plugin, runnable));
   }

   @Override
   public GlobalTask runTaskLater(Runnable runnable, long delay) {
      return new BukkitScheduler.BukkitTask(Bukkit.getScheduler().runTaskLater(this.plugin, runnable, delay));
   }

   @Override
   public GlobalTask runTaskTimer(Runnable runnable, long delay, long period) {
      return new BukkitScheduler.BukkitTask(Bukkit.getScheduler().runTaskTimer(this.plugin, runnable, delay, period), true);
   }

   @Override
   public GlobalTask runTask(Plugin plugin, Runnable runnable) {
      return new BukkitScheduler.BukkitTask(Bukkit.getScheduler().runTask(plugin, runnable));
   }

   @Override
   public GlobalTask runTaskLater(Plugin plugin, Runnable runnable, long delay) {
      return new BukkitScheduler.BukkitTask(Bukkit.getScheduler().runTaskLater(plugin, runnable, delay));
   }

   @Override
   public GlobalTask runTaskTimer(Plugin plugin, Runnable runnable, long delay, long period) {
      return new BukkitScheduler.BukkitTask(Bukkit.getScheduler().runTaskTimer(plugin, runnable, delay, period), true);
   }

   @Override
   public GlobalTask runTaskAsynchronously(Runnable runnable) {
      return new BukkitScheduler.BukkitTask(Bukkit.getScheduler().runTaskAsynchronously(this.plugin, runnable));
   }

   @Override
   public GlobalTask runTaskLaterAsynchronously(Runnable runnable, long delay) {
      return new BukkitScheduler.BukkitTask(Bukkit.getScheduler().runTaskLaterAsynchronously(this.plugin, runnable, delay));
   }

   @Override
   public GlobalTask runTaskTimerAsynchronously(Runnable runnable, long delay, long period) {
      return new BukkitScheduler.BukkitTask(Bukkit.getScheduler().runTaskTimerAsynchronously(this.plugin, runnable, delay, period), true);
   }

   @Override
   public GlobalTask runTaskAsynchronously(Plugin plugin, Runnable runnable) {
      return new BukkitScheduler.BukkitTask(Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable));
   }

   @Override
   public GlobalTask runTaskLaterAsynchronously(Plugin plugin, Runnable runnable, long delay) {
      return new BukkitScheduler.BukkitTask(Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, runnable, delay));
   }

   @Override
   public GlobalTask runTaskTimerAsynchronously(Plugin plugin, Runnable runnable, long delay, long period) {
      return new BukkitScheduler.BukkitTask(Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, runnable, delay, period), true);
   }

   @Override
   public void execute(Runnable runnable) {
      Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, runnable);
   }

   @Override
   public void cancel(int taskId) {
      Bukkit.getScheduler().cancelTask(taskId);
   }

   @Override
   public void cancelAll() {
      this.cancelAll(this.plugin);
   }

   @Override
   public void cancelAll(Plugin plugin) {
      Bukkit.getScheduler().cancelTasks(plugin);
   }

   @Override
   public String toString() {
      return "BukkitScheduler{plugin=" + this.plugin + '}';
   }

   @Generated
   BukkitScheduler(@NotNull Plugin plugin) {
      if (plugin == null) {
         throw new NullPointerException("plugin is marked non-null but is null");
      } else {
         this.plugin = plugin;
      }
   }

   private static final class BukkitTask implements GlobalTask {
      org.bukkit.scheduler.BukkitTask task;
      boolean repeating;

      public BukkitTask(org.bukkit.scheduler.BukkitTask task, boolean repeating) {
         this.task = task;
         this.repeating = repeating;
      }

      public BukkitTask(org.bukkit.scheduler.BukkitTask task) {
         this(task, false);
      }

      @Override
      public int getTaskId() {
         return this.task.getTaskId();
      }

      @Override
      public void cancel() {
         this.task.cancel();
      }

      @Override
      public boolean isCancelled() {
         return this.task.isCancelled();
      }

      @Override
      public Plugin getPlugin() {
         return this.task.getOwner();
      }

      @Override
      public boolean isRunning() {
         return Bukkit.getScheduler().isCurrentlyRunning(this.getTaskId());
      }

      @Generated
      @Override
      public boolean isRepeating() {
         return this.repeating;
      }
   }
}
