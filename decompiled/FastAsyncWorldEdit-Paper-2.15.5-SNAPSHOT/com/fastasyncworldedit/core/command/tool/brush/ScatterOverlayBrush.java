package com.fastasyncworldedit.core.command.tool.brush;

import com.fastasyncworldedit.core.math.LocalBlockVectorSet;
import com.fastasyncworldedit.core.math.MutableBlockVector3;
import com.fastasyncworldedit.core.util.collection.BlockVector3Set;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;

public class ScatterOverlayBrush extends ScatterBrush {
   public ScatterOverlayBrush(int count, int distance) {
      super(count, distance);
   }

   @Deprecated(
      forRemoval = true,
      since = "2.13.0"
   )
   @Override
   public void apply(EditSession editSession, LocalBlockVectorSet placed, BlockVector3 position, Pattern p, double size) throws MaxChangedBlocksException {
      this.apply(editSession, LocalBlockVectorSet.wrap(placed), position, p, size);
   }

   @Override
   public void apply(EditSession editSession, BlockVector3Set placed, BlockVector3 pt, Pattern p, double size) throws MaxChangedBlocksException {
      int x = pt.x();
      int y = pt.y();
      int z = pt.z();
      BlockVector3 dir = this.getDirection(pt);
      if (dir == null) {
         MutableBlockVector3 mut = new MutableBlockVector3(pt);
         int yy = 0;

         label29:
         while (true) {
            if (!(yy < size)) {
               for (int yyx = 0; yyx > -size; yyx--) {
                  if ((dir = this.getDirection(mut.mutY(y - yyx))) != null) {
                     break label29;
                  }
               }

               return;
            }

            if ((dir = this.getDirection(mut.mutY(y + yy))) != null) {
               break;
            }

            yy++;
         }
      }

      editSession.setBlock(x + dir.x(), y + dir.y(), z + dir.z(), p);
   }
}
