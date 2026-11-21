package com.lishid.openinv.internal;

import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;

public interface ISpecialEnderChest extends ISpecialInventory {
   @NotNull
   @Override
   default InventoryType getBukkitType() {
      return InventoryType.ENDER_CHEST;
   }
}
