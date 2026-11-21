package me.chancesd.pvpmanager.sdutils.tasks;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import me.chancesd.pvpmanager.sdutils.scheduler.ScheduleUtils;

public abstract class PausableTask implements Runnable {
   private long finishTime;
   private long duration;
   private ScheduledFuture<?> task;
   private boolean paused;
   private long pausedAt;
   private boolean expired;

   protected PausableTask(long duration) {
      this.finishTime = System.currentTimeMillis() + duration;
      this.duration = duration;
      this.scheduleTask(duration);
   }

   protected abstract void onComplete();

   @Override
   public final void run() {
      this.expired = true;
      this.onComplete();
   }

   protected void scheduleTask(long timeMs) {
      this.task = ScheduleUtils.runAsyncLater(this, timeMs, TimeUnit.MILLISECONDS);
   }

   public synchronized void pause() {
      if (!this.paused) {
         this.task.cancel(false);
         this.pausedAt = System.currentTimeMillis();
         this.paused = true;
      }
   }

   public synchronized void resume() {
      if (this.paused) {
         long timeLeft = this.getTimeleft();
         this.scheduleTask(timeLeft);
         this.finishTime = System.currentTimeMillis() + timeLeft;
         this.paused = false;
      }
   }

   public synchronized void cancel() {
      if (this.task != null) {
         this.task.cancel(false);
      }
   }

   public boolean isExpired() {
      return this.expired;
   }

   public long getTimeleft() {
      if (this.paused) {
         long pauseDuration = System.currentTimeMillis() - this.pausedAt;
         return Math.max(0L, this.finishTime + pauseDuration - System.currentTimeMillis());
      } else {
         return Math.max(0L, this.finishTime - System.currentTimeMillis());
      }
   }

   public long getFinishTime() {
      return this.finishTime;
   }

   public long getDuration() {
      return this.duration;
   }
}
