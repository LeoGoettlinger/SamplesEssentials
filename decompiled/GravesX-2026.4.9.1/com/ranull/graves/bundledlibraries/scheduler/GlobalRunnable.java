package com.ranull.graves.bundledlibraries.scheduler;

import java.util.Objects;
import lombok.Generated;

public abstract class GlobalRunnable implements Runnable {
   private final GlobalScheduler scheduler;
   private GlobalTask task;

   public synchronized boolean isCancelled() {
      return this.task != null && this.task.isCancelled();
   }

   public synchronized void cancel() {
      if (this.task != null) {
         this.task.cancel();
      }
   }

   private void checkTask() {
      if (this.task != null) {
         throw new GlobalRunnable.AlreadyScheduledException();
      }
   }

   public synchronized GlobalTask runTask() {
      this.checkTask();
      return this.task = this.scheduler.runTask(this);
   }

   public synchronized GlobalTask runTaskAsynchronously() {
      this.checkTask();
      return this.task = this.scheduler.runTaskAsynchronously(this);
   }

   public synchronized GlobalTask runTaskLater(long delay) {
      this.checkTask();
      return this.task = this.scheduler.runTaskLater(this, delay);
   }

   public synchronized GlobalTask runTaskLaterAsynchronously(long delay) {
      this.checkTask();
      return this.task = this.scheduler.runTaskLaterAsynchronously(this, delay);
   }

   public synchronized GlobalTask runTaskTimer(long delay, long period) {
      this.checkTask();
      return this.task = this.scheduler.runTaskTimer(this, delay, period);
   }

   public synchronized GlobalTask runTaskTimerAsynchronously(long delay, long period) {
      this.checkTask();
      return this.task = this.scheduler.runTaskTimerAsynchronously(this, delay, period);
   }

   public synchronized int getTaskId() {
      return Objects.requireNonNull(this.task, "Task not scheduled yet").getTaskId();
   }

   @Generated
   public GlobalRunnable(GlobalScheduler scheduler) {
      this.scheduler = scheduler;
   }

   private class AlreadyScheduledException extends RuntimeException {
      AlreadyScheduledException() {
         super("Task " + GlobalRunnable.this.task.getTaskId() + " is already scheduled");
      }
   }
}
