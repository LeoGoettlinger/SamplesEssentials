package com.github.jikoo.openinv.lib.nahu.scheduler-wrapper.runnable;

import com.github.jikoo.openinv.lib.nahu.scheduler-wrapper.WrappedJavaPlugin;
import com.github.jikoo.openinv.lib.nahu.scheduler-wrapper.WrappedScheduler;
import com.github.jikoo.openinv.lib.nahu.scheduler-wrapper.task.WrappedTask;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

public abstract class WrappedRunnable implements Runnable {
   private WrappedTask wrappedTask;

   @NotNull
   public WrappedTask runTask(@NotNull WrappedScheduler scheduler) {
      this.checkNotYetScheduled();
      return this.setupTask(scheduler.runTask(this));
   }

   @NotNull
   public WrappedTask runTask(@NotNull WrappedJavaPlugin plugin) {
      return this.runTask(plugin.getScheduler());
   }

   @NotNull
   public WrappedTask runTaskAsynchronously(@NotNull WrappedScheduler scheduler) {
      this.checkNotYetScheduled();
      return this.setupTask(scheduler.runTaskAsynchronously(this));
   }

   @NotNull
   public WrappedTask runTaskAsynchronously(@NotNull WrappedJavaPlugin plugin) {
      return this.runTaskAsynchronously(plugin.getScheduler());
   }

   @NotNull
   public WrappedTask runTaskAtEntity(@NotNull WrappedScheduler scheduler, @NotNull Entity entity) {
      this.checkNotYetScheduled();
      return this.setupTask(scheduler.runTaskAtEntity(entity, this));
   }

   @NotNull
   public WrappedTask runTaskAtEntity(@NotNull WrappedJavaPlugin plugin, @NotNull Entity entity) {
      return this.runTaskAtEntity(plugin.getScheduler(), entity);
   }

   @NotNull
   public WrappedTask runTaskAtLocation(@NotNull WrappedScheduler scheduler, @NotNull Location location) {
      this.checkNotYetScheduled();
      return this.setupTask(scheduler.runTaskAtLocation(location, this));
   }

   @NotNull
   public WrappedTask runTaskAtLocation(@NotNull WrappedJavaPlugin plugin, @NotNull Location location) {
      return this.runTaskAtLocation(plugin.getScheduler(), location);
   }

   @NotNull
   public WrappedTask runTaskTimer(@NotNull WrappedScheduler scheduler, long delay, long period) {
      this.checkNotYetScheduled();
      return this.setupTask(scheduler.runTaskTimer(this, delay, period));
   }

   @NotNull
   public WrappedTask runTaskTimer(@NotNull WrappedJavaPlugin plugin, long delay, long period) {
      return this.runTaskTimer(plugin.getScheduler(), delay, period);
   }

   @NotNull
   public WrappedTask runTaskTimerAsynchronously(@NotNull WrappedScheduler scheduler, long delay, long period) {
      this.checkNotYetScheduled();
      return this.setupTask(scheduler.runTaskTimerAsynchronously(this, delay, period));
   }

   @NotNull
   public WrappedTask runTaskTimerAsynchronously(@NotNull WrappedJavaPlugin plugin, long delay, long period) {
      return this.runTaskTimerAsynchronously(plugin.getScheduler(), delay, period);
   }

   @NotNull
   public WrappedTask runTaskTimerAtEntity(@NotNull WrappedScheduler scheduler, @NotNull Entity entity, long delay, long period) {
      this.checkNotYetScheduled();
      return this.setupTask(scheduler.runTaskTimerAtEntity(entity, this, delay, period));
   }

   @NotNull
   public WrappedTask runTaskTimerAtEntity(@NotNull WrappedJavaPlugin plugin, @NotNull Entity entity, long delay, long period) {
      return this.runTaskTimerAtEntity(plugin.getScheduler(), entity, delay, period);
   }

   @NotNull
   public WrappedTask runTaskTimerAtLocation(@NotNull WrappedScheduler scheduler, @NotNull Location location, long delay, long period) {
      this.checkNotYetScheduled();
      return this.setupTask(scheduler.runTaskTimerAtLocation(location, this, delay, period));
   }

   @NotNull
   public WrappedTask runTaskTimerAtLocation(@NotNull WrappedJavaPlugin plugin, @NotNull Location location, long delay, long period) {
      return this.runTaskTimerAtLocation(plugin.getScheduler(), location, delay, period);
   }

   @NotNull
   public WrappedTask runTaskLater(@NotNull WrappedScheduler scheduler, long delay) {
      this.checkNotYetScheduled();
      return this.setupTask(scheduler.runTaskLater(this, delay));
   }

   @NotNull
   public WrappedTask runTaskLater(@NotNull WrappedJavaPlugin plugin, long delay) {
      return this.runTaskLater(plugin.getScheduler(), delay);
   }

   @NotNull
   public WrappedTask runTaskLaterAsynchronously(@NotNull WrappedScheduler scheduler, long delay) {
      this.checkNotYetScheduled();
      return this.setupTask(scheduler.runTaskLaterAsynchronously(this, delay));
   }

   @NotNull
   public WrappedTask runTaskLaterAsynchronously(@NotNull WrappedJavaPlugin plugin, long delay) {
      return this.runTaskLaterAsynchronously(plugin.getScheduler(), delay);
   }

   @NotNull
   public WrappedTask runTaskLaterAtEntity(@NotNull WrappedScheduler scheduler, @NotNull Entity entity, long delay) {
      this.checkNotYetScheduled();
      return this.setupTask(scheduler.runTaskLaterAtEntity(entity, this, delay));
   }

   @NotNull
   public WrappedTask runTaskLaterAtEntity(@NotNull WrappedJavaPlugin plugin, @NotNull Entity entity, long delay) {
      return this.runTaskLaterAtEntity(plugin.getScheduler(), entity, delay);
   }

   @NotNull
   public WrappedTask runTaskLaterAtLocation(@NotNull WrappedScheduler scheduler, @NotNull Location location, long delay) {
      this.checkNotYetScheduled();
      return this.setupTask(scheduler.runTaskLaterAtLocation(location, this, delay));
   }

   @NotNull
   public WrappedTask runTaskLaterAtLocation(@NotNull WrappedJavaPlugin plugin, @NotNull Location location, long delay) {
      return this.runTaskLaterAtLocation(plugin.getScheduler(), location, delay);
   }

   public synchronized boolean isCancelled() throws IllegalStateException {
      this.checkScheduled();
      return this.wrappedTask.isCancelled();
   }

   public synchronized void cancel() throws IllegalStateException {
      this.checkScheduled();
      this.wrappedTask.cancel();
   }

   private void checkScheduled() {
      if (this.wrappedTask == null) {
         throw new IllegalStateException("Not scheduled yet");
      }
   }

   private void checkNotYetScheduled() {
      if (this.wrappedTask != null) {
         throw new IllegalStateException("Task is already scheduled!");
      }
   }

   @NotNull
   private WrappedTask setupTask(@NotNull WrappedTask wrappedTask) {
      this.wrappedTask = wrappedTask;
      return wrappedTask;
   }
}
