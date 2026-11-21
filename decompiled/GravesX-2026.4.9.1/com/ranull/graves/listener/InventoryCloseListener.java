package com.ranull.graves.listener;

import com.ranull.graves.Graves;
import com.ranull.graves.compatibility.CompatibilityInventoryView;
import com.ranull.graves.type.Grave;
import dev.cwhead.GravesX.compatibility.CompatibilitySoundEnum;
import dev.cwhead.GravesX.event.GraveCloseEvent;
import dev.cwhead.GravesX.event.GraveLootedEvent;
import java.util.Objects;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.inventory.Inventory;

public class InventoryCloseListener implements Listener {
   private final Graves plugin;

   public InventoryCloseListener(Graves plugin) {
      this.plugin = plugin;
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onInventoryClose(InventoryCloseEvent event) {
      Inventory topInventory = CompatibilityInventoryView.getTopInventory((InventoryEvent)event);
      if (topInventory.getHolder() instanceof Grave grave) {
         if (event.getPlayer() instanceof Player player) {
            try {
               this.plugin.getCacheManager().stopViewingGrave(grave.getUUID(), player.getUniqueId());
            } catch (Throwable var6) {
            }

            this.callGraveCloseEvent(event, grave, player);
            if (this.isEmptyGrave(grave)) {
               this.handleEmptyGrave(event, player, grave);
            }

            this.plugin.getEntityManager().playWorldSound("sound.close", player, grave);
         }
      }
   }

   private void callGraveCloseEvent(InventoryCloseEvent event, Grave grave, Player player) {
      GraveCloseEvent modern = new GraveCloseEvent(event.getView(), grave, player);
      this.plugin.getServer().getPluginManager().callEvent(modern);
      com.ranull.graves.event.GraveCloseEvent legacy = new com.ranull.graves.event.GraveCloseEvent(event.getView(), grave, player);
      this.plugin.getServer().getPluginManager().callEvent(legacy);
   }

   private boolean isEmptyGrave(Grave grave) {
      return grave.getItemAmount() <= 0;
   }

   private void handleEmptyGrave(InventoryCloseEvent event, Player player, Grave grave) {
      GraveLootedEvent modern = new GraveLootedEvent(event.getView(), grave, player);
      this.plugin.getServer().getPluginManager().callEvent(modern);
      com.ranull.graves.event.GraveLootedEvent legacy = new com.ranull.graves.event.GraveLootedEvent(event.getView(), grave, player);
      this.plugin.getServer().getPluginManager().callEvent(legacy);
      grave.getInventory().getViewers().remove(player);
      if (!modern.isCancelled() && !modern.isAddon() && !legacy.isCancelled() && !legacy.isAddon()) {
         this.plugin.getEntityManager().runCommands("event.command.loot", player, player.getLocation(), grave);
         this.plugin.getEntityManager().sendMessage("message.looted", player, player.getLocation(), grave);
         this.plugin.getEntityManager().spawnZombie(grave.getLocationDeath(), player, player, grave);
         if (this.plugin.getConfigManager().getConfigSection("drop.looted-explosion-effect", grave).getBoolean("drop.looted-explosion-effect", false)) {
            try {
               Location location = grave.getLocationDeath();
               Objects.requireNonNull(location.getWorld()).spawnParticle(this.plugin.getVersionManager().getParticleForVersion("EXPLOSION"), location, 1);

               try {
                  location.getWorld().playSound(location, Objects.requireNonNull(CompatibilitySoundEnum.valueOf("ENTITY_GENERIC_EXPLODE")), 1.0F, 1.0F);
               } catch (Exception var8) {
                  location.getWorld().playSound(location, Objects.requireNonNull(CompatibilitySoundEnum.valueOf("EXPLODE")), 1.0F, 1.0F);
               }
            } catch (Exception var9) {
            }
         }

         this.plugin.getGraveManager().giveGraveExperience(player, grave);
         this.plugin.getGraveManager().removeGrave(grave);
         if (this.plugin.getIntegrationManager().hasNoteBlockAPI()) {
            if (this.plugin.getIntegrationManager().getNoteBlockAPI().isSongPlayingForPlayer(player)) {
               this.plugin.getIntegrationManager().getNoteBlockAPI().stopSongForPlayer(player);
            }

            if (this.plugin.getIntegrationManager().getNoteBlockAPI().isSongPlayingForAllPlayers()) {
               this.plugin.getIntegrationManager().getNoteBlockAPI().stopSongForAllPlayers();
            }
         }
      }
   }
}
