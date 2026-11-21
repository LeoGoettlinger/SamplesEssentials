package com.lishid.openinv.event;

import com.google.errorprone.annotations.RestrictedApi;
import com.lishid.openinv.internal.ISpecialInventory;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.ApiStatus.Internal;

public class OpenPlayerSaveEvent extends PlayerSaveEvent {
   private static final HandlerList HANDLERS = new HandlerList();
   private final ISpecialInventory inventory;

   @RestrictedApi(
      explanation = "Constructor is not considered part of the API and may be subject to change.",
      allowedOnPath = ".*/com/lishid/openinv/event/OpenEvents.java"
   )
   @Internal
   OpenPlayerSaveEvent(@NotNull Player player, @NotNull ISpecialInventory inventory) {
      super(player);
      this.inventory = inventory;
   }

   @NotNull
   public ISpecialInventory getInventory() {
      return this.inventory;
   }

   @NotNull
   @Override
   public HandlerList getHandlers() {
      return HANDLERS;
   }

   public static HandlerList getHandlerList() {
      return HANDLERS;
   }
}
