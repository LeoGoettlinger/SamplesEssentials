package net.ess3.api.events;

import net.ess3.api.IUser;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class NickChangeEvent extends StateChangeEvent implements Cancellable {
   private static final HandlerList handlers = new HandlerList();
   private final String newValue;

   public NickChangeEvent(final IUser affected, final IUser controller, final String value) {
      super(affected, controller);
      this.newValue = value;
   }

   public String getValue() {
      return this.newValue;
   }

   @Override
   public IUser getAffected() {
      return super.getAffected();
   }

   @Override
   public IUser getController() {
      return super.getController();
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }
}
