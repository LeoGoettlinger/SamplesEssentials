package net.ess3.provider;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

public interface InventoryViewProvider extends Provider {
   Inventory getTopInventory(InventoryView view);

   void close(InventoryView view);

   Inventory getBottomInventory(InventoryView view);

   void setItem(InventoryView view, int slot, ItemStack item);
}
