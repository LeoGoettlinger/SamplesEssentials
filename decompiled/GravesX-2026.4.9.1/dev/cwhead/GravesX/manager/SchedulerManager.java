package dev.cwhead.GravesX.manager;

import com.ranull.graves.bundledlibraries.scheduler.GlobalScheduler;
import com.ranull.graves.bundledlibraries.scheduler.GlobalTask;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class SchedulerManager {
   private final Plugin plugin;
   private final GlobalScheduler scheduler;
   private final SchedulerManager.Returnable returnable;

   public SchedulerManager(@NotNull Plugin plugin) {
      this.plugin = plugin;
      this.scheduler = GlobalScheduler.getScheduler(plugin);
      this.returnable = new SchedulerManager.Returnable(this, this.scheduler);
   }

   @NotNull
   public GlobalScheduler getScheduler() {
      return this.scheduler;
   }

   @NotNull
   public Plugin getPlugin() {
      return this.plugin;
   }

   @NotNull
   public SchedulerManager.Returnable returnable() {
      return this.returnable;
   }

   public boolean isGlobalThread() {
      return this.scheduler.isGlobalThread();
   }

   public boolean isTickThread() {
      return this.scheduler.isTickThread();
   }

   public boolean isEntityThread(@NotNull Entity entity) {
      return this.scheduler.isEntityThread(entity);
   }

   public boolean isRegionThread(@NotNull Location location) {
      return this.scheduler.isRegionThread(location);
   }

   @NotNull
   public <T> Future<T> callSyncMethod(@NotNull Callable<T> task) {
      return this.scheduler.callSyncMethod(task);
   }

   public void runTask(@NotNull Runnable runnable) {
      this.scheduler.runTask(runnable);
   }

   @Deprecated
   public void runTask(@NotNull Plugin plugin, @NotNull Runnable runnable) {
      this.scheduler.runTask(plugin, runnable);
   }

   public void runTaskLater(@NotNull Runnable runnable, long delay) {
      this.scheduler.runTaskLater(runnable, delay);
   }

   @Deprecated
   public void runTaskLater(@NotNull Plugin plugin, @NotNull Runnable runnable, long delay) {
      this.scheduler.runTaskLater(plugin, runnable, delay);
   }

   public void runTaskTimer(@NotNull Runnable runnable, long delay, long period) {
      this.scheduler.runTaskTimer(runnable, delay, period);
   }

   @Deprecated
   public void runTaskTimer(@NotNull Plugin plugin, @NotNull Runnable runnable, long delay, long period) {
      this.scheduler.runTaskTimer(plugin, runnable, delay, period);
   }

   public void runTask(@NotNull Location location, @NotNull Runnable runnable) {
      this.scheduler.runTask(location, runnable);
   }

   public void runTaskLater(@NotNull Location location, @NotNull Runnable runnable, long delay) {
      this.scheduler.runTaskLater(location, runnable, delay);
   }

   public void runTaskTimer(@NotNull Location location, @NotNull Runnable runnable, long delay, long period) {
      this.scheduler.runTaskTimer(location, runnable, delay, period);
   }

   public void runTask(@NotNull Entity entity, @NotNull Runnable runnable) {
      this.scheduler.runTask(entity, runnable);
   }

   public void runTaskLater(@NotNull Entity entity, @NotNull Runnable runnable, long delay) {
      this.scheduler.runTaskLater(entity, runnable, delay);
   }

   public void runTaskTimer(@NotNull Entity entity, @NotNull Runnable runnable, long delay, long period) {
      this.scheduler.runTaskTimer(entity, runnable, delay, period);
   }

   public void runTaskAsynchronously(@NotNull Runnable runnable) {
      this.scheduler.runTaskAsynchronously(runnable);
   }

   @Deprecated
   public void runTaskAsynchronously(@NotNull Plugin plugin, @NotNull Runnable runnable) {
      this.scheduler.runTaskAsynchronously(plugin, runnable);
   }

   public void runTaskLaterAsynchronously(@NotNull Runnable runnable, long delay) {
      this.scheduler.runTaskLaterAsynchronously(runnable, delay);
   }

   @Deprecated
   public void runTaskLaterAsynchronously(@NotNull Plugin plugin, @NotNull Runnable runnable, long delay) {
      this.scheduler.runTaskLaterAsynchronously(plugin, runnable, delay);
   }

   public void runTaskTimerAsynchronously(@NotNull Runnable runnable, long delay, long period) {
      this.scheduler.runTaskTimerAsynchronously(runnable, delay, period);
   }

   @Deprecated
   public void runTaskTimerAsynchronously(@NotNull Plugin plugin, @NotNull Runnable runnable, long delay, long period) {
      this.scheduler.runTaskTimerAsynchronously(plugin, runnable, delay, period);
   }

   @Deprecated
   public void scheduleSyncDelayedTask(@NotNull Runnable runnable) {
      this.scheduler.scheduleSyncDelayedTask(runnable);
   }

   @Deprecated
   public void scheduleSyncDelayedTask(@NotNull Runnable runnable, long delay) {
      this.scheduler.scheduleSyncDelayedTask(runnable, delay);
   }

   @Deprecated
   public void scheduleSyncRepeatingTask(@NotNull Runnable runnable, long delay, long period) {
      this.scheduler.scheduleSyncRepeatingTask(runnable, delay, period);
   }

   public void execute(@NotNull Runnable runnable) {
      this.scheduler.execute(runnable);
   }

   public void execute(@NotNull Location location, @NotNull Runnable runnable) {
      this.scheduler.execute(location, runnable);
   }

   public void execute(@NotNull Entity entity, @NotNull Runnable runnable) {
      this.scheduler.execute(entity, runnable);
   }

   public void cancel(@Nullable GlobalTask task) {
      if (task != null) {
         task.cancel();
      }
   }

   public void cancel(int taskId) {
      this.scheduler.cancel(taskId);
   }

   public void cancelAll() {
      this.scheduler.cancelAll();
   }

   public void cancelAll(@NotNull Plugin plugin) {
      this.scheduler.cancelAll(plugin);
   }

   public static final class Returnable {
      private final SchedulerManager manager;
      private final GlobalScheduler scheduler;

      private Returnable(@NotNull SchedulerManager manager, @NotNull GlobalScheduler scheduler) {
         this.manager = manager;
         this.scheduler = scheduler;
      }

      @NotNull
      public <T> Future<T> callSyncMethod(@NotNull Callable<T> task) {
         return this.scheduler.callSyncMethod(task);
      }

      @NotNull
      public SchedulerManager.Task wrap(@NotNull GlobalTask task) {
         return new SchedulerManager.Task(this.manager, task);
      }

      @Nullable
      public SchedulerManager.Task wrapNullable(@Nullable GlobalTask task) {
         return task == null ? null : new SchedulerManager.Task(this.manager, task);
      }

      @NotNull
      public SchedulerManager.Task runTask(@NotNull Runnable runnable) {
         return this.wrap(this.scheduler.runTask(runnable));
      }

      @Deprecated
      @NotNull
      public SchedulerManager.Task runTask(@NotNull Plugin plugin, @NotNull Runnable runnable) {
         return this.wrap(this.scheduler.runTask(plugin, runnable));
      }

      @NotNull
      public SchedulerManager.Task runTaskLater(@NotNull Runnable runnable, long delay) {
         return this.wrap(this.scheduler.runTaskLater(runnable, delay));
      }

      @Deprecated
      @NotNull
      public SchedulerManager.Task runTaskLater(@NotNull Plugin plugin, @NotNull Runnable runnable, long delay) {
         return this.wrap(this.scheduler.runTaskLater(plugin, runnable, delay));
      }

      @NotNull
      public SchedulerManager.Task runTaskTimer(@NotNull Runnable runnable, long delay, long period) {
         return this.wrap(this.scheduler.runTaskTimer(runnable, delay, period));
      }

      @Deprecated
      @NotNull
      public SchedulerManager.Task runTaskTimer(@NotNull Plugin plugin, @NotNull Runnable runnable, long delay, long period) {
         return this.wrap(this.scheduler.runTaskTimer(plugin, runnable, delay, period));
      }

      @NotNull
      public SchedulerManager.Task runTask(@NotNull Location location, @NotNull Runnable runnable) {
         return this.wrap(this.scheduler.runTask(location, runnable));
      }

      @NotNull
      public SchedulerManager.Task runTaskLater(@NotNull Location location, @NotNull Runnable runnable, long delay) {
         return this.wrap(this.scheduler.runTaskLater(location, runnable, delay));
      }

      @NotNull
      public SchedulerManager.Task runTaskTimer(@NotNull Location location, @NotNull Runnable runnable, long delay, long period) {
         return this.wrap(this.scheduler.runTaskTimer(location, runnable, delay, period));
      }

      @NotNull
      public SchedulerManager.Task runTask(@NotNull Entity entity, @NotNull Runnable runnable) {
         return this.wrap(this.scheduler.runTask(entity, runnable));
      }

      @NotNull
      public SchedulerManager.Task runTaskLater(@NotNull Entity entity, @NotNull Runnable runnable, long delay) {
         return this.wrap(this.scheduler.runTaskLater(entity, runnable, delay));
      }

      @NotNull
      public SchedulerManager.Task runTaskTimer(@NotNull Entity entity, @NotNull Runnable runnable, long delay, long period) {
         return this.wrap(this.scheduler.runTaskTimer(entity, runnable, delay, period));
      }

      @NotNull
      public SchedulerManager.Task runTaskAsynchronously(@NotNull Runnable runnable) {
         return this.wrap(this.scheduler.runTaskAsynchronously(runnable));
      }

      @Deprecated
      @NotNull
      public SchedulerManager.Task runTaskAsynchronously(@NotNull Plugin plugin, @NotNull Runnable runnable) {
         return this.wrap(this.scheduler.runTaskAsynchronously(plugin, runnable));
      }

      @NotNull
      public SchedulerManager.Task runTaskLaterAsynchronously(@NotNull Runnable runnable, long delay) {
         return this.wrap(this.scheduler.runTaskLaterAsynchronously(runnable, delay));
      }

      @Deprecated
      @NotNull
      public SchedulerManager.Task runTaskLaterAsynchronously(@NotNull Plugin plugin, @NotNull Runnable runnable, long delay) {
         return this.wrap(this.scheduler.runTaskLaterAsynchronously(plugin, runnable, delay));
      }

      @NotNull
      public SchedulerManager.Task runTaskTimerAsynchronously(@NotNull Runnable runnable, long delay, long period) {
         return this.wrap(this.scheduler.runTaskTimerAsynchronously(runnable, delay, period));
      }

      @Deprecated
      @NotNull
      public SchedulerManager.Task runTaskTimerAsynchronously(@NotNull Plugin plugin, @NotNull Runnable runnable, long delay, long period) {
         return this.wrap(this.scheduler.runTaskTimerAsynchronously(plugin, runnable, delay, period));
      }

      @Deprecated
      @NotNull
      public SchedulerManager.Task scheduleSyncDelayedTask(@NotNull Runnable runnable) {
         return this.wrap(this.scheduler.scheduleSyncDelayedTask(runnable));
      }

      @Deprecated
      @NotNull
      public SchedulerManager.Task scheduleSyncDelayedTask(@NotNull Runnable runnable, long delay) {
         return this.wrap(this.scheduler.scheduleSyncDelayedTask(runnable, delay));
      }

      @Deprecated
      @NotNull
      public SchedulerManager.Task scheduleSyncRepeatingTask(@NotNull Runnable runnable, long delay, long period) {
         return this.wrap(this.scheduler.scheduleSyncRepeatingTask(runnable, delay, period));
      }
   }

   public static final class Task {
      private final SchedulerManager manager;
      private final GlobalTask task;

      private Task(@NotNull SchedulerManager manager, @NotNull GlobalTask task) {
         this.manager = manager;
         this.task = task;
      }

      @NotNull
      public SchedulerManager getSchedulerManager() {
         return this.manager;
      }

      @NotNull
      public GlobalTask getHandle() {
         return this.task;
      }

      public int getTaskId() {
         return this.task.getTaskId();
      }

      public void cancel() {
         this.task.cancel();
      }

      public boolean isCancelled() {
         return this.task.isCancelled();
      }

      @NotNull
      public Plugin getPlugin() {
         return this.task.getPlugin();
      }

      public boolean isRunning() {
         return this.task.isRunning();
      }

      public boolean isRepeating() {
         return this.task.isRepeating();
      }
   }
}
