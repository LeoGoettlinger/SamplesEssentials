package net.ess3.api.events;

import com.earth2me.essentials.Trade;
import net.ess3.api.IUser;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class UserWarpEvent extends Event implements Cancellable {
   private static final HandlerList handlers = new HandlerList();
   private final IUser user;
   private final Trade trade;
   private String warp;
   private boolean cancelled = false;

   public UserWarpEvent(final IUser user, final String warp, final Trade trade) {
      super(!Bukkit.getServer().isPrimaryThread());
      this.user = user;
      this.warp = warp;
      this.trade = trade;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   public IUser getUser() {
      return this.user;
   }

   public String getWarp() {
      return this.warp;
   }

   public void setWarp(final String warp) {
      this.warp = warp;
   }

   public Trade getTrade() {
      return this.trade;
   }

   public boolean isCancelled() {
      return this.cancelled;
   }

   public void setCancelled(final boolean b) {
      this.cancelled = b;
   }

   public HandlerList getHandlers() {
      return handlers;
   }
}
