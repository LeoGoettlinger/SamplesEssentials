package com.fastasyncworldedit.core.function.mask;

import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.mask.AbstractExtentMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.math.BlockVector3;

public class DataMask extends AbstractExtentMask implements ResettableMask {
   private transient int data = -1;

   public DataMask(Extent extent) {
      super(extent);
   }

   @Override
   public boolean test(BlockVector3 vector) {
      if (this.data != -1) {
         return vector.getBlock(this.getExtent()).getInternalPropertiesId() == this.data;
      } else {
         this.data = vector.getBlock(this.getExtent()).getInternalPropertiesId();
         return true;
      }
   }

   @Override
   public boolean test(Extent extent, BlockVector3 vector) {
      if (this.data != -1) {
         return extent.getBlock(vector).getInternalPropertiesId() == this.data;
      } else {
         this.data = extent.getBlock(vector).getInternalPropertiesId();
         return true;
      }
   }

   @Override
   public void reset() {
      this.data = -1;
   }

   @Override
   public Mask copy() {
      return new DataMask(this.getExtent());
   }
}
