package com.fastasyncworldedit.core.function.mask;

import com.sk89q.worldedit.function.mask.AbstractMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.Mask2D;
import com.sk89q.worldedit.function.mask.Masks;
import com.sk89q.worldedit.math.BlockVector3;
import javax.annotation.Nullable;

public class InverseMask extends AbstractMask {
   private final Mask mask;

   public InverseMask(Mask other) {
      this.mask = other;
   }

   @Override
   public boolean test(BlockVector3 pos) {
      return !this.mask.test(pos);
   }

   @Nullable
   @Override
   public Mask2D toMask2D() {
      Mask2D mask2d = this.mask.toMask2D();
      return mask2d != null ? Masks.negate(mask2d) : null;
   }

   @Override
   public Mask inverse() {
      return this.mask;
   }

   @Override
   public Mask copy() {
      return new InverseMask(this.mask.copy());
   }

   @Override
   public boolean replacesAir() {
      return this.mask.replacesAir();
   }
}
