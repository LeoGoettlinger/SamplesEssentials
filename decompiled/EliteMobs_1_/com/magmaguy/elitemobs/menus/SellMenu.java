package com.magmaguy.elitemobs.menus;

import com.magmaguy.elitemobs.api.utils.EliteItemManager;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.config.menus.premade.SellMenuConfig;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import com.magmaguy.elitemobs.items.ItemWorthCalculator;
import com.magmaguy.elitemobs.items.customenchantments.SoulbindEnchantment;
import com.magmaguy.elitemobs.magmacore.util.ItemStackGenerator;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SellMenu extends EliteMenu implements Listener {
   private static final Map<Inventory, SellMenu.SaleSession> sessions = new IdentityHashMap<>();
   public static Set<Inventory> inventories = new HashSet<>();
   private static final Set<Inventory> selling = Collections.newSetFromMap(new IdentityHashMap<>());
   private static final Map<UUID, List<SellMenu.HeldSale>> uncertainSales = new HashMap<>();

   public static void shutdown() {
      for (Inventory inventory : List.copyOf(inventories)) {
         if (inventory.getHolder() instanceof Player player) {
            close(player, inventory);
            if (player.getOpenInventory().getTopInventory() == inventory) {
               player.closeInventory();
            }
         }
      }

      if (!uncertainSales.isEmpty()) {
         Logger.warn("Unconfirmed sale stock retained for manual reconciliation before restart: " + uncertainSales);
      }
   }

   private static List<Integer> slotsFor(Inventory inventory) {
      SellMenu.SaleSession session = sessions.get(inventory);
      return session == null ? List.of() : session.layout.inputs();
   }

   private static void close(Player player, Inventory inventory) {
      if (inventories.remove(inventory)) {
         SellMenu.SaleSession session = sessions.remove(inventory);
         if (session != null) {
            EliteMenu.cancel(player, inventory, player.getInventory(), session.layout.inputs());
         }
      }
   }

   private static double calculateShopValue(Inventory shopInventory, Player player) {
      double itemWorth = 0.0;

      for (Integer validSlot : slotsFor(shopInventory)) {
         ItemStack itemStack = shopInventory.getItem(validSlot);
         if (itemStack != null) {
            itemWorth += ItemWorthCalculator.determineResaleWorth(itemStack, player) * itemStack.getAmount();
         }
      }

      return itemWorth;
   }

   private static ItemStack updateConfirmButton(SellMenu.SaleSession session, double itemWorth) {
      ItemStack clonedConfirmButton = session.confirmButton.clone();
      List<String> lore = new ArrayList<>();

      for (String string : clonedConfirmButton.getItemMeta().getLore()) {
         lore.add(
            string.replace("$currency_amount", EconomyHandler.formatCurrency(itemWorth)).replace("$currency_name", EconomySettingsConfig.getCurrencyName())
         );
      }

      ItemMeta clonedMeta = clonedConfirmButton.getItemMeta();
      clonedMeta.setLore(lore);
      clonedConfirmButton.setItemMeta(clonedMeta);
      return clonedConfirmButton;
   }

   public void constructSellMenu(Player player) {
      EliteMenu.StoreLayout layout = new EliteMenu.StoreLayout(
         SellMenuConfig.storeSlots, SellMenuConfig.infoSlot, SellMenuConfig.cancelSlot, SellMenuConfig.confirmSlot
      );
      SellMenu.SaleSession session = new SellMenu.SaleSession(layout, SellMenuConfig.confirmButton.clone());
      String menuName = SellMenuConfig.shopName;
      if (DefaultConfig.useResourcePackModels()) {
         menuName = ChatColor.WHITE + "\udb83\udef1\udb83\ude05\udb83\udef5          " + menuName;
      }

      Inventory sellInventory = Bukkit.createInventory(player, 54, menuName);

      for (int i = 0; i < 54; i++) {
         if (i == layout.info()) {
            sellInventory.setItem(i, SellMenuConfig.infoButton);
         } else if (i == layout.cancel()) {
            sellInventory.setItem(i, SellMenuConfig.cancelButton);
         } else if (i != layout.confirm()) {
            if (!layout.inputs().contains(i) && DefaultConfig.isUseGlassToFillMenuEmptySpace()) {
               sellInventory.setItem(i, ItemStackGenerator.generateItemStack(Material.GLASS_PANE));
            }
         } else {
            ItemStack clonedConfirmButton = SellMenuConfig.confirmButton.clone();
            List<String> lore = new ArrayList<>();

            for (String string : SellMenuConfig.confirmButton.getItemMeta().getLore()) {
               lore.add(
                  string.replace("$currency_amount", EconomyHandler.formatCurrency(0.0)).replace("$currency_name", EconomySettingsConfig.getCurrencyName())
               );
            }

            SellMenuConfig.confirmButton.getItemMeta().setLore(lore);
            ItemMeta clonedMeta = clonedConfirmButton.getItemMeta();
            clonedMeta.setLore(lore);
            clonedConfirmButton.setItemMeta(clonedMeta);
            sellInventory.setItem(i, clonedConfirmButton);
         }
      }

      sessions.put(sellInventory, session);
      createEliteMenu(sellInventory, inventories);

      try {
         player.openInventory(sellInventory);
      } finally {
         if (player.getOpenInventory().getTopInventory() != sellInventory) {
            close(player, sellInventory);
         }
      }
   }

   @EventHandler
   public void onInventoryClick(InventoryClickEvent event) {
      if (isEliteMenu(event, inventories)) {
         event.setCancelled(true);
         Player player = (Player)event.getWhoClicked();
         ItemStack currentItem = event.getCurrentItem();
         Inventory shopInventory = event.getView().getTopInventory();
         SellMenu.SaleSession session = sessions.get(shopInventory);
         if (session != null) {
            EliteMenu.StoreLayout layout = session.layout;
            Inventory playerInventory = event.getView().getBottomInventory();
            if (SharedShopElements.itemNullPointerPrevention(event)) {
               if (isBottomMenu(event)) {
                  if (!EliteItemManager.isEliteMobsItem(event.getCurrentItem())) {
                     event.getWhoClicked().sendMessage(EconomySettingsConfig.getShopSaleInstructions());
                     return;
                  }

                  if (!SoulbindEnchantment.isValidSoulbindUser(currentItem.getItemMeta(), player)) {
                     player.sendMessage(EconomySettingsConfig.getShopSaleOthersItems());
                     return;
                  }

                  int firstEmptySlot = -1;

                  for (int i : slotsFor(shopInventory)) {
                     if (shopInventory.getItem(i) == null) {
                        firstEmptySlot = i;
                        break;
                     }
                  }

                  if (firstEmptySlot == -1) {
                     return;
                  }

                  shopInventory.setItem(firstEmptySlot, currentItem);
                  playerInventory.clear(event.getSlot());
                  event.getInventory().setItem(layout.confirm(), updateConfirmButton(session, calculateShopValue(shopInventory, player)));
               } else {
                  if (event.getSlot() == layout.info()) {
                     return;
                  }

                  if (event.getSlot() == layout.confirm()) {
                     if (!EconomyHandler.isReady(player.getUniqueId())) {
                        player.sendMessage(EconomySettingsConfig.getShopTransactionFailedMessage());
                        return;
                     }

                     if (!selling.add(shopInventory)) {
                        return;
                     }

                     double totalItemValue = 0.0;
                     int sold = 0;

                     try {
                        for (Integer validSlot : slotsFor(shopInventory)) {
                           ItemStack itemStack = shopInventory.getItem(validSlot);
                           if (itemStack != null) {
                              ItemStack held = itemStack.clone();
                              double itemValue = ItemWorthCalculator.determineResaleWorth(held, player) * held.getAmount();
                              if (!Double.isFinite(itemValue) || itemValue < 0.0) {
                                 player.sendMessage(EconomySettingsConfig.getShopTransactionFailedMessage());
                                 break;
                              }

                              shopInventory.clear(validSlot);

                              boolean accepted;
                              try {
                                 accepted = EconomyHandler.tryCredit(player.getUniqueId(), itemValue);
                              } catch (RuntimeException var22) {
                                 uncertainSales.computeIfAbsent(player.getUniqueId(), ignored -> new ArrayList<>()).add(new SellMenu.HeldSale(held, itemValue));
                                 Logger.warn(
                                    "Unconfirmed sale credit; retained stock for "
                                       + player.getUniqueId()
                                       + "; proceeds="
                                       + itemValue
                                       + "; item="
                                       + held
                                       + "; "
                                       + var22
                                 );
                                 player.sendMessage(EconomySettingsConfig.getShopTransactionFailedMessage());
                                 break;
                              }

                              if (!accepted) {
                                 if (inventories.contains(shopInventory)
                                    && player.getOpenInventory().getTopInventory() == shopInventory
                                    && shopInventory.getItem(validSlot) == null) {
                                    shopInventory.setItem(validSlot, held);
                                 } else {
                                    returnUnsold(player, held);
                                 }

                                 player.sendMessage(EconomySettingsConfig.getShopTransactionFailedMessage());
                                 break;
                              }

                              sold++;
                              totalItemValue += itemValue;
                              if (!inventories.contains(shopInventory)) {
                                 break;
                              }
                           }
                        }
                     } finally {
                        selling.remove(shopInventory);
                     }

                     if (sold > 0) {
                        player.sendMessage(
                           EconomySettingsConfig.getShopBatchSellMessage()
                              .replace("$currency_amount", EconomyHandler.formatCurrency(totalItemValue))
                              .replace("$currency_name", EconomySettingsConfig.getCurrencyName())
                        );
                        player.sendMessage(
                           EconomySettingsConfig.getShopCurrentBalance()
                              .replace("$currency_amount", EconomyHandler.formatCurrency(EconomyHandler.checkCurrency(player.getUniqueId())))
                              .replace("$currency_name", EconomySettingsConfig.getCurrencyName())
                        );
                     }

                     if (inventories.contains(shopInventory)) {
                        shopInventory.setItem(layout.confirm(), updateConfirmButton(session, calculateShopValue(shopInventory, player)));
                     }

                     return;
                  }

                  if (event.getSlot() == layout.cancel()) {
                     event.getWhoClicked().closeInventory();
                     return;
                  }

                  if (!slotsFor(shopInventory).contains(event.getSlot())) {
                     return;
                  }

                  moveItemDown(shopInventory, event.getSlot(), player);
                  event.getInventory().setItem(layout.confirm(), updateConfirmButton(session, calculateShopValue(shopInventory, player)));
               }
            }
         }
      }
   }

   private static void returnUnsold(Player player, ItemStack item) {
      for (ItemStack overflow : player.getInventory().addItem(new ItemStack[]{item}).values()) {
         Item dropped = player.getWorld().dropItem(player.getLocation(), overflow);
         dropped.setOwner(player.getUniqueId());
      }
   }

   @EventHandler
   public void onInventoryClose(InventoryCloseEvent event) {
      if (event.getPlayer() instanceof Player player) {
         close(player, event.getInventory());
      }
   }

   private record HeldSale(ItemStack item, double proceeds) {
   }

   private record SaleSession(EliteMenu.StoreLayout layout, ItemStack confirmButton) {
   }
}
