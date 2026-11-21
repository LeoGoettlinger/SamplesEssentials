package com.amethysttools.listener;

import com.amethysttools.AmethystToolService;
import com.amethysttools.AmethystToolsPlugin;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Queue;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class AmethystPickaxeListener implements Listener {
   private static final int MAX_TREE_BLOCKS = 128;
   private final AmethystToolsPlugin plugin;
   private final AmethystToolService toolService;

   public AmethystPickaxeListener(AmethystToolsPlugin plugin, AmethystToolService toolService) {
      this.plugin = plugin;
      this.toolService = toolService;
   }

   @EventHandler(
      priority = EventPriority.HIGH,
      ignoreCancelled = true
   )
   public void onBreak(BlockBreakEvent event) {
      Player player = event.getPlayer();
      ItemStack tool = player.getInventory().getItemInMainHand();
      if (this.toolService.isAmethystTool(tool)) {
         if (!player.hasPermission("amethysttools.use")) {
            event.setCancelled(true);
            player.sendMessage(this.toolService.msg("no-permission"));
         } else if (this.toolService.isExpired(tool)) {
            player.getInventory().setItemInMainHand(null);
            event.setCancelled(true);
            player.sendMessage(this.toolService.msg("tool-expired"));
         } else {
            Block origin = event.getBlock();
            if (this.toolService.isBlocked(origin.getType())) {
               event.setCancelled(true);
            } else if (this.toolService.isToolItem(tool, "amethyst_axe")) {
               this.handleAxeBreak(origin, tool);
            } else {
               int radius = this.toolService.getRadius();
               int depth = this.toolService.getDepth();
               BlockFace face = player.getTargetBlockFace(6);
               if (face == null) {
                  face = this.getFallbackFace(player);
               }

               Set<Block> extraBlocks = this.collectExtraBlocks(origin, face, radius, depth);
               extraBlocks.remove(origin);

               for (Block block : extraBlocks) {
                  if (block.getType() != Material.AIR && !this.toolService.isBlocked(block.getType())) {
                     block.breakNaturally(tool);
                  }
               }
            }
         }
      }
   }

   private void handleAxeBreak(Block origin, ItemStack tool) {
      if (this.isTreeLog(origin.getType())) {
         Set<Block> treeBlocks = this.collectMainTrunkBlocks(origin);
         treeBlocks.remove(origin);

         for (Block block : treeBlocks) {
            if (block.getType() != Material.AIR) {
               block.breakNaturally(tool);
            }
         }
      }
   }

   @EventHandler
   public void onJoin(PlayerJoinEvent event) {
      Player player = event.getPlayer();
      ItemStack[] content = player.getInventory().getContents();

      for (int i = 0; i < content.length; i++) {
         content[i] = this.toolService.updateOrExpire(content[i], player);
      }

      player.getInventory().setContents(content);
   }

   private Set<Block> collectExtraBlocks(Block origin, BlockFace face, int radius, int depth) {
      Set<Block> result = new HashSet<>();
      int stepX = -face.getModX();
      int stepY = -face.getModY();
      int stepZ = -face.getModZ();

      for (int d = 0; d < depth; d++) {
         int baseX = origin.getX() + stepX * d;
         int baseY = origin.getY() + stepY * d;
         int baseZ = origin.getZ() + stepZ * d;
         if (Math.abs(face.getModY()) == 1) {
            for (int x = -radius; x <= radius; x++) {
               for (int z = -radius; z <= radius; z++) {
                  result.add(origin.getWorld().getBlockAt(baseX + x, baseY, baseZ + z));
               }
            }
         } else if (Math.abs(face.getModX()) == 1) {
            for (int y = -radius; y <= radius; y++) {
               for (int z = -radius; z <= radius; z++) {
                  result.add(origin.getWorld().getBlockAt(baseX, baseY + y, baseZ + z));
               }
            }
         } else {
            for (int x = -radius; x <= radius; x++) {
               for (int y = -radius; y <= radius; y++) {
                  result.add(origin.getWorld().getBlockAt(baseX + x, baseY + y, baseZ));
               }
            }
         }
      }

      return result;
   }

   private Set<Block> collectMainTrunkBlocks(Block origin) {
      Set<Block> result = new LinkedHashSet<>();
      Queue<Block> queue = new ArrayDeque<>();
      Material trunkType = origin.getType();
      queue.add(origin);

      while (!queue.isEmpty() && result.size() < 128) {
         Block current = queue.poll();
         if (!result.contains(current) && current.getType() == trunkType) {
            result.add(current);
            this.addVertical(queue, result, current, trunkType, BlockFace.UP);
            this.addVertical(queue, result, current, trunkType, BlockFace.DOWN);

            for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST}) {
               Block side = current.getRelative(face);
               if (!result.contains(side) && side.getType() == trunkType && this.hasVerticalTrunkConnection(side, trunkType)) {
                  queue.add(side);
               }
            }
         }
      }

      return result;
   }

   private void addVertical(Queue<Block> queue, Set<Block> visited, Block start, Material type, BlockFace direction) {
      for (Block next = start.getRelative(direction); next.getType() == type && visited.size() + queue.size() < 128; next = next.getRelative(direction)) {
         if (!visited.contains(next)) {
            queue.add(next);
         }
      }
   }

   private boolean hasVerticalTrunkConnection(Block block, Material type) {
      return block.getRelative(BlockFace.UP).getType() == type || block.getRelative(BlockFace.DOWN).getType() == type;
   }

   private boolean isTreeLog(Material material) {
      return material.name().endsWith("_LOG");
   }

   private BlockFace getFallbackFace(Player player) {
      Vector dir = player.getLocation().getDirection();
      double ax = Math.abs(dir.getX());
      double ay = Math.abs(dir.getY());
      double az = Math.abs(dir.getZ());
      if (ay >= ax && ay >= az) {
         return dir.getY() > 0.0 ? BlockFace.DOWN : BlockFace.UP;
      } else if (ax >= az) {
         return dir.getX() > 0.0 ? BlockFace.WEST : BlockFace.EAST;
      } else {
         return dir.getZ() > 0.0 ? BlockFace.NORTH : BlockFace.SOUTH;
      }
   }
}
