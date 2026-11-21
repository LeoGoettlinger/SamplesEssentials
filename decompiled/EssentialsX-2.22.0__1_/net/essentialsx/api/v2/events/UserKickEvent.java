package net.essentialsx.api.v2.events;

import com.earth2me.essentials.IUser;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class UserKickEvent extends Event implements Cancellable {
   private static final HandlerList handlers = new HandlerList();
   private final IUser kicked;
   private final IUser kicker;
   private String reason;
   private boolean cancelled;

   public UserKickEvent(IUser kicked, IUser kicker, String reason) {
      this.kicked = kicked;
      this.kicker = kicker;
      this.reason = reason;
   }

   public IUser getKicked() {
      return this.kicked;
   }

   public IUser getKicker() {
      return this.kicker;
   }

   public String getReason() {
      return this.reason;
   }

   public void setReason(String reason) {
      this.reason = reason;
   }

   public boolean isCancelled() {
      return this.cancelled;
   }

   public void setCancelled(boolean cancelled) {
      this.cancelled = cancelled;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }
}
