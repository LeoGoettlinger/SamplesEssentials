package com.ranull.graves.listener;

import com.ranull.graves.Graves;
import com.ranull.graves.integration.MiniMessage;
import com.ranull.graves.inventory.GraveList;
import com.ranull.graves.inventory.GraveMenu;
import com.ranull.graves.type.Grave;
import com.ranull.graves.util.StringUtil;
import dev.cwhead.GravesX.event.GraveItemTakeEvent;
import java.util.Map;
import java.util.UUID;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class InventoryDragListener implements Listener {
   private final Graves plugin;

   public InventoryDragListener(Graves plugin) {
      this.plugin = plugin;
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onInventoryDrag(InventoryDragEvent event) {
      InventoryHolder inventoryHolder = event.getInventory().getHolder();
      Player player = (Player)event.getWhoClicked();
      if (inventoryHolder != null) {
         if (inventoryHolder instanceof Grave grave) {
            this.handleGraveInventoryDrag(event, player, grave);
         } else if (event.getWhoClicked() instanceof Player) {
            this.handlePlayerInventoryDrag(event, player, inventoryHolder);
         }

         this.checkCompassDrag(event);
      }
   }

   private void checkCompassDrag(InventoryDragEvent event) {
      Map<Integer, ItemStack> newItems = event.getNewItems();

      for (ItemStack item : newItems.values()) {
         if (item != null && item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasDisplayName()) {
               try {
                  if (item.getType() == this.plugin.getVersionManager().getMaterialForVersion("RECOVERY_COMPASS")) {
                     UUID graveUUID = this.getGraveUUIDFromItemStack(item);
                     if (graveUUID != null) {
                        Grave grave = this.plugin.getCacheManager().getGrave(graveUUID);
                        if (grave != null) {
                           String configured = this.plugin.getConfigManager().getConfigSection("compass.name", grave).getString("compass.name");
                           String parsedLegacy = StringUtil.parseString("&f" + configured, grave, this.plugin);
                           String expectedName = this.plugin.getIntegrationManager().hasMiniMessage() ? MiniMessage.parseString(parsedLegacy) : parsedLegacy;
                           if (meta.getDisplayName().equals(expectedName)) {
                              InventoryType type = event.getInventory().getType();
                              if (this.isXpGivingInventory(type)) {
                                 event.setCancelled(true);
                                 return;
                              }
                           }
                        }
                     }
                  }
               } catch (Throwable var12) {
               }
            }
         }
      }
   }

   private void handleGraveInventoryDrag(InventoryDragEvent event, Player player, Grave grave) {
      if (!grave.getGravePreview() && this.plugin.getEntityManager().canOpenGrave(player, grave)) {
         Map<Integer, ItemStack> draggedItems = event.getNewItems();
         InventoryAction action = InventoryAction.MOVE_TO_OTHER_INVENTORY;

         for (ItemStack item : draggedItems.values()) {
            if (item != null) {
               GraveItemTakeEvent takeEvent = new GraveItemTakeEvent(grave, player, item, action);
               this.plugin.getServer().getPluginManager().callEvent(takeEvent);
               if (takeEvent.isCancelled()) {
                  event.setCancelled(true);
                  return;
               }
            }
         }

         event.setCancelled(true);
      } else {
         event.setCancelled(true);
      }
   }

   private void handlePlayerInventoryDrag(InventoryDragEvent event, Player player, InventoryHolder holder) {
      if (holder instanceof GraveList list) {
         this.handleGraveListDrag(event, player, list);
      } else if (holder instanceof GraveMenu menu) {
         this.handleGraveMenuDrag(event, player, menu);
      }
   }

   private boolean isXpGivingInventory(InventoryType type) {
      return switch (type) {
         case FURNACE, BLAST_FURNACE, SMOKER, ANVIL, GRINDSTONE, HOPPER -> true;
         default -> false;
      };
   }

   private void handleGraveListDrag(InventoryDragEvent event, Player player, GraveList graveList) {
      event.setCancelled(true);
   }

   private void handleGraveMenuDrag(InventoryDragEvent event, Player player, GraveMenu graveMenu) {
      try {
         Grave grave = graveMenu.getGrave();
         if (grave != null) {
            event.setCancelled(true);
         }
      } catch (IllegalArgumentException | NullPointerException var5) {
         event.getWhoClicked().closeInventory();
         event.setCancelled(true);
      }
   }

   private UUID getGraveUUIDFromItemStack(ItemStack itemStack) {
      if (!itemStack.hasItemMeta()) {
         return null;
      } else {
         ItemMeta meta = itemStack.getItemMeta();
         if (meta == null) {
            return null;
         } else {
            String uuidString = (String)meta.getPersistentDataContainer().get(new NamespacedKey(this.plugin, "graveUUID"), PersistentDataType.STRING);
            return uuidString != null ? UUID.fromString(uuidString) : null;
         }
      }
   }
}
