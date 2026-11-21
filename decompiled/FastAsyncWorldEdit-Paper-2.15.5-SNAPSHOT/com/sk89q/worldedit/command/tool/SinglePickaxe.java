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
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import javax.annotation.Nullable;

public class SinglePickaxe implements BlockTool {
   @Override
   public boolean canUse(Actor player) {
      return player.hasPermission("worldedit.superpickaxe");
   }

   @Override
   public boolean actPrimary(Platform server, LocalConfiguration config, Player player, LocalSession session, Location clicked, @Nullable Direction face) {
      World world = (World)clicked.getExtent();
      BlockVector3 blockPoint = clicked.toBlockPoint();
      BlockType blockType = world.getBlock(blockPoint).getBlockType();
      if (blockType == BlockTypes.BEDROCK && !player.canDestroyBedrock()) {
         return false;
      } else {
         try (EditSession editSession = session.createEditSession(player, null)) {
            try {
               editSession.getSurvivalExtent().setToolUse(config.superPickaxeDrop);
               editSession.setBlock(blockPoint, BlockTypes.AIR.getDefaultState());
            } catch (MaxChangedBlocksException var18) {
               player.print(Caption.of("worldedit.tool.max-block-changes"));
            } finally {
               session.remember(editSession);
            }
         }

         return true;
      }
   }
}
