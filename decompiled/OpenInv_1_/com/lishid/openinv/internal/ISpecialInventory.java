package com.lishid.openinv.internal;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public interface ISpecialInventory {
   @NotNull
   Inventory getBukkitInventory();

   @NotNull
   InventoryType getBukkitType();

   void setPlayerOnline(@NotNull Player var1);

   @Deprecated(
      forRemoval = true,
      since = "5.1.11"
   )
   default void setPlayerOffline() {
   }

   default boolean isInUse() {
      return !this.getBukkitInventory().getViewers().isEmpty();
   }

   @NotNull
   HumanEntity getPlayer();
}
