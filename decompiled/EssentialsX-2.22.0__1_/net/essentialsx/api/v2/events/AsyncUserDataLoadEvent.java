package net.essentialsx.api.v2.events;

import net.ess3.api.IUser;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AsyncUserDataLoadEvent extends Event {
   private static final HandlerList handlers = new HandlerList();
   private final IUser user;
   private final String joinMessage;

   public AsyncUserDataLoadEvent(IUser user, String joinMessage) {
      super(true);
      this.user = user;
      this.joinMessage = joinMessage;
   }

   public IUser getUser() {
      return this.user;
   }

   public String getJoinMessage() {
      return this.joinMessage;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }
}
