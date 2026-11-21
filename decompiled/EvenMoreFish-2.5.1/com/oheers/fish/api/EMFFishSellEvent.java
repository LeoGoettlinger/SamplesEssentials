package com.oheers.fish.api;

import com.oheers.fish.selling.SoldFish;
import java.time.LocalDateTime;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;

@Deprecated(
   forRemoval = true
)
public class EMFFishSellEvent extends Event implements Cancellable {
   private static final HandlerList handlers = new HandlerList();
   private final List<SoldFish> soldFish;
   private final Player player;
   private final double worth;
   private final LocalDateTime soldTime;
   private boolean cancel;

   @ApiStatus.Internal
   public EMFFishSellEvent(@NonNull SoldFish soldFish, @NonNull Player player, double worth, @NonNull LocalDateTime soldTime) {
      this.soldFish = List.of(soldFish);
      this.player = player;
      this.worth = worth;
      this.soldTime = soldTime;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   @NonNull
   public HandlerList getHandlers() {
      return handlers;
   }

   @NonNull
   public List<SoldFish> getSoldFish() {
      return this.soldFish;
   }

   @NonNull
   public Player getPlayer() {
      return this.player;
   }

   public double getWorth() {
      return this.worth;
   }

   public boolean isCancelled() {
      return this.cancel;
   }

   public void setCancelled(boolean cancel) {
      this.cancel = cancel;
   }

   public LocalDateTime getSoldTime() {
      return this.soldTime;
   }
}
