package com.sk89q.worldedit.command.tool;

import com.fastasyncworldedit.core.configuration.Caption;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;

public class AreaPickaxe implements BlockTool {
   private final int range;

   public AreaPickaxe(int range) {
      this.range = range;
   }

   @Override
   public boolean canUse(Actor player) {
      return player.hasPermission("worldedit.superpickaxe.area");
   }

   @Override
   public boolean actPrimary(Platform server, LocalConfiguration config, Player player, LocalSession session, Location clicked) {
      int ox = clicked.getBlockX();
      int oy = clicked.getBlockY();
      int oz = clicked.getBlockZ();
      BlockType initialType = clicked.getExtent().getBlock(clicked.toVector().toBlockPoint()).getBlockType();
      if (initialType.getMaterial().isAir()) {
         return false;
      } else if (initialType == BlockTypes.BEDROCK && !player.canDestroyBedrock()) {
         return false;
      } else {
         try (EditSession editSession = session.createEditSession(player, "AreaPickaxe")) {
            editSession.getSurvivalExtent().setToolUse(config.superPickaxeManyDrop);
            int maxY = editSession.getMaxY();

            try {
               for (int x = ox - this.range; x <= ox + this.range; x++) {
                  for (int y = oy - this.range; y <= oy + this.range && y <= maxY; y++) {
                     for (int z = oz - this.range; z <= oz + this.range; z++) {
                        if (initialType.equals(editSession.getBlock(x, y, z).getBlockType()) && editSession.setBlock(x, y, z, BlockTypes.AIR.getDefaultState())
                           )
                         {
                           BlockVector3 pos = BlockVector3.at(x, y, z);
                           ((World)clicked.getExtent()).queueBlockBreakEffect(server, pos, initialType, clicked.toVector().toBlockPoint().distanceSq(pos));
                        }
                     }
                  }
               }

               editSession.flushQueue();
            } catch (MaxChangedBlocksException var22) {
               player.print(Caption.of("worldedit.tool.max-block-changes"));
            } finally {
               session.remember(editSession);
            }
         }

         return true;
      }
   }
}
