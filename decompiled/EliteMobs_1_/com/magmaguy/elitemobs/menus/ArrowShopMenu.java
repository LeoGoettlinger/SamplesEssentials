package com.magmaguy.elitemobs.menus;

import com.magmaguy.elitemobs.config.menus.premade.ArrowShopMenuConfig;
import com.magmaguy.elitemobs.magmacore.util.ChatColorConverter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

public class ArrowShopMenu {
   private static final Map<Integer, ArrowShopMenu.ArrowItem> slotToArrow = new HashMap<>();

   public static void openArrowShop(Player player) {
      String menuName = ArrowShopMenuConfig.getShopName();
      Inventory inventory = Bukkit.createInventory(player, 27, menuName);

      for (Entry<Integer, ArrowShopMenu.ArrowItem> entry : slotToArrow.entrySet()) {
         inventory.setItem(entry.getKey(), createShopItem(entry.getValue()));
      }

      player.openInventory(inventory);
      ArrowShopMenu.ArrowShopMenuEvents.menus.add(inventory);
   }

   private static ItemStack createShopItem(ArrowShopMenu.ArrowItem arrowItem) {
      ItemStack item;
      if (arrowItem.potionType != null) {
         item = new ItemStack(arrowItem.material, arrowItem.amount);
         PotionMeta meta = (PotionMeta)item.getItemMeta();
         meta.setBasePotionType(arrowItem.potionType);
         meta.setDisplayName(ChatColorConverter.convert(arrowItem.displayName));
         meta.setLore(
            List.of(
               "",
               ArrowShopMenuConfig.getShopItemAmountFormat().replace("$amount", String.valueOf(arrowItem.amount)),
               ArrowShopMenuConfig.getShopItemPriceFormat().replace("$price", String.valueOf(arrowItem.price)),
               "",
               ArrowShopMenuConfig.getShopItemClickToPurchase()
            )
         );
         item.setItemMeta(meta);
      } else {
         item = new ItemStack(arrowItem.material, arrowItem.amount);
         ItemMeta meta = item.getItemMeta();
         meta.setDisplayName(ChatColorConverter.convert(arrowItem.displayName));
         meta.setLore(
            List.of(
               "",
               ArrowShopMenuConfig.getShopItemAmountFormat().replace("$amount", String.valueOf(arrowItem.amount)),
               ArrowShopMenuConfig.getShopItemPriceFormat().replace("$price", String.valueOf(arrowItem.price)),
               "",
               ArrowShopMenuConfig.getShopItemClickToPurchase()
            )
         );
         item.setItemMeta(meta);
      }

      return item;
   }

   private static ItemStack createArrowItem(ArrowShopMenu.ArrowItem arrowItem) {
      ItemStack item;
      if (arrowItem.potionType != null) {
         item = new ItemStack(arrowItem.material, arrowItem.amount);
         PotionMeta meta = (PotionMeta)item.getItemMeta();
         meta.setBasePotionType(arrowItem.potionType);
         item.setItemMeta(meta);
      } else {
         item = new ItemStack(arrowItem.material, arrowItem.amount);
      }

      return item;
   }

   public static void shutdown() {
      ArrowShopMenu.ArrowShopMenuEvents.menus.clear();
   }

