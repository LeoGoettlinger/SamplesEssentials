package com.magmaguy.elitemobs.menus;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.CustomModelsConfig;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.config.menus.premade.BuyOrSellMenuConfig;
import com.magmaguy.elitemobs.config.menus.premade.ProceduralShopMenuConfig;
import com.magmaguy.elitemobs.items.EliteItemLore;
import com.magmaguy.elitemobs.items.GearRestrictionHandler;
import com.magmaguy.elitemobs.items.ItemTagger;
import com.magmaguy.elitemobs.items.ItemWorthCalculator;
import com.magmaguy.elitemobs.items.itemconstructor.ItemConstructor;
import com.magmaguy.elitemobs.items.itemconstructor.MaterialGenerator;
import com.magmaguy.elitemobs.items.itemconstructor.ProceduralItemType;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.SkillXPCalculator;
import com.magmaguy.elitemobs.utils.CustomModelAdder;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
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

public class ProceduralShopMenu {
   private static final List<Integer> validSlots = ProceduralShopMenuConfig.storeSlots;
   public static Set<UUID> cooldownPlayers = new HashSet<>();

   public static void shutdown() {
      cooldownPlayers.clear();
      ProceduralShopMenu.ProceduralShopMenuEvents.menus.clear();
   }

   public static void shopInitializer(Player player) {
      if (EconomySettingsConfig.isEnableEconomy()) {
         BuyOrSellMenu.constructBuyOrSellMenu(player, BuyOrSellMenuConfig.BUY_PROCEDURAL_ITEM, () -> shopConstructor(player));
      }
   }

   public static void shopConstructor(Player player) {
      String menuName = ProceduralShopMenuConfig.shopName;
      if (DefaultConfig.useResourcePackModels()) {
         menuName = ChatColor.WHITE + "\udb83\udef1\udb83\ude06\udb83\udef5          " + menuName;
      }

      Inventory shopInventory = Bukkit.createInventory(player, 54, menuName);
      populateShop(shopInventory, player);
      player.openInventory(shopInventory);
      ProceduralShopMenu.ProceduralShopMenuEvents.menus.add(shopInventory);
   }

   private static void populateShop(Inventory shopInventory, Player player) {
      ItemStack rerollButton = ProceduralShopMenuConfig.rerollItem;
      if (DefaultConfig.useResourcePackModels()) {
         rerollButton.setType(Material.PAPER);
         CustomModelAdder.addCustomModel(rerollButton, CustomModelsConfig.goldenQuestionMark);
      }

      shopInventory.setItem(ProceduralShopMenuConfig.rerollSlot, rerollButton);
      shopContents(shopInventory, player);
   }

   private static void shopContents(Inventory shopInventory, Player player) {
      for (int i : validSlots) {
         ProceduralItemType itemType = MaterialGenerator.generateRandomItemType();
         if (itemType != null) {
            SkillType skillType = itemType.skill();
            int itemLevel = calculateItemLevel(player, skillType);
            ItemStack itemStack = ItemConstructor.constructProceduralItem(itemType, itemLevel, null, player, true);
            if (itemStack != null) {
               new EliteItemLore(itemStack, true);
               shopInventory.setItem(i, itemStack);
            }
         }
      }
   }

   private static int calculateItemLevel(Player player, SkillType skillType) {
      int playerSkillLevel;
      if (skillType != null) {
         long skillXP = PlayerData.getSkillXP(player.getUniqueId(), skillType);
         playerSkillLevel = SkillXPCalculator.levelFromTotalXP(skillXP);
      } else {
         playerSkillLevel = getAverageSkillLevel(player);
      }

      int minLevel = Math.max(1, playerSkillLevel - 5);
      int maxLevel = Math.min(100, playerSkillLevel);
      if (minLevel > maxLevel) {
         minLevel = maxLevel;
      }

      return minLevel == maxLevel ? minLevel : ThreadLocalRandom.current().nextInt(minLevel, maxLevel + 1);
   }

   private static int getAverageSkillLevel(Player player) {
      long totalXP = 0L;
      int count = 0;

      for (SkillType type : SkillType.values()) {
         totalXP += PlayerData.getSkillXP(player.getUniqueId(), type);
         count++;
      }

      if (count == 0) {
         return 1;
      } else {
         long averageXP = totalXP / count;
         return Math.max(1, SkillXPCalculator.levelFromTotalXP(averageXP));
      }
   }

   public static class ProceduralShopMenuEvents implements Listener {
      public static Set<Inventory> menus = new HashSet<>();

      @EventHandler
      public void onClick(final InventoryClickEvent event) {
         if (EliteMenu.isEliteMenu(event, menus)) {
            event.setCancelled(true);
            if (EliteMenu.isTopMenu(event)) {
               if (SharedShopElements.itemNullPointerPrevention(event)) {
                  if (event.getSlot() == ProceduralShopMenuConfig.rerollSlot) {
                     if (ProceduralShopMenu.cooldownPlayers.contains(event.getWhoClicked().getUniqueId())) {
                        event.setCancelled(true);
                     } else {
                        ProceduralShopMenu.cooldownPlayers.add(event.getWhoClicked().getUniqueId());
                        (new BukkitRunnable() {
                           public void run() {
                              ProceduralShopMenu.cooldownPlayers.remove(event.getWhoClicked().getUniqueId());
                           }
                        }).runTaskLater(MetadataHandler.PLUGIN, 40L);
                        ProceduralShopMenu.populateShop(event.getInventory(), Bukkit.getPlayer(event.getWhoClicked().getUniqueId()));
                        event.setCancelled(true);
                     }
                  } else if (ProceduralShopMenu.validSlots.contains(event.getSlot()) && ItemTagger.isEliteItem(event.getCurrentItem())) {
                     Player player = (Player)event.getWhoClicked();
                     ItemStack itemStack = event.getCurrentItem();
                     String itemDisplayName = itemStack.getItemMeta().getDisplayName();
                     double itemValue = ItemWorthCalculator.determineItemWorth(itemStack, player);
                     boolean inventoryHasFreeSlots = false;

                     for (ItemStack iteratedStack : player.getInventory().getStorageContents()) {
                        if (iteratedStack == null) {
                           inventoryHasFreeSlots = true;
                           break;
                        }
                     }

                     if (EliteMenu.isTopMenu(event)) {
                        if (!inventoryHasFreeSlots) {
                           player.sendMessage(ProceduralShopMenuConfig.messageFullInventory);
                           player.closeInventory();
                        } else {
                           if (!GearRestrictionHandler.canEquip(player, itemStack)) {
                              GearRestrictionHandler.sendRestrictionMessage(player, itemStack);
                              return;
                           }

                           if (!SharedShopElements.purchase(player, itemStack, itemValue)) {
                              return;
                           }

                           ProceduralShopMenu.populateShop(event.getInventory(), Bukkit.getPlayer(event.getWhoClicked().getUniqueId()));
                           SharedShopElements.buyMessage(player, itemDisplayName, itemValue);
                        }
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
