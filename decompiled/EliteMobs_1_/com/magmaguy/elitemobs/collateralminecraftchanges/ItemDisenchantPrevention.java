package com.magmaguy.elitemobs.collateralminecraftchanges;

import com.magmaguy.elitemobs.api.utils.EliteItemManager;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Result;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;

public class ItemDisenchantPrevention implements Listener {
   @EventHandler
   public void onDisenchant(InventoryClickEvent event) {
      if (event.getSlotType().equals(SlotType.RESULT)) {
         if (event.getInventory().getType().equals(InventoryType.GRINDSTONE)) {
            if (EliteItemManager.isEliteMobsItem(event.getInventory().getItem(0)) || EliteItemManager.isEliteMobsItem(event.getInventory().getItem(1))) {
               event.setResult(Result.DENY);
               event.setCancelled(true);
               event.getWhoClicked().sendMessage(ItemSettingsConfig.getPreventEliteItemDisenchantmentMessage());
               event.getWhoClicked().closeInventory();
            }
         }
      }
   }
}
