package com.fastasyncworldedit.core.function.pattern;

import com.fastasyncworldedit.core.math.MutableBlockVector3;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.pattern.AbstractPattern;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockTypes;

public class RelativePattern extends AbstractPattern implements ResettablePattern {
   private final Pattern pattern;
   private final int minY;
   private final int maxY;
   private final MutableBlockVector3 mutable = new MutableBlockVector3();
   private BlockVector3 origin;

   public RelativePattern(Pattern pattern, int minY, int maxY) {
      this.pattern = pattern;
      this.minY = minY;
      this.maxY = maxY;
   }

   @Override
   public BaseBlock applyBlock(BlockVector3 pos) {
      if (this.origin == null) {
         this.origin = pos;
      }

      this.mutable.mutX(pos.x() - this.origin.x());
      this.mutable.mutY(pos.y() - this.origin.y());
      this.mutable.mutZ(pos.z() - this.origin.z());
      return this.mutable.y() >= this.minY && this.mutable.y() <= this.maxY
         ? this.pattern.applyBlock(this.mutable)
         : BlockTypes.AIR.getDefaultState().toBaseBlock();
   }

   @Override
   public boolean apply(Extent extent, BlockVector3 get, BlockVector3 set) throws WorldEditException {
      if (this.origin == null) {
         this.origin = set;
      }

      this.mutable.mutX(set.x() - this.origin.x());
      this.mutable.mutY(set.y() - this.origin.y());
      this.mutable.mutZ(set.z() - this.origin.z());
      return this.mutable.y() >= extent.getMinY() && this.mutable.y() <= extent.getMaxY() ? this.pattern.apply(extent, get, this.mutable) : false;
   }

   @Override
   public void reset() {
      this.origin = null;
   }

   @Override
   public Pattern fork() {
      RelativePattern forked = new RelativePattern(this.pattern.fork(), this.minY, this.maxY);
      forked.origin = this.origin;
      return forked;
   }
}
