package com.lishid.openinv.event;

import com.google.errorprone.annotations.RestrictedApi;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.ApiStatus.Internal;

public class PlayerSaveEvent extends PlayerEvent implements Cancellable {
   private static final HandlerList HANDLERS = new HandlerList();
   private boolean cancelled = false;

   @RestrictedApi(
      explanation = "Constructor is not considered part of the API and may be subject to change.",
      allowedOnPath = ".*/com/lishid/openinv/event/(OpenPlayerSaveEvent|OpenEvents).java"
   )
   @Internal
   PlayerSaveEvent(@NotNull Player player) {
      super(player);
   }

   public boolean isCancelled() {
      return this.cancelled;
   }

   public void setCancelled(boolean cancel) {
      this.cancelled = cancel;
   }

   @NotNull
   public HandlerList getHandlers() {
      return HANDLERS;
   }

   public static HandlerList getHandlerList() {
      return HANDLERS;
   }
}
