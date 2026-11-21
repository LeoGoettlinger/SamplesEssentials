package com.magmaguy.elitemobs.menus;

import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import com.magmaguy.elitemobs.items.EliteItemLore;
import com.magmaguy.elitemobs.items.ItemTagger;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class SharedShopElements {
   private static final Set<UUID> purchases = new HashSet<>();

   public static boolean purchase(Player player, ItemStack template, double price) {
      if (!purchases.add(player.getUniqueId())) {
         return false;
      } else {
         boolean paymentAttempted = false;
         boolean paid = false;
         ItemStack delivery = null;

         boolean failure;
         try {
            delivery = template.clone();
            if (ItemTagger.isEliteItem(delivery)) {
               new EliteItemLore(delivery, false);
            }

            paymentAttempted = true;
            if (EconomyHandler.tryWithdraw(player.getUniqueId(), price)) {
               paid = true;

               for (ItemStack overflow : player.getInventory().addItem(new ItemStack[]{delivery}).values()) {
                  Item dropped = player.getWorld().dropItem(player.getLocation(), overflow);
                  dropped.setOwner(player.getUniqueId());
               }

               return true;
            }

            insufficientFundsMessage(player, price);
            failure = false;
         } catch (RuntimeException var13) {
            Logger.warn(
               "Shop purchase needs inspection for "
                  + player.getUniqueId()
                  + "; price="
                  + price
                  + "; debit attempted="
                  + paymentAttempted
                  + "; debit accepted="
                  + paid
                  + "; item="
                  + delivery
                  + "; failure="
                  + var13
            );
            player.sendMessage(EconomySettingsConfig.getShopTransactionFailedMessage());
            player.closeInventory();
            return false;
         } finally {
            purchases.remove(player.getUniqueId());
         }

         return failure;
      }
   }

   public static boolean itemNullPointerPrevention(InventoryClickEvent event) {
      if (event.getCurrentItem() == null) {
         return false;
      } else {
         return event.getCurrentItem().getType().equals(Material.AIR) ? false : event.getCurrentItem().getItemMeta() != null;
      }
   }

   public static void buyMessage(final Player player, final String itemDisplayName, final double itemValue) {
      (new BukkitRunnable() {
            public void run() {
               player.sendMessage(
                  EconomySettingsConfig.getShopBuyMessage()
                     .replace("$item_name", itemDisplayName)
                     .replace("$item_value", EconomyHandler.formatCurrency(itemValue))
                     .replace("$currency_name", EconomySettingsConfig.getCurrencyName())
               );
               player.sendMessage(
                  EconomySettingsConfig.getShopCurrentBalance()
                     .replace("$currency_amount", EconomyHandler.formatCurrency(EconomyHandler.checkCurrency(player.getUniqueId())))
                     .replace("$currency_name", EconomySettingsConfig.getCurrencyName())
               );
            }
         })
         .runTaskLater(Bukkit.getPluginManager().getPlugin("EliteMobs"), 2L);
   }

   public static void insufficientFundsMessage(final Player player, final double itemValue) {
      (new BukkitRunnable() {
            public void run() {
               player.sendMessage(EconomySettingsConfig.getShopInsufficientFundsMessage().replace("$currency_name", EconomySettingsConfig.getCurrencyName()));
               player.sendMessage(
                  EconomySettingsConfig.getShopCurrentBalance()
                     .replace("$currency_amount", EconomyHandler.formatCurrency(EconomyHandler.checkCurrency(player.getUniqueId())))
                     .replace("$currency_name", EconomySettingsConfig.getCurrencyName())
               );
               player.sendMessage(
                  EconomySettingsConfig.getShopItemPrice()
                     .replace("$item_value", EconomyHandler.formatCurrency(itemValue))
                     .replace("$currency_name", EconomySettingsConfig.getCurrencyName())
               );
            }
         })
         .runTaskLater(Bukkit.getPluginManager().getPlugin("EliteMobs"), 2L);
      player.closeInventory();
   }
}
