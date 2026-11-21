package me.chancesd.pvpmanager.sdutils.scheduler;

import com.google.common.base.Preconditions;

public abstract class SDCancellableTask implements Runnable {
   private SDTask task;

   public void cancel() {
      this.task.cancel();
   }

   public boolean isCancelled() {
      return this.task.isCancelled();
   }

   public void setTask(SDTask task) {
      Preconditions.checkNotNull(task);
      this.task = task;
   }
}
