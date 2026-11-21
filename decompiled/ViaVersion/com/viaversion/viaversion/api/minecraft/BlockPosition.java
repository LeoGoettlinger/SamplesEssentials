package com.viaversion.viaversion.api.minecraft;

public class BlockPosition {
   protected final int x;
   protected final int y;
   protected final int z;

   public BlockPosition(int x, int y, int z) {
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public BlockPosition getRelative(BlockFace face) {
      return new BlockPosition(this.x + face.modX(), this.y + face.modY(), this.z + face.modZ());
   }

   public double distanceFromCenterSquared(double x, double y, double z) {
      double dx = this.x + 0.5 - x;
      double dy = this.y + 0.5 - y;
      double dz = this.z + 0.5 - z;
      return dx * dx + dy * dy + dz * dz;
   }

   public int x() {
      return this.x;
   }

   public int y() {
      return this.y;
   }

   public int z() {
      return this.z;
   }

   public GlobalBlockPosition withDimension(String dimension) {
      return new GlobalBlockPosition(dimension, this.x, this.y, this.z);
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         BlockPosition position = (BlockPosition)o;
         if (this.x != position.x) {
            return false;
         } else {
            return this.y != position.y ? false : this.z == position.z;
         }
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int result = this.x;
      result = 31 * result + this.y;
      return 31 * result + this.z;
   }

   @Override
   public String toString() {
      return "BlockPosition{x=" + this.x + ", y=" + this.y + ", z=" + this.z + "}";
   }
}
