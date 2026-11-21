package com.fastasyncworldedit.core.extent;

import com.fastasyncworldedit.core.math.MutableBlockVector3;
import com.google.common.base.Preconditions;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockStateHolder;

public class SourceMaskExtent extends TemporalExtent {
   private Mask mask;
   private final MutableBlockVector3 mutable = new MutableBlockVector3();

   public SourceMaskExtent(Extent extent, Mask mask) {
      super(extent);
      Preconditions.checkNotNull(mask);
      this.mask = mask;
   }

   public Mask getMask() {
      return this.mask;
   }

   public void setMask(Mask mask) {
      Preconditions.checkNotNull(mask);
      this.mask = mask;
   }

   @Override
   public <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 location, T block) throws WorldEditException {
      this.set(location.x(), location.y(), location.z(), block);
      return this.mask.test(location) && super.setBlock(location.x(), location.y(), location.z(), block);
   }

   @Override
   public <T extends BlockStateHolder<T>> boolean setBlock(int x, int y, int z, T block) throws WorldEditException {
      this.set(x, y, z, block);
      this.mutable.mutX(x);
      this.mutable.mutY(y);
      this.mutable.mutZ(z);
      return this.mask.test(this.mutable) && super.setBlock(x, y, z, block);
   }
}
