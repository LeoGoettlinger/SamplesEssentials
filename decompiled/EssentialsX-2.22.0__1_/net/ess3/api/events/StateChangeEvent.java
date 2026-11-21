package net.ess3.api.events;

import net.ess3.api.IUser;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public abstract class StateChangeEvent extends Event implements Cancellable {
   final IUser affected;
   final IUser controller;
   private boolean cancelled = false;

   public StateChangeEvent(final IUser affected, final IUser controller) {
      this.affected = affected;
      this.controller = controller;
   }

   public StateChangeEvent(final boolean isAsync, final IUser affected, final IUser controller) {
      super(isAsync);
      this.affected = affected;
      this.controller = controller;
   }

   public IUser getAffected() {
      return this.affected;
   }

   public IUser getController() {
      return this.controller;
   }

   public boolean isCancelled() {
      return this.cancelled;
   }

   public void setCancelled(final boolean cancelled) {
      this.cancelled = cancelled;
   }
}
