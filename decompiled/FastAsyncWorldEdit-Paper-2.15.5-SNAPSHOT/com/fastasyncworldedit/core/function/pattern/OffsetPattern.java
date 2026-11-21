package com.fastasyncworldedit.core.function.pattern;

import com.fastasyncworldedit.core.math.MutableBlockVector3;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.pattern.AbstractPattern;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockTypes;

public class OffsetPattern extends AbstractPattern {
   private final int dx;
   private final int dy;
   private final int dz;
   private final int minY;
   private final int maxY;
   private final transient MutableBlockVector3 mutable = new MutableBlockVector3();
   private final Pattern pattern;

   public OffsetPattern(Pattern pattern, int dx, int dy, int dz, int minY, int maxY) {
      this.dx = dx;
      this.dy = dy;
      this.dz = dz;
      this.minY = minY;
      this.maxY = maxY;
      this.pattern = pattern;
   }

   @Override
   public BaseBlock applyBlock(BlockVector3 position) {
      this.mutable.mutX(position.x() + this.dx);
      this.mutable.mutY(position.y() + this.dy);
      this.mutable.mutZ(position.z() + this.dz);
      return this.mutable.y() >= this.minY && this.mutable.y() <= this.maxY
         ? this.pattern.applyBlock(this.mutable)
         : BlockTypes.AIR.getDefaultState().toBaseBlock();
   }

   @Override
   public boolean apply(Extent extent, BlockVector3 get, BlockVector3 set) throws WorldEditException {
      this.mutable.mutX(get.x() + this.dx);
      this.mutable.mutY(get.y() + this.dy);
      this.mutable.mutZ(get.z() + this.dz);
      return this.mutable.y() >= extent.getMinY() && this.mutable.y() <= extent.getMaxY() ? this.pattern.apply(extent, get, this.mutable) : false;
   }

   @Override
   public BlockVector3 size() {
      return BlockVector3.at(this.dx, this.dy, this.dz);
   }

   @Override
   public Pattern fork() {
      return new OffsetPattern(this.pattern.fork(), this.dx, this.dy, this.dz, this.minY, this.maxY);
   }
}
