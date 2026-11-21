package de.theredend2000.advancedhunt.listeners;

import de.theredend2000.advancedhunt.Main;
import de.theredend2000.advancedhunt.managers.PlaceModeManager;
import de.theredend2000.advancedhunt.managers.TreasureManager;
import de.theredend2000.advancedhunt.model.TreasureCore;
import java.util.List;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.inventory.ItemStack;

public class TreasureProtectListener implements Listener {
   private final Main plugin;
   private final TreasureManager treasureManager;
   private final PlaceModeManager placeModeManager;

   public TreasureProtectListener(Main plugin) {
      this.plugin = plugin;
      this.treasureManager = plugin.getTreasureManager();
      this.placeModeManager = plugin.getPlaceModeManager();
   }

   @EventHandler(
      priority = EventPriority.HIGH
   )
   public void onBlockBreak(BlockBreakEvent event) {
      TreasureCore core = this.treasureManager.getTreasureCoreAt(event.getBlock().getLocation());
      if (core != null) {
         Player player = event.getPlayer();
         if (!this.placeModeManager.isInPlaceMode(player)) {
            event.setCancelled(true);
            if (player.hasPermission("advancedhunt.admin.place")) {
               player.sendMessage(this.plugin.getMessageManager().getMessage("treasure.protected"));
               this.plugin.getSoundManager().playBlockProtected(event.getPlayer());
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGH
   )
   public void onBlockPlace(BlockPlaceEvent event) {
      TreasureCore core = this.treasureManager.getTreasureCoreAt(event.getBlock().getLocation());
      if (core != null) {
         Player player = event.getPlayer();
         if (!this.placeModeManager.isInPlaceMode(player)) {
            event.setCancelled(true);
            if (player.hasPermission("advancedhunt.admin.place")) {
               player.sendMessage(this.plugin.getMessageManager().getMessage("treasure.protected"));
               this.plugin.getSoundManager().playBlockProtected(player);
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGH
   )
   public void onBlockExplode(BlockExplodeEvent event) {
      this.handleExplosion(event.blockList());
   }

   @EventHandler(
      priority = EventPriority.HIGH
   )
   public void onEntityExplode(EntityExplodeEvent event) {
      this.handleExplosion(event.blockList());
   }

   private void handleExplosion(List<Block> blocks) {
      blocks.removeIf(block -> this.treasureManager.getTreasureCoreAt(block.getLocation()) != null);
   }

   @EventHandler(
      priority = EventPriority.HIGH
   )
   public void onPistonExtend(BlockPistonExtendEvent event) {
      for (Block block : event.getBlocks()) {
         if (this.treasureManager.getTreasureCoreAt(block.getLocation()) != null) {
            event.setCancelled(true);
            return;
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGH
   )
   public void onPistonRetract(BlockPistonRetractEvent event) {
      for (Block block : event.getBlocks()) {
         if (this.treasureManager.getTreasureCoreAt(block.getLocation()) != null) {
            event.setCancelled(true);
            return;
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGH
   )
   public void onBlockBurn(BlockBurnEvent event) {
      if (this.treasureManager.getTreasureCoreAt(event.getBlock().getLocation()) != null) {
         event.setCancelled(true);
      }
   }

   @EventHandler(
      priority = EventPriority.HIGH
   )
   public void onEntityChangeBlock(EntityChangeBlockEvent event) {
      if (this.treasureManager.getTreasureCoreAt(event.getBlock().getLocation()) != null) {
         event.setCancelled(true);
      }
   }

   @EventHandler(
      priority = EventPriority.HIGH
   )
   public void onBlockFromTo(BlockFromToEvent event) {
      if (this.treasureManager.getTreasureCoreAt(event.getToBlock().getLocation()) != null) {
         event.setCancelled(true);
      }
   }

   @EventHandler(
      priority = EventPriority.HIGH
   )
   public void onBlockFade(BlockFadeEvent event) {
      if (this.treasureManager.getTreasureCoreAt(event.getBlock().getLocation()) != null) {
         event.setCancelled(true);
      }
   }

   @EventHandler(
      priority = EventPriority.HIGH
   )
   public void onLeavesDecay(LeavesDecayEvent event) {
      if (this.treasureManager.getTreasureCoreAt(event.getBlock().getLocation()) != null) {
         event.setCancelled(true);
      }
   }

   @EventHandler(
      priority = EventPriority.HIGH
   )
   public void onBlockPhysics(BlockPhysicsEvent event) {
      if (this.treasureManager.getTreasureCoreAt(event.getBlock().getLocation()) != null) {
         event.setCancelled(true);
      }
   }

   @EventHandler(
      priority = EventPriority.HIGH
   )
   public void onBucketEmpty(PlayerBucketEmptyEvent event) {
      Block targetBlock = event.getBlockClicked().getRelative(event.getBlockFace());
      if (this.treasureManager.getTreasureCoreAt(targetBlock.getLocation()) != null
         || this.treasureManager.getTreasureCoreAt(event.getBlockClicked().getLocation()) != null) {
         event.setCancelled(true);
      }
   }

   @EventHandler(
      priority = EventPriority.HIGH
   )
   public void onBucketFill(PlayerBucketFillEvent event) {
      Block targetBlock = event.getBlockClicked().getRelative(event.getBlockFace());
      if (this.treasureManager.getTreasureCoreAt(targetBlock.getLocation()) != null
         || this.treasureManager.getTreasureCoreAt(event.getBlockClicked().getLocation()) != null) {
         event.setCancelled(true);
      }
   }

   @EventHandler(
      priority = EventPriority.HIGH
   )
   public void onBlockDispense(BlockDispenseEvent event) {
      ItemStack item = event.getItem();
      if (item.getType().toString().contains("BUCKET")) {
         BlockData data = event.getBlock().getBlockData();
         if (data instanceof Directional) {
            Block targetBlock = event.getBlock().getRelative(((Directional)data).getFacing());
            if (this.treasureManager.getTreasureCoreAt(targetBlock.getLocation()) != null) {
               event.setCancelled(true);
            }
         }
      }
   }
}
