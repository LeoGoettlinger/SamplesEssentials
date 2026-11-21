package net.ess3.api.events;

import net.ess3.api.IUser;
import org.bukkit.event.HandlerList;

public class JailStatusChangeEvent extends StatusChangeEvent {
   private static final HandlerList handlers = new HandlerList();

   public JailStatusChangeEvent(final IUser affected, final IUser controller, final boolean value) {
      super(affected, controller, value);
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }
}
