package com.fastasyncworldedit.core.command.tool.brush;

import com.fastasyncworldedit.core.function.mask.SurfaceMask;
import com.fastasyncworldedit.core.math.LocalBlockVectorSet;
import com.fastasyncworldedit.core.math.MutableBlockVector3;
import com.fastasyncworldedit.core.util.collection.BlockVector3Set;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.Masks;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.function.visitor.BreadthFirstSearch;
import com.sk89q.worldedit.math.BlockVector3;
import java.util.concurrent.ThreadLocalRandom;

public class ShatterBrush extends ScatterBrush {
   private final MutableBlockVector3 mutable = new MutableBlockVector3();

   public ShatterBrush(int count) {
      super(count, 1);
   }

   @Deprecated(
      forRemoval = true,
      since = "2.13.0"
   )
   @Override
   public void apply(final EditSession editSession, final BlockVector3Set placed, final BlockVector3 position, Pattern p, double size) throws MaxChangedBlocksException {
   }

   @Override
   public void finish(EditSession editSession, BlockVector3Set placed, final BlockVector3 position, Pattern pattern, double size) {
      int radius2 = (int)(size * size);
      LocalBlockVectorSet[] frontiers = new LocalBlockVectorSet[placed.size()];
      LocalBlockVectorSet[] frontiersVisited = new LocalBlockVectorSet[placed.size()];
      int i = 0;

      for (BlockVector3 pos : placed) {
         LocalBlockVectorSet set = new LocalBlockVectorSet();
         set.add(pos);
         frontiers[i] = set;
         frontiersVisited[i] = set.copy();
         i++;
      }

      Mask mask = editSession.getMask();
      if (mask == null) {
         mask = Masks.alwaysTrue();
      }

      Mask finalMask = mask;
      SurfaceMask surfaceTest = new SurfaceMask(editSession);
      boolean notEmpty = true;
      LocalBlockVectorSet tmp = new LocalBlockVectorSet();

      while (notEmpty) {
         notEmpty = false;

         for (int var19 = 0; var19 < frontiers.length; var19++) {
            LocalBlockVectorSet frontier = frontiers[var19];
            notEmpty |= !frontier.isEmpty();
            LocalBlockVectorSet frontierVisited = frontiersVisited[var19];
            LocalBlockVectorSet finalTmp = tmp;
            frontier.forEach((x, y, z, index) -> {
               if (ThreadLocalRandom.current().nextInt(2) == 0) {
                  finalTmp.add(x, y, z);
               } else {
                  for (int i1 = 0; i1 < BreadthFirstSearch.DIAGONAL_DIRECTIONS.length; i1++) {
                     BlockVector3 direction = BreadthFirstSearch.DIAGONAL_DIRECTIONS[i1];
                     int x2 = x + direction.x();
                     int y2 = y + direction.y();
                     int z2 = z + direction.z();
                     int dx = position.x() - x2;
                     int dy = position.y() - y2;
                     int dz = position.z() - z2;
                     int dSqr = dx * dx + dy * dy + dz * dz;
                     if (dSqr <= radius2) {
                        BlockVector3 bv = this.mutable.setComponents(x2, y2, z2);
                        if (surfaceTest.test(bv) && finalMask.test(bv)) {
                           if (!placed.add(x2, y2, z2)) {
                              if (!frontierVisited.contains(x2, y2, z2)) {
                                 editSession.setBlock(x2, y2, z2, pattern);
                              }
                           } else {
                              finalTmp.add(x2, y2, z2);
                              frontierVisited.add(x2, y2, z2);
                           }
                        }
                     }
                  }
               }
            });
            frontiers[var19] = tmp;
            tmp = frontier;
            frontier.clear();
         }
      }
   }
}
