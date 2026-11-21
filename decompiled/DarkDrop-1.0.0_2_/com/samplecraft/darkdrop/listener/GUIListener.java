package com.samplecraft.darkdrop.listener;

import com.samplecraft.darkdrop.DarkDrop;
import com.samplecraft.darkdrop.gui.CommandInputGUI;
import com.samplecraft.darkdrop.gui.GoodLootGUI;
import com.samplecraft.darkdrop.gui.SettingsGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class GUIListener implements Listener {
   private final DarkDrop plugin;

   public GUIListener(DarkDrop plugin) {
      this.plugin = plugin;
   }

   @EventHandler
   public void onInventoryClick(InventoryClickEvent event) {
      InventoryHolder holder = event.getView().getTopInventory().getHolder();
      if (holder != null) {
         if (holder instanceof SettingsGUI settings) {
            this.handleSettingsClick(event, settings);
         } else if (holder instanceof GoodLootGUI lootGui) {
            this.handleGoodLootClick(event, lootGui);
         } else if (holder instanceof CommandInputGUI cmdGui) {
            this.handleCommandInputClick(event, cmdGui);
         }
      }
   }

   private void handleCommandInputClick(InventoryClickEvent event, CommandInputGUI gui) {
      event.setCancelled(true);
      if (event.getClickedInventory() != null) {
         if (event.getClickedInventory().equals(event.getView().getTopInventory())) {
            int slot = event.getSlot();
            ClickType click = event.getClick();
            boolean right = click == ClickType.RIGHT || click == ClickType.SHIFT_RIGHT;
            boolean shift = click.isShiftClick();
            gui.handleClick(slot, right, shift);
         }
      }
   }

   private void handleSettingsClick(InventoryClickEvent event, SettingsGUI gui) {
      event.setCancelled(true);
      if (event.getClickedInventory() != null) {
         if (event.getClickedInventory().equals(event.getView().getTopInventory())) {
            int slot = event.getSlot();
            ClickType click = event.getClick();
            boolean right = click == ClickType.RIGHT || click == ClickType.SHIFT_RIGHT;
            boolean shift = click.isShiftClick();
            gui.handleClick(slot, right, shift);
         }
      }
   }

   private void handleGoodLootClick(InventoryClickEvent event, GoodLootGUI gui) {
      int slot = event.getRawSlot();
      ClickType click = event.getClick();
      if (GoodLootGUI.isButtonSlot(slot)) {
         event.setCancelled(true);
         if (slot == 52) {
            gui.saveAndReturn();
         } else if (slot == 53) {
            gui.cancel();
         }
      } else if (!GoodLootGUI.isInputSlot(slot)) {
         if (event.getClickedInventory() != null && !event.getClickedInventory().equals(event.getView().getTopInventory())) {
            if (click.isShiftClick()) {
               event.setCancelled(true);
               ItemStack cursor = event.getCurrentItem();
               if (cursor == null) {
                  return;
               }

               int firstFree = -1;

               for (int i = 0; i <= 44; i++) {
                  if (event.getView().getTopInventory().getItem(i) == null || event.getView().getTopInventory().getItem(i).getType().isAir()) {
                     firstFree = i;
                     break;
                  }
               }

               if (firstFree != -1) {
                  event.getView().getTopInventory().setItem(firstFree, cursor.clone());
                  event.setCurrentItem(null);
               }
            }
         } else {
            event.setCancelled(true);
         }
      }
   }

   @EventHandler
   public void onInventoryDrag(InventoryDragEvent event) {
      InventoryHolder holder = event.getView().getTopInventory().getHolder();
      if (holder != null) {
         if (holder instanceof SettingsGUI) {
            event.setCancelled(true);
         } else {
            if (holder instanceof GoodLootGUI) {
               for (int slot : event.getRawSlots()) {
                  if (!GoodLootGUI.isInputSlot(slot)) {
                     event.setCancelled(true);
                     return;
                  }
               }
            }

            if (holder instanceof CommandInputGUI) {
               event.setCancelled(true);
            }
         }
      }
   }

   @EventHandler
   public void onInventoryClose(InventoryCloseEvent event) {
      if (event.getInventory().getHolder() instanceof GoodLootGUI lootGui && event.getPlayer() instanceof Player player) {
         boolean hasItems = false;

         for (int i = 0; i <= 44; i++) {
            ItemStack s = event.getInventory().getItem(i);
            if (s != null && !s.getType().isAir()) {
               hasItems = true;
               break;
            }
         }

         if (hasItems) {
            for (int ix = 0; ix <= 44; ix++) {
               ItemStack s = event.getInventory().getItem(ix);
               if (s != null && !s.getType().isAir()) {
                  event.getInventory().setItem(ix, null);
                  player.getInventory()
                     .addItem(new ItemStack[]{s})
                     .values()
                     .forEach(leftover -> player.getWorld().dropItemNaturally(player.getLocation(), leftover));
               }
            }
         }
      }
   }
}
