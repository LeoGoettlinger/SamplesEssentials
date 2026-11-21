package com.sk89q.worldedit.command.tool;

import com.fastasyncworldedit.core.function.mask.IdMask;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.function.block.BlockReplace;
import com.sk89q.worldedit.function.visitor.RecursiveVisitor;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import java.util.Set;
import javax.annotation.Nullable;

public class RecursivePickaxe implements BlockTool {
   private final double range;

   public RecursivePickaxe(double range) {
      this.range = range;
   }

   @Override
   public boolean canUse(Actor player) {
      return player.hasPermission("worldedit.superpickaxe.recursive");
   }

   @Override
   public boolean actPrimary(Platform server, LocalConfiguration config, Player player, LocalSession session, Location clicked, @Nullable Direction face) {
      World world = (World)clicked.getExtent();
      BlockVector3 pos = clicked.toBlockPoint();
      BlockVector3 origin = clicked.toVector().toBlockPoint();
      BlockType initialType = world.getBlock(origin).getBlockType();
      if (initialType.getMaterial().isAir()) {
         return false;
      } else if (initialType == BlockTypes.BEDROCK && !player.canDestroyBedrock()) {
         return false;
      } else {
         try (EditSession editSession = session.createEditSession(player, "RecursivePickaxe")) {
            editSession.getSurvivalExtent().setToolUse(config.superPickaxeManyDrop);
            int radius = (int)this.range;
            BlockReplace replace = new BlockReplace(editSession, BlockTypes.AIR.getDefaultState());
            editSession.setMask(null);
            RecursiveVisitor visitor = new RecursiveVisitor(new IdMask(editSession), replace, radius, editSession.getMinY(), editSession.getMaxY(), editSession);
            recurse(server, editSession, world, pos, origin, radius, initialType, visitor.getVisited());
            editSession.flushQueue();
            session.remember(editSession);
         }

         return true;
      }
   }

   private static void recurse(
      Platform server,
      EditSession editSession,
      World world,
      BlockVector3 pos,
      BlockVector3 origin,
      double size,
      BlockType initialType,
      Set<BlockVector3> visited
   ) throws MaxChangedBlocksException {
      double distanceSq = origin.distanceSq(pos);
      if (!(distanceSq > size * size) && !visited.contains(pos)) {
         visited.add(pos);
         if (editSession.getBlock(pos).getBlockType() == initialType) {
            editSession.setBlock(pos, BlockTypes.AIR.getDefaultState());
            world.queueBlockBreakEffect(server, pos, initialType, distanceSq);
            recurse(server, editSession, world, pos.add(1, 0, 0), origin, size, initialType, visited);
            recurse(server, editSession, world, pos.add(-1, 0, 0), origin, size, initialType, visited);
            recurse(server, editSession, world, pos.add(0, 0, 1), origin, size, initialType, visited);
            recurse(server, editSession, world, pos.add(0, 0, -1), origin, size, initialType, visited);
            recurse(server, editSession, world, pos.add(0, 1, 0), origin, size, initialType, visited);
            recurse(server, editSession, world, pos.add(0, -1, 0), origin, size, initialType, visited);
         }
      }
   }
}
