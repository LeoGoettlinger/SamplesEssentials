package com.Sw3ys.bigenderchest.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class SelectorHolder implements InventoryHolder {
   private final int page;
   private Inventory inventory;

   public SelectorHolder(int page) {
      this.page = page;
   }

   public int getPage() {
      return this.page;
   }

   public void setInventory(Inventory inventory) {
      this.inventory = inventory;
   }

   public Inventory getInventory() {
      return this.inventory;
   }
}
