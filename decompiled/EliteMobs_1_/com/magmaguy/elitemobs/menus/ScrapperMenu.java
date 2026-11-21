package com.magmaguy.elitemobs.menus;

import com.magmaguy.elitemobs.api.utils.EliteItemManager;
import com.magmaguy.elitemobs.config.CustomModelsConfig;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.config.menus.premade.ScrapperMenuConfig;
import com.magmaguy.elitemobs.items.ItemConsumables;
import com.magmaguy.elitemobs.items.customenchantments.SoulbindEnchantment;
import com.magmaguy.elitemobs.magmacore.util.ItemStackGenerator;
import com.magmaguy.elitemobs.utils.CustomModelAdder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.bukkit.inventory.meta.ItemMeta;

public class ScrapperMenu extends EliteMenu {
   private static final Map<Inventory, ScrapperMenu.ScrapSession> sessions = new IdentityHashMap<>();
   public static Set<Inventory> inventories = new HashSet<>();

   public static void shutdown() {
      for (Inventory inventory : List.copyOf(inventories)) {
         if (inventory.getHolder() instanceof Player player) {
            close(player, inventory);
            if (player.getOpenInventory().getTopInventory() == inventory) {
               player.closeInventory();
            }
         }
      }
   }

   private static void close(Player player, Inventory inventory) {
      if (inventories.remove(inventory)) {
         ScrapperMenu.ScrapSession session = sessions.remove(inventory);
         if (session != null) {
            EliteMenu.cancel(player, inventory, player.getInventory(), session.layout.inputs());
         }
      }
   }

   public void constructScrapMenu(Player player) {
      EliteMenu.StoreLayout layout = new EliteMenu.StoreLayout(
         ScrapperMenuConfig.storeSlots, ScrapperMenuConfig.infoSlot, ScrapperMenuConfig.cancelSlot, ScrapperMenuConfig.confirmSlot
      );
      ScrapperMenu.ScrapSession session = new ScrapperMenu.ScrapSession(layout, ScrapperMenuConfig.scrapChance);
      String menuName = ScrapperMenuConfig.shopName;
      if (DefaultConfig.useResourcePackModels()) {
         menuName = ChatColor.WHITE + "\udb83\udef1\udb83\ude02\udb83\udef5         " + menuName;
      }

      Inventory scrapInventory = Bukkit.createInventory(player, 54, menuName);

      for (int i = 0; i < 54; i++) {
         if (i == layout.info()) {
            ItemStack infoButton = ScrapperMenuConfig.infoButton.clone();
            if (DefaultConfig.useResourcePackModels()) {
               infoButton.setType(Material.PAPER);
               CustomModelAdder.addCustomModel(infoButton, CustomModelsConfig.goldenQuestionMark);
               ItemMeta itemMeta = infoButton.getItemMeta();
               List<String> parsedLore = new ArrayList<>();
               itemMeta.getLore().forEach(entry -> parsedLore.add(entry.replace("$chance", session.chance * 100.0 + "")));
               itemMeta.setLore(parsedLore);
               infoButton.setItemMeta(itemMeta);
            }

            scrapInventory.setItem(i, infoButton);
         } else if (i == layout.cancel()) {
            scrapInventory.setItem(i, ScrapperMenuConfig.cancelButton);
         } else if (i != layout.confirm()) {
            if (!layout.inputs().contains(i) && DefaultConfig.isUseGlassToFillMenuEmptySpace()) {
               scrapInventory.setItem(i, ItemStackGenerator.generateItemStack(Material.GLASS_PANE));
            }
         } else {
            ItemStack clonedConfirmButton = ScrapperMenuConfig.confirmButton.clone();
            List<String> lore = new ArrayList<>();

            for (String string : ScrapperMenuConfig.confirmButton.getItemMeta().getLore()) {
               lore.add(string.replace("$chance", session.chance * 100.0 + ""));
            }

            ScrapperMenuConfig.confirmButton.getItemMeta().setLore(lore);
            ItemMeta clonedMeta = clonedConfirmButton.getItemMeta();
            clonedMeta.setLore(lore);
            clonedConfirmButton.setItemMeta(clonedMeta);
            scrapInventory.setItem(i, clonedConfirmButton);
         }
      }

      sessions.put(scrapInventory, session);
      createEliteMenu(scrapInventory, inventories);

      try {
         player.openInventory(scrapInventory);
      } finally {
         if (player.getOpenInventory().getTopInventory() != scrapInventory) {
            close(player, scrapInventory);
         }
      }
   }

