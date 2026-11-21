package org.evenmorefish.fish.libs.dimensionfishing.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.evenmorefish.fish.libs.dimensionfishing.DimensionFishing;
import org.evenmorefish.fish.libs.dimensionfishing.common.TrackedHook;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public class VoidFishCaughtEvent extends PlayerEvent implements Cancellable {
   private static final HandlerList handlers = new HandlerList();
   private final TrackedHook hook;
   private boolean cancel;

   @ApiStatus.Internal
   public VoidFishCaughtEvent(@NotNull TrackedHook hook) {
      super(hook.getPlayer());
      this.hook = hook;
   }

   @NotNull
   public TrackedHook getTrackedHook() {
      return this.hook;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   @NotNull
   public HandlerList getHandlers() {
      return handlers;
   }

   public static boolean hasListeners() {
      if (handlers.getRegisteredListeners().length == 0) {
         DimensionFishing.getInstance().getLogger().warning("There are no listeners for VoidFishCaughtEvent.");
         return false;
      } else {
         return true;
      }
   }

   public boolean isCancelled() {
      return this.cancel;
   }

   public void setCancelled(boolean cancel) {
      this.cancel = cancel;
   }
}
