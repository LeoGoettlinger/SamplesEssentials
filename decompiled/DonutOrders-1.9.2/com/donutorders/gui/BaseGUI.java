package com.donutorders.gui;

import com.donutorders.util.ItemUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public abstract class BaseGUI {
   protected final Inventory inventory;

   protected BaseGUI(Inventory inventory) {
      this.inventory = inventory;
   }

   public abstract void handleClick(Player var1, int var2, ItemStack var3, ClickType var4);

   public Inventory getInventory() {
      return this.inventory;
   }

   public static ItemStack filler() {
      return ItemUtils.filler(Material.GRAY_STAINED_GLASS_PANE);
   }

   protected void fillEmpty() {
      ItemStack fill = filler();

      for (int i = 0; i < this.inventory.getSize(); i++) {
         if (this.inventory.getItem(i) == null) {
            this.inventory.setItem(i, fill);
         }
      }
   }
}
