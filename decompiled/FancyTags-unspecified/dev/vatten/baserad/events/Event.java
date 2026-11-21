package dev.vatten.baserad.events;

public abstract class Event {
   private boolean cancelled = false;

   public void cancel() {
      this.cancelled = true;
   }

   public boolean isCancelled() {
      return this.cancelled;
   }
}
