package com.magmaguy.elitemobs.menus;

import com.magmaguy.elitemobs.api.utils.EliteItemManager;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.menus.premade.UnbinderMenuConfig;
import com.magmaguy.elitemobs.items.ItemConsumables;
import com.magmaguy.elitemobs.items.customenchantments.SoulbindEnchantment;
import com.magmaguy.elitemobs.magmacore.util.ItemStackGenerator;
import com.magmaguy.elitemobs.versionnotifier.VersionChecker;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public class UnbindMenu extends EliteMenu {
   private static final Map<Inventory, EliteMenu.RecipeLayout> layouts = new IdentityHashMap<>();
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
      EliteMenu.RecipeLayout layout = layouts.get(inventory);
      if (layout != null && inventories.remove(inventory)) {
         EliteMenu.cancel(player, inventory, player.getInventory(), List.of(layout.item(), layout.consumable()));
         inventory.clear(layout.output());
         layouts.remove(inventory);
      }
   }

   private static void calculateOutput(Inventory unbinderInventory) {
      EliteMenu.RecipeLayout layout = layouts.get(unbinderInventory);
      if (layout != null) {
         if (unbinderInventory.getItem(layout.consumable()) != null && unbinderInventory.getItem(layout.item()) != null) {
            ItemStack outputItem = unbinderInventory.getItem(layout.item()).clone();
            unbinderInventory.setItem(layout.output(), ItemConsumables.unbind(outputItem));
         } else {
            unbinderInventory.setItem(layout.output(), null);
         }
      }
   }

   public void constructUnbinderMenu(Player player) {
      EliteMenu.RecipeLayout layout = new EliteMenu.RecipeLayout(
         UnbinderMenuConfig.getEliteItemInputSlot(),
         UnbinderMenuConfig.getEliteUnbindInputSlot(),
         UnbinderMenuConfig.getOutputSlot(),
         UnbinderMenuConfig.getEliteItemInputInformationSlot(),
         UnbinderMenuConfig.getEliteScrapInputInformationSlot(),
         UnbinderMenuConfig.getOutputInformationSlot(),
         UnbinderMenuConfig.getInfoSlot(),
         UnbinderMenuConfig.getCancelSlot(),
         UnbinderMenuConfig.getConfirmSlot()
      );
      String menuName = UnbinderMenuConfig.getShopName();
      if (DefaultConfig.useResourcePackModels()) {
         menuName = ChatColor.WHITE + "\udb83\udef1\udb83\ude09\udb83\udef5          " + menuName;
      }

      Inventory unbinderInventory = Bukkit.createInventory(player, 54, menuName);

      for (int i = 0; i < unbinderInventory.getSize(); i++) {
         if (i == layout.info()) {
            ItemStack infoButton = UnbinderMenuConfig.getInfoButton().clone();
            if (DefaultConfig.useResourcePackModels()) {
               infoButton.setType(Material.PAPER);
               ItemMeta itemMeta = infoButton.getItemMeta();
               if (!VersionChecker.serverVersionOlderThan(21, 4)) {
                  itemMeta.setItemModel(NamespacedKey.fromString("elitemobs:ui/goldenquestionmark"));
               }

               infoButton.setItemMeta(itemMeta);
            }

            unbinderInventory.setItem(i, infoButton);
         } else if (i == layout.cancel()) {
            unbinderInventory.setItem(i, UnbinderMenuConfig.getCancelButton());
         } else if (i == layout.itemInfo()) {
            unbinderInventory.setItem(i, UnbinderMenuConfig.getEliteItemInputInfoButton());
         } else if (i == layout.consumableInfo()) {
            unbinderInventory.setItem(i, UnbinderMenuConfig.getEliteUnbindInputInfoButton());
         } else if (i == layout.outputInfo()) {
            unbinderInventory.setItem(i, UnbinderMenuConfig.getOutputInfoButton());
         } else if (i != layout.confirm()) {
            if (i != layout.item() && i != layout.consumable() && i != layout.output() && DefaultConfig.isUseGlassToFillMenuEmptySpace()) {
               unbinderInventory.setItem(i, ItemStackGenerator.generateItemStack(Material.GLASS_PANE));
            }
         } else {
            ItemStack clonedConfirmButton = UnbinderMenuConfig.getConfirmButton().clone();
            List<String> lore = new ArrayList<>();

            for (String string : UnbinderMenuConfig.getConfirmButton().getItemMeta().getLore()) {
               lore.add(string);
            }

            UnbinderMenuConfig.getConfirmButton().getItemMeta().setLore(lore);
            ItemMeta clonedMeta = clonedConfirmButton.getItemMeta();
            clonedMeta.setLore(lore);
            clonedConfirmButton.setItemMeta(clonedMeta);
            unbinderInventory.setItem(i, clonedConfirmButton);
         }
      }

      layouts.put(unbinderInventory, layout);
      createEliteMenu(unbinderInventory, inventories);

      try {
         player.openInventory(unbinderInventory);
      } finally {
         if (player.getOpenInventory().getTopInventory() != unbinderInventory) {
            close(player, unbinderInventory);
         }
      }
   }

   public static class UnbinderMenuEvents implements Listener {
      @EventHandler
      public void onInteract(InventoryClickEvent event) {
         if (EliteMenu.isEliteMenu(event, UnbindMenu.inventories)) {
            event.setCancelled(true);
            Player player = (Player)event.getWhoClicked();
            ItemStack currentItem = event.getCurrentItem();
            int clickedSlot = event.getSlot();
            Inventory unbinderInventory = event.getView().getTopInventory();
            EliteMenu.RecipeLayout layout = UnbindMenu.layouts.get(unbinderInventory);
            if (layout != null) {
               Inventory playerInventory = event.getView().getBottomInventory();
               if (currentItem != null) {
                  if (EliteMenu.isBottomMenu(event)) {
                     if (ItemConsumables.is(currentItem, ItemConsumables.Type.UNBIND)
                        && SoulbindEnchantment.isValidSoulbindUser(currentItem.getItemMeta(), player)) {
                        if (unbinderInventory.getItem(layout.consumable()) == null) {
                           EliteMenu.moveOneItemUp(layout.consumable(), event);
                           UnbindMenu.calculateOutput(unbinderInventory);
                        }

                        return;
                     }

                     if (EliteItemManager.isEliteMobsItem(currentItem)
                        && currentItem.getItemMeta() instanceof Damageable
                        && unbinderInventory.getItem(layout.item()) == null) {
                        unbinderInventory.setItem(layout.item(), currentItem);
                        playerInventory.clear(event.getSlot());
                        UnbindMenu.calculateOutput(unbinderInventory);
                     }
                  } else if (EliteMenu.isTopMenu(event)) {
                     if (currentItem == null) {
                        return;
                     }

                     if (event.getSlot() == layout.consumable() || event.getSlot() == layout.item()) {
                        EliteMenu.moveItemDown(event.getView().getTopInventory(), clickedSlot, player);
                        UnbindMenu.calculateOutput(unbinderInventory);
                        return;
                     }

                     if (event.getSlot() == layout.cancel()) {
                        player.closeInventory();
                        return;
                     }

                     if (event.getSlot() == layout.confirm() && unbinderInventory.getItem(layout.output()) != null) {
                        unbinderInventory.setItem(layout.item(), null);
                        unbinderInventory.setItem(layout.consumable(), null);
                        EliteMenu.moveItemDown(unbinderInventory, layout.output(), player);
                     }
                  }
               }
            }
         }
      }

      @EventHandler
      public void onClose(InventoryCloseEvent event) {
         if (event.getPlayer() instanceof Player player) {
            UnbindMenu.close(player, event.getInventory());
         }
      }
   }
}
