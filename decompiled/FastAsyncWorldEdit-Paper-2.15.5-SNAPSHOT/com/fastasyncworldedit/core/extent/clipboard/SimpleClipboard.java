package com.fastasyncworldedit.core.extent.clipboard;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;

public abstract class SimpleClipboard implements Clipboard {
   protected BlockVector3 offset;
   private final BlockVector3 size;
   private final int area;
   private final int volume;
   private BlockVector3 origin;

   SimpleClipboard(BlockVector3 dimensions, BlockVector3 offset) {
      this.size = dimensions;
      this.offset = offset;
      long longVolume = (long)this.getWidth() * this.getHeight() * this.getLength();
      if (longVolume >= 2147483647L) {
         throw new IllegalArgumentException("Dimensions are too large for this clipboard format.");
      } else {
         this.area = this.getWidth() * this.getLength();
         this.volume = (int)longVolume;
         this.origin = BlockVector3.ZERO;
      }
   }

   SimpleClipboard(Region region) {
      this(region.getDimensions(), region.getMinimumPoint());
   }

   protected void setOffset(final BlockVector3 offset) {
      this.offset = offset;
   }

   @Override
   public BlockVector3 getOrigin() {
      return this.origin;
   }

   @Override
   public void setOrigin(BlockVector3 origin) {
      this.origin = origin;
   }

   @Override
   public BlockVector3 getMinimumPoint() {
      return BlockVector3.ZERO;
   }

   @Override
   public BlockVector3 getMaximumPoint() {
      return this.size.subtract(BlockVector3.ONE);
   }

   @Override
   public Region getRegion() {
      return new CuboidRegion(null, BlockVector3.ZERO, BlockVector3.at(this.getWidth() - 1, this.getHeight() - 1, this.getLength() - 1), false);
   }

   @Override
   public final BlockVector3 getDimensions() {
      return this.size;
   }

   @Override
   public final int getWidth() {
      return this.size.x();
   }

   @Override
   public final int getHeight() {
      return this.size.y();
   }

   @Override
   public final int getLength() {
      return this.size.z();
   }

   @Override
   public int getArea() {
      return this.area;
   }

   @Override
   public int getVolume() {
      return this.volume;
   }
}
