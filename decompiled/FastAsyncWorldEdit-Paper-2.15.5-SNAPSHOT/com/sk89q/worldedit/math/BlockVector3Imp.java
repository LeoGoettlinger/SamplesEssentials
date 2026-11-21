package com.sk89q.worldedit.math;

public final class BlockVector3Imp extends BlockVector3 {
   public static final BlockVector3Imp ZERO = new BlockVector3Imp(0, 0, 0);
   public static final BlockVector3Imp UNIT_X = new BlockVector3Imp(1, 0, 0);
   public static final BlockVector3Imp UNIT_Y = new BlockVector3Imp(0, 1, 0);
   public static final BlockVector3Imp UNIT_Z = new BlockVector3Imp(0, 0, 1);
   public static final BlockVector3Imp ONE = new BlockVector3Imp(1, 1, 1);
   private final int x;
   private final int y;
   private final int z;

   public static BlockVector3Imp at(double x, double y, double z) {
      return at((int)Math.floor(x), (int)Math.floor(y), (int)Math.floor(z));
   }

   public static BlockVector3Imp at(int x, int y, int z) {
      return new BlockVector3Imp(x, y, z);
   }

   protected BlockVector3Imp(int x, int y, int z) {
      this.x = x;
      this.y = y;
      this.z = z;
   }

   @Override
   public int x() {
      return this.x;
   }

   @Override
   public int y() {
      return this.y;
   }

   @Override
   public int z() {
      return this.z;
   }

   @Override
   public int hashCode() {
      return this.x() ^ this.z() << 12 ^ this.y() << 24;
   }

   @Override
   public BlockVector3 toImmutable() {
      return this;
   }

   @Override
   public String toString() {
      return "(" + this.x() + ", " + this.y() + ", " + this.z() + ")";
   }
}
