package com.magmaguy.elitemobs.menus;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.CustomModelsConfig;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.config.menus.premade.BuyOrSellMenuConfig;
import com.magmaguy.elitemobs.config.menus.premade.CustomShopMenuConfig;
import com.magmaguy.elitemobs.items.EliteItemLore;
import com.magmaguy.elitemobs.items.ItemTagger;
import com.magmaguy.elitemobs.items.ItemWorthCalculator;
import com.magmaguy.elitemobs.items.customenchantments.SoulbindEnchantment;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import com.magmaguy.elitemobs.utils.CustomModelAdder;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
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
import org.bukkit.scheduler.BukkitRunnable;

public class CustomShopMenu {
   public static Set<UUID> cooldownPlayers = new HashSet<>();

   public static void shutdown() {
      cooldownPlayers.clear();
      CustomShopMenu.CustomShopMenuEvents.menus.clear();
   }

   public static void customShopInitializer(Player player) {
      if (EconomySettingsConfig.isEnableEconomy()) {
         BuyOrSellMenu.constructBuyOrSellMenu(player, BuyOrSellMenuConfig.BUY_CUSTOM_ITEM, () -> customShopConstructor(player));
      }
   }

   public static void customShopConstructor(Player player) {
      String menuName = CustomShopMenuConfig.shopName;
      if (DefaultConfig.useResourcePackModels()) {
         menuName = ChatColor.WHITE + "\udb83\udef1\udb83\ude06\udb83\udef5          " + menuName;
      }

      Inventory shopInventory = Bukkit.createInventory(player, 54, menuName);
      populateShop(shopInventory, player);
      player.openInventory(shopInventory);
      CustomShopMenu.CustomShopMenuEvents.menus.add(shopInventory);
   }

   private static void populateShop(Inventory shopInventory, Player player) {
      ItemStack rerollButton = CustomShopMenuConfig.rerollItem;
      if (DefaultConfig.useResourcePackModels()) {
         rerollButton.setType(Material.PAPER);
         CustomModelAdder.addCustomModel(rerollButton, CustomModelsConfig.goldenQuestionMark);
      }

      shopInventory.setItem(CustomShopMenuConfig.rerollSlot, rerollButton);
      shopContents(shopInventory, player);
   }

   private static void shopContents(Inventory shopInventory, Player player) {
      Random random = new Random();

      for (int i : CustomShopMenuConfig.storeSlots) {
         int itemEntryIndex = random.nextInt(CustomItem.getCustomItemStackShopList().size());
         ItemStack itemStack = CustomItem.getCustomItemStackShopList().get(itemEntryIndex).clone();
         SoulbindEnchantment.addEnchantment(itemStack, player);
         new EliteItemLore(itemStack, true);
         shopInventory.setItem(i, itemStack);
      }
   }

   public static class CustomShopMenuEvents implements Listener {
      public static HashSet<Inventory> menus = new HashSet<>();

      @EventHandler
      public void onClick(final InventoryClickEvent event) {
         if (EliteMenu.isEliteMenu(event, menus)) {
            event.setCancelled(true);
            if (EliteMenu.isTopMenu(event)) {
               if (SharedShopElements.itemNullPointerPrevention(event)) {
                  if (event.getSlot() == CustomShopMenuConfig.rerollSlot) {
                     if (CustomShopMenu.cooldownPlayers.contains(event.getWhoClicked().getUniqueId())) {
                        event.setCancelled(true);
                     } else {
                        CustomShopMenu.cooldownPlayers.add(event.getWhoClicked().getUniqueId());
                        (new BukkitRunnable() {
                           public void run() {
                              CustomShopMenu.cooldownPlayers.remove(event.getWhoClicked().getUniqueId());
                           }
                        }).runTaskLater(MetadataHandler.PLUGIN, 40L);
                        CustomShopMenu.populateShop(event.getInventory(), Bukkit.getPlayer(event.getWhoClicked().getUniqueId()));
                        event.setCancelled(true);
                     }
                  } else if (CustomShopMenuConfig.storeSlots.contains(event.getSlot()) && ItemTagger.isEliteItem(event.getCurrentItem())) {
                     Player player = (Player)event.getWhoClicked();
                     ItemStack itemStack = event.getCurrentItem();
                     String itemDisplayName = event.getCurrentItem().getItemMeta().getDisplayName();
                     double itemValue = ItemWorthCalculator.determineItemWorth(itemStack, player);
                     boolean inventoryHasFreeSlots = false;

                     for (ItemStack iteratedStack : player.getInventory().getStorageContents()) {
                        if (iteratedStack == null) {
                           inventoryHasFreeSlots = true;
                           break;
                        }
                     }

                     if (!inventoryHasFreeSlots) {
                        player.sendMessage(CustomShopMenuConfig.messageFullInventory);
                        player.closeInventory();
                     } else if (SharedShopElements.purchase(player, itemStack, itemValue)) {
                        CustomShopMenu.populateShop(event.getInventory(), Bukkit.getPlayer(event.getWhoClicked().getUniqueId()));
                        SharedShopElements.buyMessage(player, itemDisplayName, itemValue);
                     }
                  } else {
                     event.setCancelled(true);
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
