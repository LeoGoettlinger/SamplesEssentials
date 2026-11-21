package com.fastasyncworldedit.core.function.mask;

import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.mask.InverseSingleBlockStateMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.Masks;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockState;

public class SingleBlockStateMask extends ABlockMask {
   private final char ordinal;
   private final boolean isAir;

   public BlockState getBlockState() {
      return BlockState.getFromOrdinal(this.ordinal);
   }

   public SingleBlockStateMask(Extent extent, BlockState state) {
      super(extent);
      this.isAir = state.isAir();
      this.ordinal = state.getOrdinalChar();
   }

   private SingleBlockStateMask(Extent extent, char ordinal, boolean isAir) {
      super(extent);
      this.ordinal = ordinal;
      this.isAir = isAir;
   }

   @Override
   public boolean test(BlockVector3 vector) {
      int test = vector.getBlock(this.getExtent()).getOrdinal();
      return this.ordinal == test || this.isAir && test == 0;
   }

   @Override
   public final boolean test(BlockState state) {
      return state.getOrdinalChar() == this.ordinal;
   }

   @Override
   public Mask inverse() {
      return new InverseSingleBlockStateMask(this.getExtent(), BlockState.getFromOrdinal(this.ordinal));
   }

   @Override
   public boolean replacesAir() {
      return this.isAir;
   }

   @Override
   public Mask tryCombine(Mask mask) {
      if (mask instanceof ABlockMask other) {
         return (Mask)(other.test(BlockState.getFromOrdinal(this.ordinal)) ? this : Masks.alwaysFalse());
      } else {
         return null;
      }
   }

   @Override
   public Mask copy() {
      return new SingleBlockStateMask(this.getExtent(), this.ordinal, this.isAir);
   }
}
