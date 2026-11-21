package com.magmaguy.elitemobs.playerdata.statusscreen;

import java.util.HashSet;
import java.util.Set;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

public final class StatusInventorySafety implements Listener {
   private static final Set<Inventory> protectedInventories = new HashSet<>();

   static void protect(Inventory inventory) {
      protectedInventories.add(inventory);
   }

   public static void shutdown() {
      protectedInventories.clear();
   }

   @EventHandler
   public void onInventoryDrag(InventoryDragEvent event) {
      Inventory topInventory = event.getView().getTopInventory();
      if (protectedInventories.contains(topInventory)) {
         if (event.getRawSlots().stream().anyMatch(slot -> slot < topInventory.getSize())) {
            event.setCancelled(true);
         }
      }
   }

   @EventHandler
   public void onInventoryClose(InventoryCloseEvent event) {
      protectedInventories.remove(event.getInventory());
   }
}
