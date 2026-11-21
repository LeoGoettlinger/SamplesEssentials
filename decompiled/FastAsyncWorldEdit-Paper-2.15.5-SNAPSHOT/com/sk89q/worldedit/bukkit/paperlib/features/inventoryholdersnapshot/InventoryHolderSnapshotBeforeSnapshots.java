package com.sk89q.worldedit.bukkit.paperlib.features.inventoryholdersnapshot;

import org.bukkit.inventory.Inventory;

public class InventoryHolderSnapshotBeforeSnapshots implements InventoryHolderSnapshot {
   @Override
   public InventoryHolderSnapshotResult getHolder(Inventory inventory, boolean useSnapshot) {
      return new InventoryHolderSnapshotResult(false, inventory.getHolder());
   }
}
