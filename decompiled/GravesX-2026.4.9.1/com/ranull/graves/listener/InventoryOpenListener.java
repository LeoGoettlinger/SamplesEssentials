package com.ranull.graves.listener;

import com.ranull.graves.Graves;
import com.ranull.graves.compatibility.CompatibilityInventoryView;
import com.ranull.graves.type.Grave;
import dev.cwhead.GravesX.event.GraveOpenEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;

public class InventoryOpenListener implements Listener {
   private final Graves plugin;

   public InventoryOpenListener(Graves plugin) {
      this.plugin = plugin;
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onInventoryOpen(InventoryOpenEvent event) {
      if (this.isGraveInventory(event)) {
         this.handleGraveInventoryOpen(event);
      }
   }

   private boolean isGraveInventory(InventoryOpenEvent event) {
      return event.getInventory().getHolder() instanceof Grave;
   }

   private void handleGraveInventoryOpen(InventoryOpenEvent event) {
      Inventory topInventory = CompatibilityInventoryView.getTopInventory((InventoryEvent)event);
      if (topInventory.getHolder() instanceof Grave grave && event.getPlayer() instanceof Player player) {
         GraveOpenEvent var11 = new GraveOpenEvent(event.getView(), grave, player);
         this.plugin.getServer().getPluginManager().callEvent(var11);
         com.ranull.graves.event.GraveOpenEvent legacy = new com.ranull.graves.event.GraveOpenEvent(event.getView(), grave, player);
         this.plugin.getServer().getPluginManager().callEvent(legacy);
         boolean cancelled = var11.isCancelled() || legacy.isCancelled();
         boolean addon = var11.isAddon() || legacy.isAddon();
         if (cancelled && !addon) {
            event.setCancelled(true);
         } else {
            if (this.plugin.getConfigManager().getConfigSection("zombie.open", grave).getBoolean("zombie.open", false)) {
               Location spawnAt = grave.getLocationDeath();
               if (spawnAt != null && spawnAt.getWorld() != null) {
                  this.plugin.getEntityManager().spawnZombie(spawnAt, player, player, grave);
               }
            }
         }
      }
   }
}
