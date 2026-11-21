package com.oheers.fish.api.events;

import com.oheers.fish.api.economy.selling.SoldFish;
import com.oheers.fish.api.fishing.items.IFish;
import java.time.LocalDateTime;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;

public class EMFFishSoldEvent extends PlayerEvent {
   private static final HandlerList handlers = new HandlerList();
   @NonNull
   private final IFish fish;
   private final double value;
   private final int quantity;
   @NonNull
   private final LocalDateTime sellTime;

   @ApiStatus.Internal
   public EMFFishSoldEvent(@NonNull Player player, @NonNull SoldFish fish) {
      super(player);
      this.value = fish.getFinalValue();
      this.fish = fish.getFish();
      this.quantity = fish.getQuantity();
      this.sellTime = fish.getSellTime();
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   @NonNull
   public HandlerList getHandlers() {
      return handlers;
   }

   @NonNull
   public IFish getFish() {
      return this.fish;
   }

   public double getValue() {
      return this.value;
   }

   public int getQuantity() {
      return this.quantity;
   }

   @NonNull
   public LocalDateTime getSellTime() {
      return this.sellTime;
   }
}
