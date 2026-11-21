package com.ranull.graves.listener;

import com.ranull.graves.Graves;
import com.ranull.graves.compatibility.CompatibilityInventoryView;
import com.ranull.graves.integration.MiniMessage;
import com.ranull.graves.inventory.GraveList;
import com.ranull.graves.inventory.GraveMenu;
import com.ranull.graves.type.Grave;
import com.ranull.graves.util.InventoryUtil;
import com.ranull.graves.util.StringUtil;
import dev.cwhead.GravesX.event.GraveItemTakeEvent;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class InventoryClickListener implements Listener {
   private final Graves plugin;

   public InventoryClickListener(Graves plugin) {
      this.plugin = plugin;
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onInventoryClick(InventoryClickEvent event) {
      Player player = (Player)event.getWhoClicked();
      Inventory clicked = event.getClickedInventory();
      if (clicked != null) {
         Inventory top = CompatibilityInventoryView.getTopInventory((InventoryEvent)event);
         InventoryHolder topHolder = top.getHolder();
         if (clicked.getHolder() instanceof Grave grave) {
            this.handleGraveInventoryClick(event, player, grave);
         }

         if (topHolder instanceof GraveList graveList) {
            if (event.getRawSlot() >= 0 && event.getRawSlot() < top.getSize()) {
               this.handleGraveListClick(event, player, graveList, event.getRawSlot());
            }

            event.setCancelled(true);
         } else if (topHolder instanceof GraveMenu graveMenu) {
            if (event.getRawSlot() >= 0 && event.getRawSlot() < top.getSize()) {
               this.handleGraveMenuClick(event, player, graveMenu, event.getRawSlot());
            }

            event.setCancelled(true);
         }

         ClickType clickType = event.getClick();
         if (clickType == ClickType.SHIFT_LEFT || clickType == ClickType.SHIFT_RIGHT) {
            this.handleShiftClick(event);
         }

         this.isCompassItem(event);
      }
   }

   private void isCompassItem(InventoryClickEvent event) {
      Inventory clickedInventory = event.getClickedInventory();
      if (clickedInventory != null) {
         ItemStack item = event.getCursor();
         if (item != null && item.hasItemMeta()) {
            ItemMeta itemMeta = item.getItemMeta();
            if (itemMeta != null && itemMeta.hasDisplayName()) {
               if (item.getType() == Material.valueOf(String.valueOf(this.plugin.getVersionManager().getMaterialForVersion("RECOVERY_COMPASS")))) {
                  UUID graveUUID = this.getGraveUUIDFromItemStack(item);
                  if (graveUUID != null) {
                     Grave grave = this.plugin.getCacheManager().getGrave(graveUUID);
                     if (grave != null) {
                        String compassName = this.plugin.getIntegrationManager().hasMiniMessage()
                           ? MiniMessage.parseString(
                              StringUtil.parseString(
                                 "&f" + this.plugin.getConfigManager().getConfigSection("compass.name", grave).getString("compass.name"), grave, this.plugin
                              )
                           )
                           : StringUtil.parseString(
                              "&f" + this.plugin.getConfigManager().getConfigSection("compass.name", grave).getString("compass.name"), grave, this.plugin
                           );
                        if (itemMeta.getDisplayName().equals(compassName)) {
                           InventoryType inventoryType = event.getInventory().getType();
                           if (this.checkIfXPGivingInventory(inventoryType)) {
                              event.setCancelled(true);
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private void handleShiftClick(InventoryClickEvent event) {
      Inventory clickedInventory = event.getClickedInventory();
      if (clickedInventory != null) {
         ItemStack item = event.getCurrentItem();
         if (item != null && item.hasItemMeta()) {
            ItemMeta itemMeta = item.getItemMeta();
            if (itemMeta != null && itemMeta.hasDisplayName()) {
               if (item.getType() == Material.valueOf(String.valueOf(this.plugin.getVersionManager().getMaterialForVersion("RECOVERY_COMPASS")))) {
                  UUID graveUUID = this.getGraveUUIDFromItemStack(item);
                  if (graveUUID != null) {
                     Grave grave = this.plugin.getCacheManager().getGrave(graveUUID);
                     if (grave != null) {
                        String compassName = this.plugin.getIntegrationManager().hasMiniMessage()
                           ? MiniMessage.parseString(
                              StringUtil.parseString(
                                 "&f" + this.plugin.getConfigManager().getConfigSection("compass.name", grave).getString("compass.name"), grave, this.plugin
                              )
                           )
                           : StringUtil.parseString(
                              "&f" + this.plugin.getConfigManager().getConfigSection("compass.name", grave).getString("compass.name"), grave, this.plugin
                           );
                        if (itemMeta.getDisplayName().equals(compassName)) {
                           InventoryType inventoryType = event.getInventory().getType();
                           if (this.checkIfXPGivingInventory(inventoryType)) {
                              event.setCancelled(true);
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private boolean checkIfXPGivingInventory(InventoryType inventoryType) {
      String var2 = inventoryType.name();

      return switch (var2) {
         case "FURNACE", "BLAST_FURNACE", "SMOKER", "ANVIL", "GRINDSTONE", "HOPPER" -> true;
         default -> false;
      };
   }

   private UUID getGraveUUIDFromItemStack(ItemStack itemStack) {
      if (itemStack.hasItemMeta()) {
         if (itemStack.getItemMeta() == null) {
            return null;
         } else {
            String uuidString = (String)itemStack.getItemMeta()
               .getPersistentDataContainer()
               .get(new NamespacedKey(this.plugin, "graveUUID"), PersistentDataType.STRING);
            return uuidString != null ? UUID.fromString(uuidString) : null;
         }
      } else {
         return null;
      }
   }

   private void handleGraveInventoryClick(InventoryClickEvent event, Player player, Grave grave) {
      if (grave.getGravePreview()) {
         event.setCancelled(true);
      } else if (!this.plugin.getEntityManager().canOpenGrave(player, grave)) {
         event.setCancelled(true);
      } else {
         Inventory clickedInventory = event.getClickedInventory();
         InventoryAction action = event.getAction();
         Inventory topInventory = CompatibilityInventoryView.getTopInventory((InventoryEvent)event);
         Inventory bottomInventory = CompatibilityInventoryView.getBottomInventory((InventoryEvent)event);
         Grave.StorageMode storageMode = this.plugin
            .getGraveManager()
            .getStorageMode(this.plugin.getConfigManager().getConfigSection("storage.mode", grave).getString("storage.mode"));
         if (clickedInventory != null) {
            if (storageMode == Grave.StorageMode.EXACT) {
               if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY && clickedInventory.equals(bottomInventory)) {
                  event.setCancelled(true);
                  return;
               }

               if (clickedInventory.equals(grave.getInventory())) {
                  if (action == InventoryAction.PLACE_ALL
                     || action == InventoryAction.PLACE_SOME
                     || action == InventoryAction.PLACE_ONE
                     || action == InventoryAction.SWAP_WITH_CURSOR) {
                     event.setCancelled(true);
                     return;
                  }

                  ItemStack clickedItem = event.getCurrentItem();
                  if (clickedItem != null && clickedItem.getType() != Material.AIR) {
                     GraveItemTakeEvent takeEvent = new GraveItemTakeEvent(grave, player, clickedItem, action);
                     this.plugin.getServer().getPluginManager().callEvent(takeEvent);
                     if (takeEvent.isCancelled()) {
                        event.setCancelled(true);
                        return;
                     }

                     this.plugin
                        .getSchedulerManager()
                        .runTaskLater(
                           () -> this.plugin
                              .getDataManager()
                              .updateGrave(grave, "inventory", InventoryUtil.inventoryToString(grave.getInventory(), this.plugin)),
                           1L
                        );
                  }
               }
            }
         }
      }
   }

   private void handleGraveListClick(InventoryClickEvent event, Player player, GraveList graveList, int slot) {
      Grave grave = graveList.getGrave(slot);
      if (grave != null) {
         if (event.getClick() == ClickType.SHIFT_LEFT) {
            event.setCancelled(true);
            return;
         }

         this.plugin
            .getEntityManager()
            .runFunction(
               player, this.plugin.getConfigManager().getConfigSection("gui.menu.list.function", grave).getString("gui.menu.list.function", "menu"), grave
            );
         this.plugin.getGUIManager().setGraveListItems(graveList.getInventory(), graveList.getUUID());
      }

      event.setCancelled(true);
   }

   private void handleGraveMenuClick(InventoryClickEvent event, Player player, GraveMenu graveMenu, int slot) {
      Grave grave = graveMenu.getGrave();
      if (grave == null) {
         event.getWhoClicked().closeInventory();
         event.setCancelled(true);
      } else if (event.getClick() == ClickType.SHIFT_LEFT) {
         event.setCancelled(true);
      } else {
         if (this.plugin
            .getConfigManager()
            .getConfigSection("gui.menu.grave.slot." + slot + ".enabled", grave)
            .getBoolean("gui.menu.grave.slot." + slot + ".enabled")) {
            this.plugin
               .getEntityManager()
               .runFunction(
                  player,
                  this.plugin
                     .getConfigManager()
                     .getConfigSection("gui.menu.grave.slot." + slot + ".function", grave)
                     .getString("gui.menu.grave.slot." + slot + ".function", "none"),
                  grave
               );
            this.plugin.getGUIManager().setGraveMenuItems(graveMenu.getInventory(), grave);
         }

         event.setCancelled(true);
      }
   }
}
