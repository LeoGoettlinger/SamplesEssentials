package de.theredend2000.advancedhunt.listeners;

import de.theredend2000.advancedhunt.Main;
import de.theredend2000.advancedhunt.managers.PlaceModeManager;
import de.theredend2000.advancedhunt.managers.TreasureInteractionHandler;
import de.theredend2000.advancedhunt.managers.TreasureManager;
import de.theredend2000.advancedhunt.model.Reward;
import de.theredend2000.advancedhunt.model.Treasure;
import de.theredend2000.advancedhunt.model.TreasureCore;
import de.theredend2000.advancedhunt.util.RewardPresetUtils;
import dev.lone.itemsadder.api.Events.CustomBlockBreakEvent;
import dev.lone.itemsadder.api.Events.CustomBlockInteractEvent;
import dev.lone.itemsadder.api.Events.CustomBlockPlaceEvent;
import dev.lone.itemsadder.api.Events.FurnitureBreakEvent;
import dev.lone.itemsadder.api.Events.FurnitureInteractEvent;
import dev.lone.itemsadder.api.Events.FurniturePlacedEvent;
import java.util.List;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ItemsAdderIntegrationListener implements Listener {
   private final Main plugin;
   private final TreasureManager treasureManager;
   private final PlaceModeManager placeModeManager;
   private final TreasureInteractionHandler treasureInteractionHandler;

   public ItemsAdderIntegrationListener(Main plugin) {
      this.plugin = plugin;
      this.treasureManager = plugin.getTreasureManager();
      this.placeModeManager = plugin.getPlaceModeManager();
      this.treasureInteractionHandler = TreasureInteractionHandler.getInstance(plugin);
   }

   @EventHandler
   public void onFurniturePlaceSuccess(FurniturePlacedEvent event) {
      Player player = event.getPlayer();
      if (player != null) {
         if (this.placeModeManager.isInPlaceMode(player)) {
            UUID collectionId = this.placeModeManager.getCollectionId(player);
            if (collectionId != null) {
               Entity entity = event.getBukkitEntity();
               Location loc = toBlockLocation(entity.getLocation());
               if (this.treasureManager.getTreasureCoreAt(loc) == null) {
                  List<Reward> rewards = RewardPresetUtils.getDefaultTreasureRewards(this.plugin, collectionId);
                  Treasure treasure = new Treasure(
                     this.treasureManager.generateUniqueTreasureId(), collectionId, loc, rewards, null, "ITEMS_ADDER", event.getNamespacedID()
                  );
                  this.treasureManager.addTreasure(treasure);
                  player.sendMessage(this.plugin.getMessageManager().getMessage("treasure.added"));
                  this.plugin.getSoundManager().playTreasurePlaced(player);
               }
            }
         }
      }
   }

   @EventHandler
   public void onCustomBlockPlace(CustomBlockPlaceEvent event) {
      Player player = event.getPlayer();
      if (player != null) {
         if (this.placeModeManager.isInPlaceMode(player)) {
            UUID collectionId = this.placeModeManager.getCollectionId(player);
            if (collectionId != null) {
               if (event.getBlock() != null) {
                  Location loc = event.getBlock().getLocation();
                  if (this.treasureManager.getTreasureCoreAt(loc) == null) {
                     String namespacedId = event.getNamespacedID();
                     if (namespacedId != null && !namespacedId.isEmpty()) {
                        List<Reward> rewards = RewardPresetUtils.getDefaultTreasureRewards(this.plugin, collectionId);
                        Treasure treasure = new Treasure(
                           this.treasureManager.generateUniqueTreasureId(), collectionId, loc, rewards, null, "ITEMS_ADDER", namespacedId
                        );
                        this.treasureManager.addTreasure(treasure);
                        player.sendMessage(this.plugin.getMessageManager().getMessage("treasure.added"));
                        this.plugin.getSoundManager().playTreasurePlaced(player);
                     }
                  }
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGH
   )
   public void onFurnitureBreak(FurnitureBreakEvent event) {
      Player player = event.getPlayer();
      Entity entity = event.getBukkitEntity();
      Location loc = toBlockLocation(entity.getLocation());
      TreasureCore core = this.treasureManager.getTreasureCoreAt(loc);
      if (core != null) {
         if (this.placeModeManager.isInPlaceMode(player)) {
            Treasure treasure = this.treasureManager.getFullTreasure(core.getId());
            if (treasure != null) {
               this.treasureManager.deleteTreasure(treasure);
               player.sendMessage(this.plugin.getMessageManager().getMessage("treasure.removed"));
               this.plugin.getSoundManager().playTreasureBroken(player);
            } else {
               event.setCancelled(true);
               player.sendMessage(this.plugin.getMessageManager().getMessage("command.place_mode.no_break"));
               this.plugin.getSoundManager().playPlaceModeBreakDeny(player);
            }
         } else {
            event.setCancelled(true);
            player.sendMessage(this.plugin.getMessageManager().getMessage("treasure.protected"));
            this.plugin.getSoundManager().playBlockProtected(player);
         }
      }
   }

   @EventHandler
   public void onFurnitureInteract(FurnitureInteractEvent event) {
      Entity entity = event.getBukkitEntity();
      Location loc = toBlockLocation(entity.getLocation());
      TreasureCore treasureCore = this.treasureManager.getTreasureCoreAt(loc);
      if (treasureCore != null) {
         Player player = event.getPlayer();
         event.setCancelled(true);
         if (!this.treasureInteractionHandler.handleSneakRewardsEditor(player, treasureCore)) {
            this.treasureInteractionHandler.handleTreasureCollect(player, treasureCore);
         }
      }
   }

   @EventHandler
   public void onCustomBlockInteract(CustomBlockInteractEvent event) {
      if (event.getAction() != null) {
         if (event.getBlockClicked() != null) {
            TreasureCore treasureCore = this.treasureManager.getTreasureCoreAt(event.getBlockClicked().getLocation());
            if (treasureCore != null) {
               event.setCancelled(true);
               Player player = event.getPlayer();
               if (player != null) {
                  if (!this.treasureInteractionHandler.handleSneakRewardsEditor(player, treasureCore)) {
                     this.treasureInteractionHandler.handleTreasureCollect(player, treasureCore);
                  }
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGH
   )
   public void onCustomBlockBreak(CustomBlockBreakEvent event) {
      if (event.getBlock() != null) {
         TreasureCore core = this.treasureManager.getTreasureCoreAt(event.getBlock().getLocation());
         if (core != null) {
            Player player = event.getPlayer();
            if (player != null) {
               if (!this.placeModeManager.isInPlaceMode(player)) {
                  event.setCancelled(true);
                  player.sendMessage(this.plugin.getMessageManager().getMessage("treasure.protected"));
                  this.plugin.getSoundManager().playBlockProtected(player);
               }
            }
         }
      }
   }

   private static Location toBlockLocation(Location location) {
      return location.getBlock().getLocation();
   }
}
