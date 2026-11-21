package com.magmaguy.elitemobs.menus;

import com.magmaguy.elitemobs.api.utils.EliteItemManager;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.menus.premade.EliteScrollMenuConfig;
import com.magmaguy.elitemobs.items.EliteScroll;
import com.magmaguy.elitemobs.magmacore.util.ChatColorConverter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

public class EliteScrollMenu extends EliteMenu {
   public EliteScrollMenu(Player player) {
      String name = EliteScrollMenuConfig.getMenuName();
      if (DefaultConfig.useResourcePackModels()) {
         name = ChatColor.WHITE + "\udb83\udef1\udb83\ude01\udb83\udef5           " + name;
      }

      Inventory inventory = Bukkit.createInventory(player, 54, name);
      inventory.setItem(EliteScrollMenuConfig.getInfoSlot(), EliteScrollMenuConfig.getInfoButton());
      inventory.setItem(EliteScrollMenuConfig.getNonEliteItemInfoSlot(), EliteScrollMenuConfig.getNonEliteItemInfoButton());
      inventory.setItem(EliteScrollMenuConfig.getEliteScrollItemInfoSlot(), EliteScrollMenuConfig.getEliteScrollItemInfoButton());
      inventory.setItem(EliteScrollMenuConfig.getOutputInfoSlot(), EliteScrollMenuConfig.getOutputInfoButton());
      inventory.setItem(EliteScrollMenuConfig.getCancelSlot(), EliteScrollMenuConfig.getCancelButton());
      inventory.setItem(EliteScrollMenuConfig.getConfirmSlot(), EliteScrollMenuConfig.getConfirmButton());
      player.openInventory(inventory);
      EliteScrollMenu.EliteScrollMenuEvents.menus.add(inventory);
   }

   private static void updateMenu(InventoryClickEvent event) {
      if (event.getView().getTopInventory().getItem(EliteScrollMenuConfig.getEliteScrollItemSlot()) != null
         && event.getView().getTopInventory().getItem(EliteScrollMenuConfig.getNonEliteItemSlot()) != null) {
         event.getView()
            .getTopInventory()
            .setItem(
               EliteScrollMenuConfig.getOutputSlot(),
               EliteScroll.convertVanillaItem(
                  event.getView().getTopInventory().getItem(EliteScrollMenuConfig.getNonEliteItemSlot()),
                  event.getView().getTopInventory().getItem(EliteScrollMenuConfig.getEliteScrollItemSlot()),
                  (Player)event.getWhoClicked()
               )
            );
      } else {
         event.getView().getTopInventory().setItem(EliteScrollMenuConfig.getOutputSlot(), null);
      }
   }

   public static class EliteScrollMenuEvents implements Listener {
      private static final Set<Inventory> menus = new HashSet<>();

      public static void shutdown() {
         menus.clear();
      }

      @EventHandler
      public void onInventoryInteract(InventoryClickEvent event) {
         if (EliteMenu.isEliteMenu(event, menus)) {
            event.setCancelled(true);
            if (SharedShopElements.itemNullPointerPrevention(event)) {
               if (EliteMenu.isTopMenu(event)) {
                  this.handleTopInventory(event);
               } else {
                  this.handleBottomInventory(event);
               }

               EliteScrollMenu.updateMenu(event);
            }
         }
      }

      private void handleTopInventory(InventoryClickEvent event) {
         int clickedSlot = event.getSlot();
         if (clickedSlot == EliteScrollMenuConfig.getCancelSlot()) {
            this.cancel(event);
         } else if (clickedSlot == EliteScrollMenuConfig.getConfirmSlot()) {
            this.confirm(event);
         } else if (clickedSlot == EliteScrollMenuConfig.getEliteScrollItemSlot() || clickedSlot == EliteScrollMenuConfig.getNonEliteItemSlot()) {
            EliteMenu.moveItemDown(event.getView().getTopInventory(), clickedSlot, event.getWhoClicked());
            event.getView().getTopInventory().clear(clickedSlot);
         }
      }

      private void handleBottomInventory(InventoryClickEvent event) {
         if (EliteScroll.isEliteScroll(event.getCurrentItem())) {
            if (!EliteScroll.canUseScroll((Player)event.getWhoClicked(), event.getCurrentItem())) {
               event.getWhoClicked().sendMessage(ChatColorConverter.convert(EliteScrollMenuConfig.getScrollNotOwnedMessage()));
               return;
            }

            if (event.getView().getTopInventory().getItem(EliteScrollMenuConfig.getEliteScrollItemSlot()) == null) {
               EliteMenu.moveOneItemUp(EliteScrollMenuConfig.getEliteScrollItemSlot(), event);
            }
         } else if (!EliteItemManager.isEliteMobsItem(event.getCurrentItem())
            && event.getView().getTopInventory().getItem(EliteScrollMenuConfig.getNonEliteItemSlot()) == null) {
            EliteMenu.moveOneItemUp(EliteScrollMenuConfig.getNonEliteItemSlot(), event);
         }
      }

      private void confirm(InventoryClickEvent event) {
         EliteScrollMenu.updateMenu(event);
         if (event.getView().getTopInventory().getItem(EliteScrollMenuConfig.getOutputSlot()) != null) {
            EliteMenu.moveItemDown(event.getView().getTopInventory(), EliteScrollMenuConfig.getOutputSlot(), event.getWhoClicked());
            event.getView().getTopInventory().clear();
            event.getWhoClicked().closeInventory();
         }
      }

      private void cancel(InventoryClickEvent event) {
         event.getWhoClicked().closeInventory();
      }

      @EventHandler
      public void onInventoryDrag(InventoryDragEvent event) {
         if (menus.contains(event.getView().getTopInventory())) {
            int topSize = event.getView().getTopInventory().getSize();
            if (event.getRawSlots().stream().anyMatch(slot -> slot < topSize)) {
               event.setCancelled(true);
            }
         }
      }

      @EventHandler
      public void onClose(InventoryCloseEvent event) {
         if (menus.contains(event.getInventory())) {
            menus.remove(event.getInventory());
            EliteMenu.cancel(
               event.getPlayer(),
               event.getView().getTopInventory(),
               event.getView().getBottomInventory(),
               new ArrayList<>(List.of(EliteScrollMenuConfig.getEliteScrollItemSlot(), EliteScrollMenuConfig.getNonEliteItemSlot()))
            );
         }
      }
   }
}
