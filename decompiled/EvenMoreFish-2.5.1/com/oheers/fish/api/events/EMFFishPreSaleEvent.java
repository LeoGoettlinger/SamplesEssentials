package com.oheers.fish.api.events;

import com.oheers.fish.api.economy.selling.SoldFish;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;

public class EMFFishPreSaleEvent extends PlayerEvent implements Cancellable {
   private static final HandlerList handlers = new HandlerList();
   private final SoldFish fish;
   private boolean cancel = false;

   @ApiStatus.Internal
   public EMFFishPreSaleEvent(@NonNull Player player, @NonNull SoldFish fish) {
      super(player);
      this.fish = fish;
      this.player = player;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   @NonNull
   public HandlerList getHandlers() {
      return handlers;
   }

   @NonNull
   public SoldFish getSoldFish() {
      return this.fish;
   }

   public boolean isCancelled() {
      return this.cancel;
   }

   public void setCancelled(boolean cancel) {
      this.cancel = cancel;
   }
}
