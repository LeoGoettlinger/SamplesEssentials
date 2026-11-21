package com.oheers.fish.api.events;

import com.oheers.fish.api.fishing.items.IFish;
import java.time.LocalDateTime;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;

public class EMFFishHuntEvent extends Event implements Cancellable {
   private static final HandlerList handlers = new HandlerList();
   private final IFish fish;
   private final Player player;
   private final LocalDateTime huntTime;
   private boolean cancel;

   @ApiStatus.Internal
   public EMFFishHuntEvent(@NonNull IFish fish, @NonNull Player player, @NonNull LocalDateTime huntTime) {
      this.fish = fish;
      this.player = player;
      this.huntTime = huntTime;
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

   @NonNull
   public Player getPlayer() {
      return this.player;
   }

   public boolean isCancelled() {
      return this.cancel;
   }

   public void setCancelled(boolean cancel) {
      this.cancel = cancel;
   }

   public LocalDateTime getHuntTime() {
      return this.huntTime;
   }
}
