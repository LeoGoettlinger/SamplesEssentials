package com.magmaguy.elitemobs.menus;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.CustomModelsConfig;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.menus.premade.BuyOrSellMenuConfig;
import com.magmaguy.elitemobs.utils.CustomModelAdder;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class BuyOrSellMenu {
   public static void constructBuyOrSellMenu(Player player, ItemStack buyItemStack, Runnable buyAction) {
      String inventoryName = BuyOrSellMenuConfig.SHOP_NAME;
      if (DefaultConfig.useResourcePackModels()) {
         inventoryName = ChatColor.WHITE + "\udb83\udef1\udb83\ude07\udb83\udef5       " + inventoryName;
      }

      Inventory shopInventory = Bukkit.createInventory(player, 18, inventoryName);
      BuyOrSellMenu.BuyOrSellMenuEvents.menus.put(shopInventory, buyAction);
      ItemStack info = BuyOrSellMenuConfig.INFORMATION_ITEM;
      if (DefaultConfig.useResourcePackModels()) {
         info.setType(Material.PAPER);
         ItemMeta itemMeta = info.getItemMeta();
         itemMeta.setCustomModelData(MetadataHandler.signatureID);
         info.setItemMeta(itemMeta);
         CustomModelAdder.addCustomModel(info, CustomModelsConfig.goldenQuestionMark);
      }

      shopInventory.setItem(BuyOrSellMenuConfig.INFORMATION_SLOT, info);
      shopInventory.setItem(BuyOrSellMenuConfig.SELL_SLOT, BuyOrSellMenuConfig.SELL_ITEM);
      shopInventory.setItem(BuyOrSellMenuConfig.BUY_SLOT, buyItemStack);
      player.openInventory(shopInventory);
   }

   public static class BuyOrSellMenuEvents implements Listener {
      private static final Map<Inventory, Runnable> menus = new HashMap<>();

      public static void shutdown() {
         menus.clear();
      }

      @EventHandler
      public void onInventoryInteraction(InventoryClickEvent event) {
         if (EliteMenu.isEliteMenu(event, menus.keySet())) {
            event.setCancelled(true);
            if (EliteMenu.isTopMenu(event)) {
               if (SharedShopElements.itemNullPointerPrevention(event)) {
                  if (event.getSlot() != BuyOrSellMenuConfig.INFORMATION_SLOT) {
                     if (event.getSlot() == BuyOrSellMenuConfig.BUY_SLOT) {
                        Runnable buyAction = menus.remove(event.getInventory());
                        buyAction.run();
                     } else {
                        if (event.getSlot() == BuyOrSellMenuConfig.SELL_SLOT) {
                           menus.remove(event.getInventory());
                           SellMenu sellMenu = new SellMenu();
                           sellMenu.constructSellMenu((Player)event.getWhoClicked());
                        }
                     }
                  }
               }
            }
         }
      }

      @EventHandler
      public void onClose(InventoryCloseEvent event) {
         menus.remove(event.getInventory());
      }
   }
}