   private record ScrapSession(EliteMenu.StoreLayout layout, double chance) {
   }

   public static class ScrapperMenuEvents implements Listener {
      @EventHandler
      public void onInventoryInteract(InventoryClickEvent event) {
         if (EliteMenu.isEliteMenu(event, ScrapperMenu.inventories)) {
            event.setCancelled(true);
            Player player = (Player)event.getWhoClicked();
            ItemStack currentItem = event.getCurrentItem();
            Inventory shopInventory = event.getView().getTopInventory();
            ScrapperMenu.ScrapSession session = ScrapperMenu.sessions.get(shopInventory);
            if (session != null) {
               EliteMenu.StoreLayout layout = session.layout;
               Inventory playerInventory = event.getView().getBottomInventory();
               if (currentItem != null) {
                  if (EliteMenu.isBottomMenu(event)) {
                     if (!EliteItemManager.isEliteMobsItem(event.getCurrentItem())) {
                        event.getWhoClicked().sendMessage(EconomySettingsConfig.getShopSaleInstructions());
                        return;
                     }

                     if (!SoulbindEnchantment.isValidSoulbindUser(currentItem.getItemMeta(), player)) {
                        player.sendMessage(EconomySettingsConfig.getShopSaleOthersItems());
                        return;
                     }

                     for (int slot : layout.inputs()) {
                        if (shopInventory.getItem(slot) == null) {
                           shopInventory.setItem(slot, currentItem);
                           playerInventory.clear(event.getSlot());
                           break;
                        }
                     }
                  } else if (EliteMenu.isTopMenu(event)) {
                     if (event.getSlot() == layout.info()) {
                        return;
                     }

                     if (event.getSlot() == layout.confirm()) {
                        int successes = 0;
                        int failures = 0;
                        boolean generationFailed = false;

                        for (Integer validSlot : layout.inputs()) {
                           ItemStack itemStack = shopInventory.getItem(validSlot);
                           if (itemStack != null) {
                              ItemStack scrapItem = ItemConsumables.generateScrap(itemStack, player, null);
                              if (scrapItem == null) {
                                 generationFailed = true;
                              } else {
                                 for (int i = 0; i < itemStack.getAmount(); i++) {
                                    if (ThreadLocalRandom.current().nextDouble() > session.chance) {
                                       failures++;
                                    } else {
                                       HashMap<Integer, ItemStack> leftOvers = player.getInventory().addItem(new ItemStack[]{scrapItem.clone()});
                                       leftOvers.values().forEach(leftOver -> player.getWorld().dropItem(player.getLocation(), leftOver));
                                       successes++;
                                    }
                                 }

                                 itemStack.setAmount(0);
                              }
                           }
                        }

                        if (successes > 0) {
                           player.sendMessage(ItemSettingsConfig.getScrapSucceededMessage().replace("$amount", successes + ""));
                        }

                        if (failures > 0) {
                           player.sendMessage(ItemSettingsConfig.getScrapFailedMessage().replace("$amount", failures + ""));
                        }

                        if (successes == 0 && failures == 0 && generationFailed) {
                           player.sendMessage(ItemSettingsConfig.getScrapNotConfiguredMessage());
                        }

                        return;
                     }

                     if (event.getSlot() == layout.cancel()) {
                        player.closeInventory();
                        return;
                     }

                     if (!layout.inputs().contains(event.getSlot())) {
                        return;
                     }

                     EliteMenu.moveItemDown(event.getView().getTopInventory(), event.getSlot(), player);
                  }
               }
            }
         }
      }

      @EventHandler
      public void onClose(InventoryCloseEvent event) {
         if (event.getPlayer() instanceof Player player) {
            ScrapperMenu.close(player, event.getInventory());
         }
      }
   }
}
