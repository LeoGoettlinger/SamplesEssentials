package com.sk89q.worldedit.function.mask;

import com.fastasyncworldedit.core.math.MutableBlockVector3;
import com.google.common.base.Preconditions;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.math.BlockVector3;
import javax.annotation.Nullable;

public class OffsetMask extends AbstractMask {
   private final int minY;
   private final int maxY;
   private Mask mask;
   private BlockVector3 offset;

   @Deprecated
   public OffsetMask(Mask mask, BlockVector3 offset) {
      this(
         mask,
         offset,
         WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.WORLD_EDITING).versionMinY(),
         WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.WORLD_EDITING).versionMaxY()
      );
   }

   public OffsetMask(Mask mask, BlockVector3 offset, int minY, int maxY) {
      Preconditions.checkNotNull(mask);
      Preconditions.checkNotNull(offset);
      this.mask = mask;
      this.offset = offset;
      this.minY = minY;
      this.maxY = maxY;
   }

   public Mask getMask() {
      return this.mask;
   }

   public void setMask(Mask mask) {
      Preconditions.checkNotNull(mask);
      this.mask = mask;
   }

   public BlockVector3 getOffset() {
      return this.offset;
   }

   public void setOffset(BlockVector3 offset) {
      Preconditions.checkNotNull(offset);
      this.offset = offset;
   }

   @Override
   public boolean test(BlockVector3 vector) {
      if (vector instanceof MutableBlockVector3) {
         vector = vector.toImmutable();
      }

      BlockVector3 testPos = vector.add(this.offset);
      return testPos.y() >= this.minY && testPos.y() <= this.maxY ? this.getMask().test(testPos) : false;
   }

   @Nullable
   @Override
   public Mask2D toMask2D() {
      Mask2D childMask = this.getMask().toMask2D();
      return childMask != null ? new OffsetMask2D(childMask, this.getOffset().toBlockVector2()) : null;
   }

   @Override
   public Mask copy() {
      return new OffsetMask(this.mask.copy(), this.offset.toImmutable(), this.minY, this.maxY);
   }
}
