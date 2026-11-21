package com.sk89q.worldedit.function.mask;

import com.fastasyncworldedit.core.function.mask.ABlockMask;
import com.fastasyncworldedit.core.function.mask.SingleBlockTypeMask;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.block.BlockTypesCache;

public class InverseSingleBlockTypeMask extends ABlockMask {
   private final int internalId;
   private final boolean replacesAir;

   public InverseSingleBlockTypeMask(Extent extent, BlockType type) {
      super(extent);
      this.internalId = type.getInternalId();
      this.replacesAir = type.getMaterial().isAir();
   }

   @Override
   public final boolean test(BlockState state) {
      return state.getBlockType().getInternalId() != this.internalId;
   }

   @Override
   public Mask inverse() {
      return new SingleBlockTypeMask(this.getExtent(), BlockTypesCache.values[this.internalId]);
   }

   public BlockType getBlockType() {
      return BlockTypes.get(this.internalId);
   }

   @Override
   public Mask copy() {
      return this;
   }

   @Override
   public boolean replacesAir() {
      return this.replacesAir;
   }
}
