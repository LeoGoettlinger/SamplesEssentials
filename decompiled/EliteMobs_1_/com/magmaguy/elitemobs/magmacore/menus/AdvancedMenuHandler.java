package com.magmaguy.elitemobs.magmacore.menus;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

public class AdvancedMenuHandler {
   private static Map<Inventory, AdvancedMenu> advancedMenus;

   public AdvancedMenuHandler() {
      advancedMenus = new HashMap<>();
   }

   public static void addAdvancedMenu(Inventory inventory, AdvancedMenu advancedMenu) {
      advancedMenus.put(inventory, advancedMenu);
   }

   public static class AdvancedMenuListeners implements Listener {
      @EventHandler
      public void onInventoryInteraction(InventoryClickEvent event) {
         AdvancedMenu advancedMenu = AdvancedMenuHandler.advancedMenus.get(event.getInventory());
         if (advancedMenu != null) {
            event.setCancelled(true);
            if (event.getClickedInventory() == event.getView().getTopInventory()) {
               advancedMenu.run(event.getSlot());
            }
         }
      }

      @EventHandler
      public void onClose(InventoryCloseEvent event) {
         AdvancedMenuHandler.advancedMenus.remove(event.getInventory());
      }
   }
}
