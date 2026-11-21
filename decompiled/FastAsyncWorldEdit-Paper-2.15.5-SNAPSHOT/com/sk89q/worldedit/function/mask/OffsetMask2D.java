package com.sk89q.worldedit.function.mask;

import com.fastasyncworldedit.core.math.MutableBlockVector2;
import com.google.common.base.Preconditions;
import com.sk89q.worldedit.math.BlockVector2;

public class OffsetMask2D extends AbstractMask2D {
   private Mask2D mask;
   private BlockVector2 offset;
   private final MutableBlockVector2 mutableBlockVector2;

   public OffsetMask2D(Mask2D mask, BlockVector2 offset) {
      Preconditions.checkNotNull(mask);
      Preconditions.checkNotNull(offset);
      this.mask = mask;
      this.offset = offset;
      this.mutableBlockVector2 = new MutableBlockVector2();
   }

   public Mask2D getMask() {
      return this.mask;
   }

   public void setMask(Mask2D mask) {
      Preconditions.checkNotNull(mask);
      this.mask = mask;
   }

   public BlockVector2 getOffset() {
      return this.offset;
   }

   public void setOffset(BlockVector2 offset) {
      Preconditions.checkNotNull(offset);
      this.offset = offset;
   }

   @Override
   public boolean test(BlockVector2 vector) {
      this.mutableBlockVector2.setComponents(vector.x() + this.offset.x(), vector.z() + this.offset.z());
      return this.getMask().test(this.mutableBlockVector2);
   }

   @Override
   public Mask2D copy2D() {
      return new OffsetMask2D(this.mask.copy2D(), BlockVector2.at(this.offset.x(), this.offset.z()));
   }
}