   static {
      slotToArrow.put(
         10,
         new ArrowShopMenu.ArrowItem(
            Material.ARROW,
            ArrowShopMenuConfig.getArrowSmallStackAmount(),
            ArrowShopMenuConfig.getArrowSmallStackPrice(),
            null,
            ArrowShopMenuConfig.getArrowDisplayName()
         )
      );
      slotToArrow.put(
         11,
         new ArrowShopMenu.ArrowItem(
            Material.ARROW,
            ArrowShopMenuConfig.getArrowLargeStackAmount(),
            ArrowShopMenuConfig.getArrowLargeStackPrice(),
            null,
            ArrowShopMenuConfig.getArrowDisplayName()
         )
      );
      slotToArrow.put(
         12,
         new ArrowShopMenu.ArrowItem(
            Material.SPECTRAL_ARROW,
            ArrowShopMenuConfig.getSpectralArrowAmount(),
            ArrowShopMenuConfig.getSpectralArrowPrice(),
            null,
            ArrowShopMenuConfig.getSpectralArrowDisplayName()
         )
      );
      slotToArrow.put(
         14,
         new ArrowShopMenu.ArrowItem(
            Material.TIPPED_ARROW,
            ArrowShopMenuConfig.getTippedArrowPoisonAmount(),
            ArrowShopMenuConfig.getTippedArrowPoisonPrice(),
            PotionType.POISON,
            ArrowShopMenuConfig.getArrowOfPoisonDisplayName()
         )
      );
      slotToArrow.put(
         15,
         new ArrowShopMenu.ArrowItem(
            Material.TIPPED_ARROW,
            ArrowShopMenuConfig.getTippedArrowSlownessAmount(),
            ArrowShopMenuConfig.getTippedArrowSlownessPrice(),
            PotionType.SLOWNESS,
            ArrowShopMenuConfig.getArrowOfSlownessDisplayName()
         )
      );
      slotToArrow.put(
         16,
         new ArrowShopMenu.ArrowItem(
            Material.TIPPED_ARROW,
            ArrowShopMenuConfig.getTippedArrowWeaknessAmount(),
            ArrowShopMenuConfig.getTippedArrowWeaknessPrice(),
            PotionType.WEAKNESS,
            ArrowShopMenuConfig.getArrowOfWeaknessDisplayName()
         )
      );
      slotToArrow.put(
         21,
         new ArrowShopMenu.ArrowItem(
            Material.TIPPED_ARROW,
            ArrowShopMenuConfig.getTippedArrowHarmingAmount(),
            ArrowShopMenuConfig.getTippedArrowHarmingPrice(),
            PotionType.HARMING,
            ArrowShopMenuConfig.getArrowOfHarmingDisplayName()
         )
      );
      slotToArrow.put(
         23,
         new ArrowShopMenu.ArrowItem(
            Material.TIPPED_ARROW,
            ArrowShopMenuConfig.getTippedArrowHealingAmount(),
            ArrowShopMenuConfig.getTippedArrowHealingPrice(),
            PotionType.HEALING,
            ArrowShopMenuConfig.getArrowOfHealingDisplayName()
         )
      );
   }

   private record ArrowItem(Material material, int amount, int price, PotionType potionType, String displayName) {
   }

   public static class ArrowShopMenuEvents implements Listener {
      static final Set<Inventory> menus = new HashSet<>();

      @EventHandler
      public void onClick(InventoryClickEvent event) {
         if (menus.contains(event.getInventory())) {
            if (event.getClickedInventory() != null && event.getClickedInventory().getType().equals(InventoryType.CHEST)) {
               event.setCancelled(true);
               if (event.getWhoClicked() instanceof Player player) {
                  ArrowShopMenu.ArrowItem arrowItem = ArrowShopMenu.slotToArrow.get(event.getSlot());
                  if (arrowItem != null) {
                     if (player.getInventory().firstEmpty() == -1) {
                        player.sendMessage(ArrowShopMenuConfig.getInventoryFullMessage());
                     } else if (SharedShopElements.purchase(player, ArrowShopMenu.createArrowItem(arrowItem), arrowItem.price)) {
                        player.sendMessage(
                           ArrowShopMenuConfig.getPurchaseSuccessMessage()
                              .replace("%amount%", String.valueOf(arrowItem.amount))
                              .replace("%item%", arrowItem.displayName.replace("&", "§"))
                              .replace("%price%", String.valueOf(arrowItem.price))
                        );
                     }
                  }
               }
            } else {
               event.setCancelled(true);
            }
         }
      }

      @EventHandler
      public void onClose(InventoryCloseEvent event) {
         menus.remove(event.getInventory());
      }
   }
}
