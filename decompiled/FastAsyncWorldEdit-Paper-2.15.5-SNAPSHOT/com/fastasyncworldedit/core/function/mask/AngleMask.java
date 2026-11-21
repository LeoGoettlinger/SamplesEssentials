package com.fastasyncworldedit.core.function.mask;

import com.fastasyncworldedit.core.math.MutableBlockVector3;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.mask.AbstractExtentMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.SolidBlockMask;
import com.sk89q.worldedit.math.BlockVector3;
import java.util.Arrays;

public class AngleMask extends AbstractExtentMask implements ResettableMask {
   protected static double ADJACENT_MOD = 0.5;
   protected static double DIAGONAL_MOD = 1.0 / Math.sqrt(8.0);
   private final SolidBlockMask mask;
   protected final double max;
   protected final double min;
   protected final boolean overlay;
   protected final boolean checkFirst;
   protected final int maxY;
   protected final int minY;
   protected final int distance;
   protected transient int cacheBotX = Integer.MIN_VALUE;
   protected transient int cacheBotZ = Integer.MIN_VALUE;
   protected transient short[] cacheHeights;
   protected transient int lastY;
   protected transient int lastX = Integer.MIN_VALUE;
   protected transient int lastZ = Integer.MIN_VALUE;
   protected transient boolean lastValue;

   public AngleMask(Extent extent, double min, double max, boolean overlay, int distance) {
      super(extent);
      this.mask = new SolidBlockMask(extent);
      this.min = min;
      this.max = max;
      this.checkFirst = max >= Math.tan(Math.PI / 2);
      this.maxY = extent.getMaxY();
      this.minY = extent.getMinY();
      this.overlay = overlay;
      this.distance = distance;
   }

   @Override
   public void reset() {
      this.cacheBotX = Integer.MIN_VALUE;
      this.cacheBotZ = Integer.MIN_VALUE;
      this.lastX = Integer.MIN_VALUE;
      this.lastY = Integer.MIN_VALUE;
      this.lastZ = Integer.MIN_VALUE;
      if (this.cacheHeights != null) {
         Arrays.fill(this.cacheHeights, (short)0);
      }
   }

   protected int getHeight(Extent extent, int x, int y, int z) {
      int rx = x - this.cacheBotX + 16;
      int rz = z - this.cacheBotZ + 16;
      int index;
      if ((rx & 0xFF) == rx && (rz & 0xFF) == rz) {
         index = rx + (rz << 8);
      } else {
         this.cacheBotX = x - 16;
         this.cacheBotZ = z - 16;
         rx = x - this.cacheBotX + 16;
         rz = z - this.cacheBotZ + 16;
         index = rx + (rz << 8);
         if (this.cacheHeights == null) {
            this.cacheHeights = new short[65536];
            Arrays.fill(this.cacheHeights, (short)this.minY);
         } else {
            Arrays.fill(this.cacheHeights, (short)this.minY);
         }
      }

      int result = this.cacheHeights[index];
      if (y > result) {
         this.cacheHeights[index] = (short)(result = this.lastY = extent.getNearestSurfaceTerrainBlock(x, z, this.lastY, this.minY, this.maxY));
      }

      return result;
   }

   protected boolean testSlope(Extent extent, int x, int y, int z) {
      this.lastY = y;
      double slope = Math.abs(this.getHeight(extent, x + this.distance, y, z) - this.getHeight(extent, x - this.distance, y, z)) * ADJACENT_MOD;
      if (this.checkFirst) {
         if (slope >= this.min) {
            return this.lastValue = true;
         } else {
            slope = Math.max(slope, Math.abs(this.getHeight(extent, x, y, z + this.distance) - this.getHeight(extent, x, y, z - this.distance)) * ADJACENT_MOD);
            slope = Math.max(
               slope,
               Math.abs(this.getHeight(extent, x + this.distance, y, z + this.distance) - this.getHeight(extent, x - this.distance, y, z - this.distance))
                  * DIAGONAL_MOD
            );
            slope = Math.max(
               slope,
               Math.abs(this.getHeight(extent, x - this.distance, y, z + this.distance) - this.getHeight(extent, x + this.distance, y, z - this.distance))
                  * DIAGONAL_MOD
            );
            return this.lastValue = slope >= this.min;
         }
      } else {
         slope = Math.max(slope, Math.abs(this.getHeight(extent, x, y, z + this.distance) - this.getHeight(extent, x, y, z - this.distance)) * ADJACENT_MOD);
         slope = Math.max(
            slope,
            Math.abs(this.getHeight(extent, x + this.distance, y, z + this.distance) - this.getHeight(extent, x - this.distance, y, z - this.distance))
               * DIAGONAL_MOD
         );
         slope = Math.max(
            slope,
            Math.abs(this.getHeight(extent, x - this.distance, y, z + this.distance) - this.getHeight(extent, x + this.distance, y, z - this.distance))
               * DIAGONAL_MOD
         );
         return this.lastValue = slope >= this.min && slope <= this.max;
      }
   }

   private boolean adjacentAir(Extent extent, BlockVector3 base) {
      int y = base.y();
      if (y != this.maxY && !SolidBlockMask.isSolid(base.getStateRelativeY(extent, 1))) {
         return true;
      } else if (y != this.minY && !SolidBlockMask.isSolid(base.getStateRelativeY(extent, -1))) {
         return true;
      } else {
         MutableBlockVector3 mutable = new MutableBlockVector3();
         int x = base.x();
         int z = base.z();
         if (!this.mask.test(extent, mutable.setComponents(x + 1, y, z))) {
            return true;
         } else if (!this.mask.test(extent, mutable.setComponents(x - 1, y, z))) {
            return true;
         } else {
            return !this.mask.test(extent, mutable.setComponents(x, y, z + 1)) ? true : !this.mask.test(extent, mutable.setComponents(x, y, z - 1));
         }
      }
   }

   @Override
   public boolean test(BlockVector3 vector) {
      if (!this.mask.test(vector)) {
         return false;
      } else {
         int y = vector.y();
         if (this.overlay && y < this.maxY && !this.adjacentAir(this.getExtent(), vector)) {
            return false;
         } else {
            int x = vector.x();
            int z = vector.z();
            return this.testSlope(this.getExtent(), x, y, z);
         }
      }
   }

   @Override
   public boolean test(final Extent extent, final BlockVector3 vector) {
      int x = vector.x();
      int y = vector.y();
      int z = vector.z();
      if (this.lastX == (this.lastX = x) & this.lastZ == (this.lastZ = z)) {
         int height = this.getHeight(extent, x, y, z);
         if (y <= height) {
            return this.overlay ? this.lastValue && y == height : this.lastValue;
         }
      }

      if (!this.mask.test(extent, vector)) {
         return false;
      } else {
         return this.overlay && y < this.maxY && !this.adjacentAir(extent, vector) ? (this.lastValue = false) : this.testSlope(extent, x, y, z);
      }
   }

   @Override
   public Mask copy() {
      return new AngleMask(this.getExtent(), this.min, this.max, this.overlay, this.distance);
   }
}
