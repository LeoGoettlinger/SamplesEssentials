package com.fastasyncworldedit.core.function.mask;

import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.mask.InverseSingleBlockTypeMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.block.BlockTypesCache;

public class SingleBlockTypeMask extends ABlockMask {
   private final int internalId;
   private final boolean isAir;

   public SingleBlockTypeMask(Extent extent, BlockType type) {
      super(extent);
      this.isAir = type == BlockTypes.AIR || type == BlockTypes.CAVE_AIR || type == BlockTypes.VOID_AIR;
      this.internalId = type.getInternalId();
   }

   private SingleBlockTypeMask(Extent extent, int internalId, boolean isAir) {
      super(extent);
      this.internalId = internalId;
      this.isAir = isAir;
   }

   @Override
   public final boolean test(BlockState state) {
      return state.getBlockType().getInternalId() == this.internalId;
   }

   @Override
   public Mask inverse() {
      return new InverseSingleBlockTypeMask(this.getExtent(), BlockTypesCache.values[this.internalId]);
   }

   public BlockType getBlockType() {
      return BlockTypes.get(this.internalId);
   }

   @Override
   public boolean replacesAir() {
      return this.isAir;
   }

   @Override
   public Mask copy() {
      return new SingleBlockTypeMask(this.getExtent(), this.internalId, this.isAir);
   }
}
