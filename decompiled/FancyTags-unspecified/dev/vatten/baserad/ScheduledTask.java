package dev.vatten.baserad;

public abstract class ScheduledTask<T> {
   protected T task;

   public ScheduledTask(T task) {
      this.task = task;
   }

   public abstract void cancel();

   public abstract boolean isCancelled();
}
