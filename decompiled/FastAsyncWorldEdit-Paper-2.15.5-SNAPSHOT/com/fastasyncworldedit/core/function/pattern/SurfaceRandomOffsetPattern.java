package com.fastasyncworldedit.core.function.pattern;

import com.fastasyncworldedit.core.math.MutableBlockVector3;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.pattern.AbstractPattern;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.function.visitor.BreadthFirstSearch;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BaseBlock;
import java.util.concurrent.ThreadLocalRandom;

public class SurfaceRandomOffsetPattern extends AbstractPattern {
   private final Pattern pattern;
   private final int moves;
   private final int minY;
   private final int maxY;
   private final MutableBlockVector3 cur;
   private final MutableBlockVector3[] buffer;
   private final MutableBlockVector3[] allowed;

   public SurfaceRandomOffsetPattern(Pattern pattern, int distance, int minY, int maxY) {
      this.pattern = pattern;
      this.minY = minY;
      this.maxY = maxY;
      this.moves = Math.min(255, distance);
      this.cur = new MutableBlockVector3();
      this.buffer = new MutableBlockVector3[BreadthFirstSearch.DIAGONAL_DIRECTIONS.length];

      for (int i = 0; i < this.buffer.length; i++) {
         this.buffer[i] = new MutableBlockVector3();
      }

      this.allowed = new MutableBlockVector3[this.buffer.length];
   }

   @Override
   public boolean apply(final Extent extent, final BlockVector3 get, final BlockVector3 set) throws WorldEditException {
      return super.apply(extent, get, set);
   }

   @Override
   public BaseBlock applyBlock(BlockVector3 position) {
      return this.pattern.applyBlock(this.travel(position));
   }

   private BlockVector3 travel(BlockVector3 pos) {
      this.cur.setComponents(pos);

      for (int move = 0; move < this.moves; move++) {
         int index = 0;

         for (int i = 0; i < this.allowed.length; i++) {
            MutableBlockVector3 next = this.buffer[i];
            BlockVector3 dir = BreadthFirstSearch.DIAGONAL_DIRECTIONS[i];
            next.setComponents(this.cur.x() + dir.x(), this.cur.y() + dir.y(), this.cur.z() + dir.z());
            if (this.allowed(next)) {
               this.allowed[index++] = next;
            }
         }

         if (index == 0) {
            return this.cur;
         }

         MutableBlockVector3 next = this.allowed[ThreadLocalRandom.current().nextInt(index)];
         this.cur.setComponents(next.x(), next.y(), next.z());
      }

      return this.cur;
   }

   private boolean allowed(BlockVector3 bv) {
      MutableBlockVector3 v = new MutableBlockVector3(bv);
      BaseBlock block = this.pattern.applyBlock(bv);
      if (!block.getBlockType().getMaterial().isMovementBlocker()) {
         return false;
      } else {
         int x = v.x();
         int y = v.y();
         int z = v.z();
         v.mutY(y + 1);
         if (y < this.maxY && this.canPassthrough(v)) {
            v.mutY(y);
            return true;
         } else {
            v.mutY(y - 1);
            if (y > this.minY && this.canPassthrough(v)) {
               v.mutY(y);
               return true;
            } else {
               v.mutY(y);
               v.mutX(x + 1);
               if (this.canPassthrough(v)) {
                  v.mutX(x);
                  return true;
               } else {
                  v.mutX(x - 1);
                  if (this.canPassthrough(v)) {
                     v.mutX(x);
                     return true;
                  } else {
                     v.mutX(x);
                     v.mutZ(z + 1);
                     if (this.canPassthrough(v)) {
                        v.mutZ(z);
                        return true;
                     } else {
                        v.mutZ(z - 1);
                        if (this.canPassthrough(v)) {
                           v.mutZ(z);
                           return true;
                        } else {
                           v.mutZ(z);
                           return false;
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private boolean canPassthrough(BlockVector3 v) {
      BaseBlock block = this.pattern.applyBlock(v);
      return !block.getBlockType().getMaterial().isMovementBlocker();
   }

   @Override
   public BlockVector3 size() {
      return BlockVector3.at(this.moves, this.moves, this.moves);
   }

   @Override
   public Pattern fork() {
      return new SurfaceRandomOffsetPattern(this.pattern.fork(), this.moves, this.minY, this.maxY);
   }
}
