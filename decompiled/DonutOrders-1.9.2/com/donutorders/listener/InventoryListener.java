package com.donutorders.listener;

import com.donutorders.gui.DeliverItemsGUI;
import com.donutorders.manager.GUIManager;
import com.donutorders.util.DeliveryItemUtils;
import com.donutorders.util.ItemUtils;
import com.donutorders.util.MessageHelper;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryListener implements Listener {
   private final GUIManager guiManager;
   private final Map<UUID, InventoryListener.PlayerInteractionState> interactionStates = new ConcurrentHashMap<>();

   public InventoryListener(GUIManager guiManager) {
      this.guiManager = guiManager;
   }

   @EventHandler(
      priority = EventPriority.HIGH,
      ignoreCancelled = false
   )
   public void onClick(InventoryClickEvent event) {
      if (event.getWhoClicked() instanceof Player player) {
         GUIManager.PlayerGUIState state = this.guiManager.getState(player.getUniqueId());
         if (state != null) {
            int currentTick = Bukkit.getCurrentTick();
            InventoryListener.PlayerInteractionState interactionState = this.interactionStates
               .computeIfAbsent(player.getUniqueId(), k -> new InventoryListener.PlayerInteractionState());
            if (interactionState.lastTick == currentTick) {
               interactionState.interactionsThisTick++;
            } else {
               interactionState.lastTick = currentTick;
               interactionState.interactionsThisTick = 1;
            }

            int slot = event.getSlot();
            if (interactionState.interactionsThisTick > 2) {
               event.setCancelled(true);
               Bukkit.getLogger()
                  .log(
                     Level.WARNING,
                     "[Security] Player {0} exceeded click rate limit ({1} clicks in tick {2}). Possible packet exploit.",
                     new Object[]{player.getName(), interactionState.interactionsThisTick, currentTick}
                  );
            } else if (interactionState.interactionsThisTick > 1 && interactionState.lastSlot == slot) {
               event.setCancelled(true);
               Bukkit.getLogger()
                  .log(
                     Level.WARNING,
                     "[Security] Player {0} double-clicked slot {1} in the same tick ({2}). Possible packet replay/delay exploit.",
                     new Object[]{player.getName(), slot, currentTick}
                  );
            } else {
               interactionState.lastSlot = slot;
               Inventory topInv = event.getView().getTopInventory();
               Inventory clickedInv = event.getClickedInventory();
               if (state.type == GUIManager.GUIType.DELIVER_ITEMS && state.gui instanceof DeliverItemsGUI deliverGUI) {
                  ItemStack template = deliverGUI.getOrder().getItemTemplate();
                  boolean isTopInv = clickedInv != null && clickedInv.equals(topInv);
                  if (!isTopInv) {
                     InventoryAction action = event.getAction();
                     if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                        ItemStack current = event.getCurrentItem();
                        if (current != null && current.getType() != Material.AIR) {
                           if (DeliveryItemUtils.isDeliverablePlacement(current, template)) {
                              event.setCancelled(false);
                           } else {
                              event.setCancelled(true);
                              MessageHelper.send(
                                 player, "delivery-wrong-item", "&cᴡʀᴏɴɢ ɪᴛᴇᴍ ᴛʏᴘᴇ — ᴛʜɪꜱ ᴏʀᴅᴇʀ ᴡᴀɴᴛꜱ &f{0}&c.", ItemUtils.describeOrderItem(template)
                              );
                           }
                        }
                     } else if (action == InventoryAction.COLLECT_TO_CURSOR) {
                        ItemStack cursor = event.getCursor();
                        if (cursor != null && cursor.getType() != Material.AIR) {
                           if (DeliveryItemUtils.isDeliverablePlacement(cursor, template)) {
                              event.setCancelled(false);
                           } else {
                              event.setCancelled(true);
                           }
                        }
                     } else {
                        event.setCancelled(false);
                     }
                  } else {
                     ItemStack clicked = event.getCurrentItem();
                     ClickType type = event.getClick();
                     if (slot >= 45) {
                        event.setCancelled(true);
                        deliverGUI.handleClick(player, slot, clicked, type);
                     } else {
                        InventoryAction action = event.getAction();
                        if (action == InventoryAction.PICKUP_ALL
                           || action == InventoryAction.PICKUP_HALF
                           || action == InventoryAction.PICKUP_ONE
                           || action == InventoryAction.PICKUP_SOME
                           || action == InventoryAction.DROP_ALL_SLOT
                           || action == InventoryAction.DROP_ONE_SLOT
                           || action == InventoryAction.MOVE_TO_OTHER_INVENTORY
                           || action == InventoryAction.COLLECT_TO_CURSOR) {
                           event.setCancelled(false);
                        } else if (action == InventoryAction.PLACE_ALL
                           || action == InventoryAction.PLACE_SOME
                           || action == InventoryAction.PLACE_ONE
                           || action == InventoryAction.SWAP_WITH_CURSOR) {
                           ItemStack cursor = event.getCursor();
                           if (cursor != null && cursor.getType() != Material.AIR) {
                              if (DeliveryItemUtils.isDeliverablePlacement(cursor, template)) {
                                 event.setCancelled(false);
                              } else {
                                 event.setCancelled(true);
                                 MessageHelper.send(
                                    player, "delivery-wrong-item", "&cᴡʀᴏɴɢ ɪᴛᴇᴍ ᴛʏᴘᴇ — ᴛʜɪꜱ ᴏʀᴅᴇʀ ᴡᴀɴᴛꜱ &f{0}&c.", ItemUtils.describeOrderItem(template)
                                 );
                              }
                           }
                        } else if (action == InventoryAction.HOTBAR_SWAP || action == InventoryAction.HOTBAR_MOVE_AND_READD) {
                           int hotbarSlot = event.getHotbarButton();
                           ItemStack hotbarItem = hotbarSlot >= 0 && hotbarSlot < 9 ? player.getInventory().getItem(hotbarSlot) : null;
                           if (hotbarItem == null || hotbarItem.getType() == Material.AIR) {
                              event.setCancelled(false);
                           } else if (DeliveryItemUtils.isDeliverablePlacement(hotbarItem, template)) {
                              event.setCancelled(false);
                           } else {
                              event.setCancelled(true);
                              MessageHelper.send(
                                 player, "delivery-wrong-item", "&cᴡʀᴏɴɢ ɪᴛᴇᴍ ᴛʏᴘᴇ — ᴛʜɪꜱ ᴏʀᴅᴇʀ ᴡᴀɴᴛꜱ &f{0}&c.", ItemUtils.describeOrderItem(template)
                              );
                           }
                        } else if (type != ClickType.SWAP_OFFHAND) {
                           event.setCancelled(true);
                        } else {
                           ItemStack offhandItem = player.getInventory().getItemInOffHand();
                           if (offhandItem == null || offhandItem.getType() == Material.AIR) {
                              event.setCancelled(false);
                           } else if (DeliveryItemUtils.isDeliverablePlacement(offhandItem, template)) {
                              event.setCancelled(false);
                           } else {
                              event.setCancelled(true);
                              MessageHelper.send(
                                 player, "delivery-wrong-item", "&cᴡʀᴏɴɢ ɪᴛᴇᴍ ᴛʏᴘᴇ — ᴛʜɪꜱ ᴏʀᴅᴇʀ ᴡᴀɴᴛꜱ &f{0}&c.", ItemUtils.describeOrderItem(template)
                              );
                           }
                        }
                     }
                  }
               } else {
                  event.setCancelled(true);
                  if (clickedInv != null && clickedInv.equals(topInv)) {
                     ItemStack clicked = event.getCurrentItem();
                     ClickType type = event.getClick();
                     if (state.gui != null) {
                        state.gui.handleClick(player, slot, clicked, type);
                     }
                  }
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGH,
      ignoreCancelled = false
   )
   public void onDrag(InventoryDragEvent event) {
      if (event.getWhoClicked() instanceof Player player) {
         GUIManager.PlayerGUIState state = this.guiManager.getState(player.getUniqueId());
         if (state != null) {
            Inventory topInv = event.getView().getTopInventory();
            int topSize = topInv.getSize();
            Iterator var6 = event.getRawSlots().iterator();

            while (true) {
               if (!var6.hasNext()) {
                  return;
               }

               int rawSlot = (Integer)var6.next();
               if (rawSlot < topSize) {
                  if (state.type != GUIManager.GUIType.DELIVER_ITEMS || !(state.gui instanceof DeliverItemsGUI deliverGUI) || rawSlot >= 45) {
                     break;
                  }

                  ItemStack oldCursor = event.getOldCursor();
                  if (oldCursor == null || !DeliveryItemUtils.isDeliverablePlacement(oldCursor, deliverGUI.getOrder().getItemTemplate())) {
                     break;
                  }
               }
            }

            event.setCancelled(true);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onClose(InventoryCloseEvent event) {
      if (event.getPlayer() instanceof Player player) {
         this.interactionStates.remove(player.getUniqueId());
         this.guiManager.returnHeldDeliveryItems(player);
         this.guiManager.clearState(player.getUniqueId());
      }
   }

   @EventHandler(
      priority = EventPriority.HIGH
   )
   public void onDeath(PlayerDeathEvent event) {
      Player player = event.getEntity();
      if (this.guiManager.getState(player.getUniqueId()) != null) {
         if (event.getKeepInventory()) {
            this.guiManager.returnHeldDeliveryItems(player);
         } else {
            this.guiManager.returnHeldDeliveryItems(player, event.getDrops());
         }
      }
   }

   private static class PlayerInteractionState {
      int lastTick = -1;
      int lastSlot = -1;
      int interactionsThisTick = 0;
   }
}
