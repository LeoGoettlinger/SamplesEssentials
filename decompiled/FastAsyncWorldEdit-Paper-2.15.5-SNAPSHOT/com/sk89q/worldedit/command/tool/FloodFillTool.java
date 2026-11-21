package com.sk89q.worldedit.command.tool;

import com.fastasyncworldedit.core.configuration.Caption;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.function.block.BlockReplace;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.function.visitor.RecursiveVisitor;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import java.util.Set;
import javax.annotation.Nullable;

public class FloodFillTool implements BlockTool {
   private final int range;
   private final Pattern pattern;

   public FloodFillTool(int range, Pattern pattern) {
      this.range = range;
      this.pattern = pattern;
   }

   @Override
   public boolean canUse(Actor player) {
      return player.hasPermission("worldedit.tool.flood-fill");
   }

   @Override
   public boolean actPrimary(Platform server, LocalConfiguration config, Player player, LocalSession session, Location clicked, @Nullable Direction face) {
      World world = (World)clicked.getExtent();
      BlockVector3 origin = clicked.toVector().toBlockPoint();
      BlockType initialType = world.getBlock(origin).getBlockType();
      if (initialType.getMaterial().isAir()) {
         return true;
      } else if (initialType == BlockTypes.BEDROCK && !player.canDestroyBedrock()) {
         return true;
      } else {
         try (EditSession editSession = session.createEditSession(player, "FloodFillTool")) {
            try {
               Mask mask = initialType.toMask(editSession);
               BlockReplace function = new BlockReplace(editSession, this.pattern);
               RecursiveVisitor visitor = new RecursiveVisitor(mask, function, this.range, editSession.getMinY(), editSession.getMaxY(), editSession);
               visitor.visit(origin);
               Operations.completeLegacy(visitor);
            } catch (MaxChangedBlocksException var20) {
               player.print(Caption.of("worldedit.tool.max-block-changes"));
            } finally {
               session.remember(editSession);
            }
         }

         return true;
      }
   }

   private void recurse(EditSession editSession, BlockVector3 pos, BlockVector3 origin, int size, BlockType initialType, Set<BlockVector3> visited) throws MaxChangedBlocksException {
      if (!(origin.distance(pos) > size) && !visited.contains(pos)) {
         visited.add(pos);
         if (editSession.getBlock(pos).getBlockType() == initialType) {
            editSession.setBlock(pos, this.pattern.applyBlock(pos));
            this.recurse(editSession, pos.add(1, 0, 0), origin, size, initialType, visited);
            this.recurse(editSession, pos.add(-1, 0, 0), origin, size, initialType, visited);
            this.recurse(editSession, pos.add(0, 0, 1), origin, size, initialType, visited);
            this.recurse(editSession, pos.add(0, 0, -1), origin, size, initialType, visited);
            this.recurse(editSession, pos.add(0, 1, 0), origin, size, initialType, visited);
            this.recurse(editSession, pos.add(0, -1, 0), origin, size, initialType, visited);
         }
      }
   }
}
