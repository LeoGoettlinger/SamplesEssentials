package net.essentialsx.api.v2.events;

import net.ess3.api.IUser;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TeleportRequestResponseEvent extends Event implements Cancellable {
   private static final HandlerList handlers = new HandlerList();
   private final IUser requestee;
   private final IUser requester;
   private final com.earth2me.essentials.IUser.TpaRequest tpaRequest;
   private final boolean accept;
   private boolean canceled = false;

   public TeleportRequestResponseEvent(IUser requestee, IUser requester, com.earth2me.essentials.IUser.TpaRequest tpaRequest, boolean accept) {
      this.requestee = requestee;
      this.requester = requester;
      this.tpaRequest = tpaRequest;
      this.accept = accept;
   }

   public IUser getRequestee() {
      return this.requestee;
   }

   public IUser getRequester() {
      return this.requester;
   }

   public com.earth2me.essentials.IUser.TpaRequest getTpaRequest() {
      return this.tpaRequest;
   }

   public boolean isAccept() {
      return this.accept;
   }

   public boolean isDeny() {
      return !this.isAccept();
   }

   public boolean isCancelled() {
      return this.canceled;
   }

   public void setCancelled(boolean cancel) {
      this.canceled = cancel;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }
}
