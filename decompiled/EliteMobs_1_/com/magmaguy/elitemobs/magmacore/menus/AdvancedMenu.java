package com.magmaguy.elitemobs.magmacore.menus;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class AdvancedMenu {
   private final Player player;
   private final Map<Integer, MenuButton> advancedMenuItems = new HashMap<>();
   private final Inventory inventory;

   public AdvancedMenu(Player player, int size) {
      this.player = player;
      AdvancedMenuHandler.addAdvancedMenu(this.inventory = Bukkit.createInventory(player, size), this);
   }

   public void addAdvancedMenuItem(int slot, MenuButton advancedMenuItem) {
      this.advancedMenuItems.put(slot, advancedMenuItem);
      this.inventory.setItem(slot, advancedMenuItem.getItemStack());
   }

   public void removeAdvancedMenuItem(int slot, MenuButton advancedMenuItem) {
      this.advancedMenuItems.put(slot, advancedMenuItem);
      this.inventory.remove(advancedMenuItem.getItemStack());
   }

   public void openInventory(Player player) {
      player.openInventory(this.inventory);
   }

   public void run(int slot) {
      MenuButton advancedMenuItem = this.advancedMenuItems.get(slot);
      if (advancedMenuItem != null) {
         advancedMenuItem.onClick(this.player);
      }
   }
}
