package com.fastasyncworldedit.core.regions;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;

@Deprecated
public class RegionWrapper extends CuboidRegion {
   private static final RegionWrapper GLOBAL = new RegionWrapper(
      Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE
   );
   public int minX;
   public int maxX;
   public int minY;
   public int maxY;
   public int minZ;
   public int maxZ;

   @Deprecated
   public RegionWrapper(final int minX, final int maxX, final int minZ, final int maxZ) {
      this(
         minX,
         maxX,
         WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.WORLD_EDITING).versionMinY(),
         WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.WORLD_EDITING).versionMaxY(),
         minZ,
         maxZ
      );
   }

   public RegionWrapper(final int minX, final int maxX, final int minY, final int maxY, final int minZ, final int maxZ) {
      this(BlockVector3.at(minX, minY, minZ), BlockVector3.at(maxX, maxY, maxZ));
   }

   public RegionWrapper(final BlockVector3 pos1, final BlockVector3 pos2) {
      super(pos1, pos2);
      this.minX = Math.min(pos1.x(), pos2.x());
      this.minZ = Math.min(pos1.z(), pos2.z());
      this.maxX = Math.max(pos1.x(), pos2.x());
      this.maxZ = Math.max(pos1.z(), pos2.z());
      this.minY = Math.min(pos1.y(), pos2.y());
      this.maxY = Math.max(pos1.y(), pos2.y());
   }

   public static RegionWrapper GLOBAL() {
      return GLOBAL;
   }

   @Override
   protected void recalculate() {
      super.recalculate();
      BlockVector3 pos1 = this.getMinimumPoint();
      BlockVector3 pos2 = this.getMaximumPoint();
      this.minX = Math.min(pos1.x(), pos2.x());
      this.minZ = Math.min(pos1.z(), pos2.z());
      this.maxX = Math.max(pos1.x(), pos2.x());
      this.maxZ = Math.max(pos1.z(), pos2.z());
      this.minY = Math.min(pos1.y(), pos2.y());
      this.maxY = Math.max(pos1.y(), pos2.y());
   }

   public RegionWrapper[] toArray() {
      return new RegionWrapper[]{this};
   }

   public boolean isIn(int x, int y, int z) {
      return x >= this.minX && x <= this.maxX && z >= this.minZ && z <= this.maxZ && y >= this.minY && y <= this.maxY;
   }

   public boolean isInChunk(int cx, int cz) {
      int bx = cx << 4;
      int bz = cz << 4;
      int tx = bx + 15;
      int tz = bz + 15;
      return tx >= this.minX && bx <= this.maxX && tz >= this.minZ && bz <= this.maxZ;
   }

   public boolean isInMCA(int mcaX, int mcaZ) {
      int bx = mcaX << 9;
      int bz = mcaZ << 9;
      int tx = bx + 511;
      int tz = bz + 511;
      return tx >= this.minX && bx <= this.maxX && tz >= this.minZ && bz <= this.maxZ;
   }

   public boolean isIn(final int x, final int z) {
      return x >= this.minX && x <= this.maxX && z >= this.minZ && z <= this.maxZ;
   }

   public int distanceX(int x) {
      if (x >= this.minX) {
         return x <= this.maxX ? 0 : this.maxX - x;
      } else {
         return this.minX - x;
      }
   }

   public int distanceZ(int z) {
      if (z >= this.minZ) {
         return z <= this.maxZ ? 0 : this.maxZ - z;
      } else {
         return this.minZ - z;
      }
   }

   public boolean intersects(RegionWrapper other) {
      return other.minX <= this.maxX && other.maxX >= this.minX && other.minZ <= this.maxZ && other.maxZ >= this.minZ;
   }

   public int distance(int x, int z) {
      if (this.isIn(x, z)) {
         return 0;
      } else {
         int dx1 = Math.abs(x - this.minX);
         int dx2 = Math.abs(x - this.maxX);
         int dz1 = Math.abs(z - this.minZ);
         int dz2 = Math.abs(z - this.maxZ);
         if (x >= this.minX && x <= this.maxX) {
            return Math.min(dz1, dz2);
         } else if (z >= this.minZ && z <= this.maxZ) {
            return Math.min(dx1, dx2);
         } else {
            int dx = Math.min(dx1, dx2);
            int dz = Math.min(dz1, dz2);
            return (int)Math.sqrt(dx * dx + dz * dz);
         }
      }
   }

   @Override
   public String toString() {
      return this.minX + "," + this.minY + "," + this.minZ + "->" + this.maxX + "," + this.maxY + "," + this.maxZ;
   }

   @Override
   public boolean isGlobal() {
      return this.minX == Integer.MIN_VALUE
         && this.minY == Integer.MIN_VALUE
         && this.minZ == Integer.MIN_VALUE
         && this.maxX == Integer.MAX_VALUE
         && this.maxY == Integer.MAX_VALUE
         && this.maxZ == Integer.MAX_VALUE;
   }

   public boolean contains(RegionWrapper current) {
      return current.minX >= this.minX && current.maxX <= this.maxX && current.minZ >= this.minZ && current.maxZ <= this.maxZ;
   }
}
