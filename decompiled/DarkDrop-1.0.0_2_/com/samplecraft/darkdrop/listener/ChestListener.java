package com.samplecraft.darkdrop.listener;

import com.samplecraft.darkdrop.DarkDrop;
import com.samplecraft.darkdrop.model.PendingChest;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class ChestListener implements Listener {
   private final DarkDrop plugin;

   public ChestListener(DarkDrop plugin) {
      this.plugin = plugin;
   }

   @EventHandler(
      priority = EventPriority.NORMAL,
      ignoreCancelled = true
   )
   public void onPlayerInteract(PlayerInteractEvent event) {
      if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
         Block block = event.getClickedBlock();
         if (block != null && block.getType() == Material.CHEST) {
            PendingChest pc = this.plugin.getDropManager().getPendingChest(block.getLocation());
            if (pc != null) {
               if (!pc.isConsumed()) {
                  Player var5 = event.getPlayer();
                  if (var5 instanceof Player) {
                     this.plugin.getDropManager().onChestOpen(block.getLocation(), var5);
                  }
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGH,
      ignoreCancelled = true
   )
   public void onBlockBreak(BlockBreakEvent event) {
      Block block = event.getBlock();
      if (block.getType() == Material.CHEST) {
         PendingChest pc = this.plugin.getDropManager().getPendingChest(block.getLocation());
         if (pc != null) {
            if (!pc.isConsumed()) {
               event.setCancelled(true);
               Player var5 = event.getPlayer();
               if (var5 instanceof Player) {
                  var5.sendMessage(this.plugin.getConfigManager().component("chest-protected"));
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGH,
      ignoreCancelled = true
   )
   public void onBlockExplode(BlockExplodeEvent event) {
      event.blockList()
         .removeIf(block -> block.getType() != Material.CHEST ? false : this.plugin.getDropManager().getPendingChest(block.getLocation()) != null);
   }

   @EventHandler(
      priority = EventPriority.HIGH,
      ignoreCancelled = true
   )
   public void onEntityExplode(EntityExplodeEvent event) {
      List<Block> blocks = event.blockList();
      blocks.removeIf(block -> block.getType() != Material.CHEST ? false : this.plugin.getDropManager().getPendingChest(block.getLocation()) != null);
   }

   @EventHandler(
      priority = EventPriority.HIGH,
      ignoreCancelled = true
   )
   public void onBlockPlace(BlockPlaceEvent event) {
      Block placed = event.getBlockPlaced();
      if (placed.getType() == Material.CHEST) {
         for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST}) {
            Block adjacent = placed.getRelative(face);
            if (adjacent.getType() == Material.CHEST && this.plugin.getDropManager().getPendingChest(adjacent.getLocation()) != null) {
               event.setCancelled(true);
               Player var9 = event.getPlayer();
               if (var9 instanceof Player) {
                  var9.sendMessage(this.plugin.getConfigManager().component("chest-protected"));
               }

               return;
            }
         }
      }
   }
}
