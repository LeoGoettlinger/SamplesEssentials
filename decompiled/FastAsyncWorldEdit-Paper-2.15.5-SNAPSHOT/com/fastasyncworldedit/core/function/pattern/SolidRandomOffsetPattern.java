package com.fastasyncworldedit.core.function.pattern;

import com.fastasyncworldedit.core.math.MutableBlockVector3;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.pattern.AbstractPattern;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.block.BlockTypesCache;
import java.util.SplittableRandom;

public class SolidRandomOffsetPattern extends AbstractPattern {
   private final int dx;
   private final int dy;
   private final int dz;
   private final int minY;
   private final int maxY;
   private final Pattern pattern;
   private final int dx2;
   private final int dy2;
   private final int dz2;
   private final MutableBlockVector3 mutable;
   private final SplittableRandom r;

   public SolidRandomOffsetPattern(Pattern pattern, int dx, int dy, int dz, int minY, int maxY) {
      this.pattern = pattern;
      this.dx = dx;
      this.dy = dy;
      this.dz = dz;
      this.minY = minY;
      this.maxY = maxY;
      this.dx2 = dx * 2 + 1;
      this.dy2 = dy * 2 + 1;
      this.dz2 = dz * 2 + 1;
      this.r = new SplittableRandom();
      this.mutable = new MutableBlockVector3();
   }

   public static boolean[] getTypes() {
      boolean[] types = new boolean[BlockTypes.size()];

      for (BlockType type : BlockTypesCache.values) {
         types[type.getInternalId()] = type.getMaterial().isSolid();
      }

      return types;
   }

   @Override
   public BaseBlock applyBlock(BlockVector3 position) {
      this.mutable.mutX(position.x() + this.r.nextInt(this.dx2) - this.dx);
      this.mutable.mutY(position.y() + this.r.nextInt(this.dy2) - this.dy);
      this.mutable.mutZ(position.z() + this.r.nextInt(this.dz2) - this.dz);
      if (this.mutable.y() < this.minY || this.mutable.y() > this.maxY) {
         return BlockTypes.AIR.getDefaultState().toBaseBlock();
      } else if (this.mutable.y() >= this.minY && this.mutable.y() <= this.maxY) {
         BaseBlock block = this.pattern.applyBlock(this.mutable);
         return block.getMaterial().isSolid() ? block : this.pattern.applyBlock(position);
      } else {
         return BlockTypes.AIR.getDefaultState().toBaseBlock();
      }
   }

   @Override
   public boolean apply(Extent extent, BlockVector3 get, BlockVector3 set) throws WorldEditException {
      this.mutable.mutX(set.x() + this.r.nextInt(this.dx2) - this.dx);
      this.mutable.mutY(set.y() + this.r.nextInt(this.dy2) - this.dy);
      this.mutable.mutZ(set.z() + this.r.nextInt(this.dz2) - this.dz);
      if (this.mutable.y() >= extent.getMinY() && this.mutable.y() <= extent.getMaxY()) {
         BaseBlock block = this.pattern.applyBlock(this.mutable);
         return block.getMaterial().isSolid() ? this.pattern.apply(extent, get, this.mutable) : this.pattern.apply(extent, get, set);
      } else {
         return false;
      }
   }

   @Override
   public Pattern fork() {
      return new SolidRandomOffsetPattern(this.pattern.fork(), this.dx, this.dy, this.dz, this.minY, this.maxY);
   }
}
