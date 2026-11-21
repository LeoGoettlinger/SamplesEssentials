package com.sk89q.worldedit.function.mask;

import com.fastasyncworldedit.core.function.mask.ABlockMask;
import com.fastasyncworldedit.core.function.mask.SingleBlockStateMask;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockState;

public class InverseSingleBlockStateMask extends ABlockMask {
   private final char ordinal;
   private final boolean isAir;

   public BlockState getBlockState() {
      return BlockState.getFromOrdinal(this.ordinal);
   }

   public InverseSingleBlockStateMask(Extent extent, BlockState state) {
      super(extent);
      this.isAir = state.isAir();
      this.ordinal = state.getOrdinalChar();
   }

   private InverseSingleBlockStateMask(Extent extent, char ordinal, boolean isAir) {
      super(extent);
      this.ordinal = ordinal;
      this.isAir = isAir;
   }

   @Override
   public boolean test(BlockVector3 vector) {
      int test = vector.getBlock(this.getExtent()).getOrdinal();
      return this.isAir && test == 0 ? false : this.ordinal != test;
   }

   @Override
   public final boolean test(BlockState state) {
      int test = state.getOrdinalChar();
      return this.isAir && test == 0 ? false : test != this.ordinal;
   }

   @Override
   public Mask inverse() {
      return new SingleBlockStateMask(this.getExtent(), BlockState.getFromOrdinal(this.ordinal));
   }

   @Override
   public Mask copy() {
      return new InverseSingleBlockStateMask(this.getExtent(), this.ordinal, this.isAir);
   }
}
