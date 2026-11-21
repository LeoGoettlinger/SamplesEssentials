package com.lishid.openinv.internal;

import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;

public interface ISpecialPlayerInventory extends ISpecialInventory {
   @NotNull
   @Override
   default InventoryType getBukkitType() {
      return InventoryType.PLAYER;
   }
}
