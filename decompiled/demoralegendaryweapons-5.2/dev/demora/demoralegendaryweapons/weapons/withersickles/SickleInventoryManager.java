package dev.demora.demoralegendaryweapons.weapons.withersickles;

import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class SickleInventoryManager {
   private final DemoraLegendaryWeapons plugin;
   private final WitherSickles witherSickles;
   private final Map<UUID, Boolean> hasOffhandSickle = new HashMap<>();

   public SickleInventoryManager(DemoraLegendaryWeapons plugin, WitherSickles witherSickles, SickleCooldownManager cooldownManager) {
      this.plugin = plugin;
      this.witherSickles = witherSickles;
   }

   public void equipOffhandSickle(Player player) {
      ItemStack mainHand = player.getInventory().getItemInMainHand();
      if (this.witherSickles.isWitherSickle(mainHand)) {
         PlayerInventory inv = player.getInventory();
         ItemStack offHand = inv.getItemInOffHand();
         if (this.witherSickles.isAdditionalSickle(offHand)) {
            this.hasOffhandSickle.put(player.getUniqueId(), true);
         } else {
            ItemStack secondSickle = this.witherSickles.createAdditionalSickle();
            if (offHand != null && offHand.getType() != Material.AIR) {
               int emptySlot = inv.firstEmpty();
               if (emptySlot != -1) {
                  inv.setItem(emptySlot, offHand);
               } else {
                  player.getWorld().dropItemNaturally(player.getLocation(), offHand);
               }
            }

            inv.setItemInOffHand(secondSickle);
            this.hasOffhandSickle.put(player.getUniqueId(), true);
         }
      }
   }

   public void removeOffhandSickle(Player player) {
      PlayerInventory inv = player.getInventory();
      ItemStack offHand = inv.getItemInOffHand();
      if (this.witherSickles.isAdditionalSickle(offHand)) {
         inv.setItemInOffHand(new ItemStack(Material.AIR));
      }

      this.hasOffhandSickle.remove(player.getUniqueId());
   }

   public boolean hasOffhandSickle(Player player) {
      return this.hasOffhandSickle.containsKey(player.getUniqueId()) && this.hasOffhandSickle.get(player.getUniqueId());
   }

   public void setOffhandSickle(Player player, boolean value) {
      this.hasOffhandSickle.put(player.getUniqueId(), value);
   }

   public void handleInventoryClick(InventoryClickEvent event) {
      if (event.getWhoClicked() instanceof Player) {
         Player player = (Player)event.getWhoClicked();
         ItemStack cursor = event.getCursor();
         ItemStack clicked = event.getCurrentItem();
         ItemStack offHand = player.getInventory().getItemInOffHand();
         boolean isPlayerInventory = event.getView().getTopInventory() == null || event.getView().getTopInventory().getType() == InventoryType.PLAYER;
         if (this.witherSickles.isWitherSickle(offHand)) {
            event.setCancelled(true);
         } else {
            if (this.witherSickles.isAdditionalSickle(offHand)) {
               boolean isOffhandSlot = event.getSlotType() == SlotType.ARMOR && event.getSlot() == 40;
               if (isOffhandSlot) {
                  event.setCancelled(true);
                  return;
               }

               boolean isOffhandSickleInCursor = this.witherSickles.isAdditionalSickle(cursor) && cursor != null && cursor.isSimilar(offHand);
               boolean isOffhandSickleInClicked = this.witherSickles.isAdditionalSickle(clicked) && clicked != null && clicked.isSimilar(offHand);
               if (!isPlayerInventory && (isOffhandSickleInCursor || isOffhandSickleInClicked)) {
                  event.setCancelled(true);
                  return;
               }

               InventoryAction action = event.getAction();
               if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY && (isOffhandSickleInCursor || isOffhandSickleInClicked)) {
                  event.setCancelled(true);
                  return;
               }

               if ((action == InventoryAction.HOTBAR_MOVE_AND_READD || action == InventoryAction.HOTBAR_SWAP)
                  && (isOffhandSickleInCursor || isOffhandSickleInClicked)) {
                  event.setCancelled(true);
                  return;
               }

               if (isOffhandSickleInCursor) {
                  event.setCancelled(true);
                  return;
               }

               if (isOffhandSickleInClicked) {
                  event.setCancelled(true);
                  return;
               }

               if (event.getClick().isShiftClick() && (isOffhandSickleInCursor || isOffhandSickleInClicked)) {
                  event.setCancelled(true);
                  return;
               }

               if (event.getHotbarButton() >= 0 && event.getHotbarButton() <= 8) {
                  if ((this.witherSickles.isWitherSickle(clicked) || this.witherSickles.isWitherSickle(cursor)) && !isPlayerInventory) {
                     event.setCancelled(true);
                     return;
                  }

                  if (isOffhandSickleInCursor || isOffhandSickleInClicked) {
                     event.setCancelled(true);
                     return;
                  }
               }
            }

            if (event.getSlot() == player.getInventory().getHeldItemSlot()) {
               Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
                  ItemStack mainHand = player.getInventory().getItemInMainHand();
                  if (!this.witherSickles.isWitherSickle(mainHand)) {
                     this.removeOffhandSickle(player);
                  }
               }, 1L);
            }
         }
      }
   }

   public void handleInventoryDrag(InventoryDragEvent event) {
      if (event.getWhoClicked() instanceof Player) {
         Player player = (Player)event.getWhoClicked();
         ItemStack dragged = event.getOldCursor();
         ItemStack offHand = player.getInventory().getItemInOffHand();
         if (!this.witherSickles.isWitherSickle(offHand) && !this.witherSickles.isWitherSickle(dragged)) {
            if (this.witherSickles.isAdditionalSickle(offHand) && this.witherSickles.isAdditionalSickle(dragged) && dragged.isSimilar(offHand)) {
               event.setCancelled(true);
            } else if (event.getInventorySlots().contains(40)
               && this.witherSickles.isAdditionalSickle(offHand)
               && this.witherSickles.isAdditionalSickle(dragged)
               && dragged.isSimilar(offHand)) {
               event.setCancelled(true);
            } else {
               for (int slot : event.getRawSlots()) {
                  if (slot == 40
                     && this.witherSickles.isAdditionalSickle(offHand)
                     && this.witherSickles.isAdditionalSickle(dragged)
                     && dragged.isSimilar(offHand)) {
                     event.setCancelled(true);
                     return;
                  }
               }
            }
         } else {
            event.setCancelled(true);
         }
      }
   }

   public void handleDropItem(PlayerDropItemEvent event) {
      Player player = event.getPlayer();
      ItemStack dropped = event.getItemDrop().getItemStack();
      if (this.witherSickles.isWitherSickle(dropped) || this.witherSickles.isAdditionalSickle(dropped)) {
         this.removeOffhandSickle(player);
      }
   }

   public void handleSwapHands(PlayerSwapHandItemsEvent event) {
      ItemStack mainHand = event.getMainHandItem();
      ItemStack offHand = event.getOffHandItem();
      if (this.witherSickles.isWitherSickle(offHand) || this.witherSickles.isWitherSickle(mainHand)) {
         event.setCancelled(true);
      }

      if (this.witherSickles.isAdditionalSickle(offHand) && !this.witherSickles.isWitherSickle(mainHand)) {
         event.setCancelled(true);
      }
   }

   public void removePlayerData(UUID playerId) {
      this.hasOffhandSickle.remove(playerId);
   }
}
