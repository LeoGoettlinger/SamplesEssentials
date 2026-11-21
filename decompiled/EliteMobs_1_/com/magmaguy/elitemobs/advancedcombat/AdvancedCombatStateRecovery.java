package com.magmaguy.elitemobs.advancedcombat;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.advancedcombat.passives.ClassPassiveRuntime;
import com.magmaguy.elitemobs.combatsystem.combattag.DungeonCombatRuntime;
import com.magmaguy.elitemobs.config.AdvancedCombatSystemConfig;
import com.magmaguy.elitemobs.skills.ArmorSkillHealthBonus;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public final class AdvancedCombatStateRecovery implements Listener {
   public static void reconcile(Player player) {
      if (AdvancedCombatSystemConfig.isEnabled() && !DungeonCombatRuntime.isInManagedCombatWorld(player)) {
         ArmorSkillHealthBonus.applyHealthBonus(player);
      }

      if (!AdvancedCombatRuntime.isActive(player)) {
         if (!AdvancedCombatSystemConfig.isEnabled() || !DungeonCombatRuntime.isEligiblePlayer(player)) {
            clearOwnedState(player);
         }
      }
   }

   public static void clearAllOnlinePlayers() {
      for (Player player : Bukkit.getOnlinePlayers()) {
         clearOwnedState(player);
      }
   }

   private static void clearOwnedState(Player player) {
      AdvancedFoodItems.restorePlayerInventory(player);
      AdvancedCombatRuntime.clearPersistedState(player);
      ClassPassiveRuntime.clearPersistedState(player);
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPlayerJoin(PlayerJoinEvent event) {
      Player player = event.getPlayer();
      Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN, () -> {
         if (player.isOnline()) {
            reconcile(player);
         }
      });
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
      reconcile(event.getPlayer());
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onInventoryOpen(InventoryOpenEvent event) {
      if (event.getPlayer() instanceof Player player) {
         refreshAfterInventoryMutation(player, event.getView().getTopInventory());
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onInventoryClick(InventoryClickEvent event) {
      if (event.getWhoClicked() instanceof Player player) {
         refreshAfterInventoryMutation(player, event.getView().getTopInventory());
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onInventoryDrag(InventoryDragEvent event) {
      if (event.getWhoClicked() instanceof Player player) {
         refreshAfterInventoryMutation(player, event.getView().getTopInventory());
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onInventoryMove(InventoryMoveItemEvent event) {
      ItemStack stack = event.getItem();
      if (AdvancedFoodItems.restoreItem(stack)) {
         event.setItem(stack);
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onDrop(PlayerDropItemEvent event) {
      AdvancedFoodItems.restoreDroppedItem(event.getItemDrop());
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onItemSpawn(ItemSpawnEvent event) {
      AdvancedFoodItems.restoreDroppedItem(event.getEntity());
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onPickup(EntityPickupItemEvent event) {
      if (event.getEntity() instanceof Player player) {
         Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN, () -> refreshPlayerInventory(player));
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onDeath(PlayerDeathEvent event) {
      for (ItemStack drop : event.getDrops()) {
         AdvancedFoodItems.restoreItem(drop);
      }
   }

   private static void refreshAfterInventoryMutation(Player player, Inventory topInventory) {
      Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN, () -> {
         AdvancedFoodItems.restoreInventory(topInventory);
         AdvancedFoodItems.restoreItem(player.getItemOnCursor());
         refreshPlayerInventory(player);
      });
   }

   private static void refreshPlayerInventory(Player player) {
      if (player.isOnline()) {
         if (AdvancedCombatRuntime.isActive(player)) {
            AdvancedFoodItems.preparePlayerInventory(player);
         } else {
            AdvancedFoodItems.restorePlayerInventory(player);
         }
      }
   }
}
