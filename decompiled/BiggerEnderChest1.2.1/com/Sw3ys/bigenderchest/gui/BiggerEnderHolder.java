package com.Sw3ys.bigenderchest.gui;

import java.util.UUID;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class BiggerEnderHolder implements InventoryHolder {
   private final UUID target;
   private Inventory inventory;

   public BiggerEnderHolder(UUID target) {
      this.target = target;
   }

   public UUID getTarget() {
      return this.target;
   }

   public void setInventory(Inventory inventory) {
      this.inventory = inventory;
   }

   public Inventory getInventory() {
      return this.inventory;
   }
}
