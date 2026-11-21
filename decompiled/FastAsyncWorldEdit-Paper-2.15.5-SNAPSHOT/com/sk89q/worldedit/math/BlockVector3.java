package com.sk89q.worldedit.math;

import com.fastasyncworldedit.core.math.MutableBlockVector3;
import com.google.common.base.Preconditions;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import java.util.Comparator;

public abstract class BlockVector3 {
   public static final BlockVector3 ZERO = at(0, 0, 0);
   public static final BlockVector3 UNIT_X = at(1, 0, 0);
   public static final BlockVector3 UNIT_Y = at(0, 1, 0);
   public static final BlockVector3 UNIT_Z = at(0, 0, 1);
   public static final BlockVector3 UNIT_MINUS_X = at(-1, 0, 0);
   public static final BlockVector3 UNIT_MINUS_Y = at(0, -1, 0);
   public static final BlockVector3 UNIT_MINUS_Z = at(0, 0, -1);
   public static final BlockVector3 ONE = at(1, 1, 1);
   private static final int WORLD_XZ_MINMAX = 30000000;
   private static final int WORLD_Y_MIN = -2048;
   private static final int WORLD_Y_MAX = 2047;
   private static final long BITS_26 = BitMath.mask(26);
   private static final long BITS_12 = BitMath.mask(12);

   public static BlockVector3 at(double x, double y, double z) {
      return at((int)Math.floor(x), (int)Math.floor(y), (int)Math.floor(z));
   }

   public static BlockVector3 at(int x, int y, int z) {
      return new BlockVector3Imp(x, y, z);
   }

   private static boolean isHorizontallyInBounds(int h) {
      return -30000000 <= h && h <= 30000000;
   }

   public static boolean isLongPackable(BlockVector3 location) {
      return isHorizontallyInBounds(location.x()) && isHorizontallyInBounds(location.z()) && -2048 <= location.y() && location.y() <= 2047;
   }

   public static void checkLongPackable(BlockVector3 location) {
      Preconditions.checkArgument(isLongPackable(location), "Location exceeds long packing limits: %s", location);
   }

   public static BlockVector3 fromLongPackedForm(long packed) {
      return at(BitMath.unpackX(packed), BitMath.unpackY(packed), BitMath.unpackZ(packed));
   }

   public static Comparator<BlockVector3> sortByCoordsYzx() {
      return BlockVector3.YzxOrderComparator.YZX_ORDER;
   }

   public boolean isAt(int x, int y, int z) {
      return this.x() == x && this.y() == y && this.z() == z;
   }

   public MutableBlockVector3 setComponents(double x, double y, double z) {
      return new MutableBlockVector3((int)x, (int)y, (int)z);
   }

   public MutableBlockVector3 setComponents(int x, int y, int z) {
      return new MutableBlockVector3(x, y, z);
   }

   public long toLongPackedForm() {
      checkLongPackable(this);
      return this.x() & BITS_26 | (this.z() & BITS_26) << 26 | (this.y() & BITS_12) << 52;
   }

   public MutableBlockVector3 mutX(double x) {
      return new MutableBlockVector3((int)x, this.y(), this.z());
   }

   public MutableBlockVector3 mutY(double y) {
      return new MutableBlockVector3(this.x(), (int)y, this.z());
   }

   public MutableBlockVector3 mutZ(double z) {
      return new MutableBlockVector3(this.x(), this.y(), (int)z);
   }

   public MutableBlockVector3 mutX(int x) {
      return new MutableBlockVector3(x, this.y(), this.z());
   }

   public MutableBlockVector3 mutY(int y) {
      return new MutableBlockVector3(this.x(), y, this.z());
   }

   public MutableBlockVector3 mutZ(int z) {
      return new MutableBlockVector3(this.x(), this.y(), z);
   }

   public BlockVector3 toImmutable() {
      return at(this.x(), this.y(), this.z());
   }

   public abstract int x();

   @Deprecated(
      forRemoval = true,
      since = "2.11.0"
   )
   public int getX() {
      return this.x();
   }

   @Deprecated(
      forRemoval = true,
      since = "2.11.0"
   )
   public int getBlockX() {
      return this.x();
   }

   public BlockVector3 withX(int x) {
      return at(x, this.y(), this.z());
   }

   public abstract int y();

   @Deprecated(
      forRemoval = true,
      since = "2.11.0"
   )
   public int getY() {
      return this.y();
   }

   @Deprecated(
      forRemoval = true,
      since = "2.11.0"
   )
   public int getBlockY() {
      return this.y();
   }

   public BlockVector3 withY(int y) {
      return at(this.x(), y, this.z());
   }

   public abstract int z();

   @Deprecated(
      forRemoval = true,
      since = "2.11.0"
   )
   public int getZ() {
      return this.z();
   }

   @Deprecated(
      forRemoval = true,
      since = "2.11.0"
   )
   public int getBlockZ() {
      return this.z();
   }

   public BlockVector3 withZ(int z) {
      return at(this.x(), this.y(), z);
   }

   public BlockVector3 add(BlockVector3 other) {
      return this.add(other.x(), other.y(), other.z());
   }

   public BlockVector3 add(int x, int y, int z) {
      return at(this.x() + x, this.y() + y, this.z() + z);
   }

   public BlockVector3 add(BlockVector3... others) {
      int newX = this.x();
      int newY = this.y();
      int newZ = this.z();

      for (BlockVector3 other : others) {
         newX += other.x();
         newY += other.y();
         newZ += other.z();
      }

      return at(newX, newY, newZ);
   }

   public BlockVector3 subtract(BlockVector3 other) {
      return this.subtract(other.x(), other.y(), other.z());
   }

   public BlockVector3 subtract(int x, int y, int z) {
      return at(this.x() - x, this.y() - y, this.z() - z);
   }

   public BlockVector3 subtract(BlockVector3... others) {
      int newX = this.x();
      int newY = this.y();
      int newZ = this.z();

      for (BlockVector3 other : others) {
         newX -= other.x();
         newY -= other.y();
         newZ -= other.z();
      }

      return at(newX, newY, newZ);
   }

   public BlockVector3 multiply(BlockVector3 other) {
      return this.multiply(other.x(), other.y(), other.z());
   }

   public BlockVector3 multiply(int x, int y, int z) {
      return at(this.x() * x, this.y() * y, this.z() * z);
   }

   public BlockVector3 multiply(BlockVector3... others) {
      int newX = this.x();
      int newY = this.y();
      int newZ = this.z();

      for (BlockVector3 other : others) {
         newX *= other.x();
         newY *= other.y();
         newZ *= other.z();
      }

      return at(newX, newY, newZ);
   }

   public BlockVector3 multiply(int n) {
      return this.multiply(n, n, n);
   }

   public BlockVector3 divide(BlockVector3 other) {
      return this.divide(other.x(), other.y(), other.z());
   }

   public BlockVector3 divide(int x, int y, int z) {
      return at(this.x() / x, this.y() / y, this.z() / z);
   }

   public BlockVector3 divide(int n) {
      return this.divide(n, n, n);
   }

   public BlockVector3 shr(int x, int y, int z) {
      return at(this.x() >> x, this.y() >> y, this.z() >> z);
   }

   public BlockVector3 shr(int n) {
      return this.shr(n, n, n);
   }

   public BlockVector3 shl(int x, int y, int z) {
      return at(this.x() << x, this.y() << y, this.z() << z);
   }

   public BlockVector3 shl(int n) {
      return this.shl(n, n, n);
   }

   public double length() {
      return Math.sqrt(this.lengthSq());
   }

   public int lengthSq() {
      return this.x() * this.x() + this.y() * this.y() + this.z() * this.z();
   }

   public double distance(BlockVector3 other) {
      return Math.sqrt(this.distanceSq(other));
   }

   public int distanceSq(BlockVector3 other) {
      int dx = other.x() - this.x();
      int dy = other.y() - this.y();
      int dz = other.z() - this.z();
      return dx * dx + dy * dy + dz * dz;
   }

   public BlockVector3 normalize() {
      double len = this.length();
      double x = this.x() / len;
      double y = this.y() / len;
      double z = this.z() / len;
      return at(x, y, z);
   }

   public double dot(BlockVector3 other) {
      return this.x() * other.x() + this.y() * other.y() + this.z() * other.z();
   }

   public BlockVector3 cross(BlockVector3 other) {
      return new BlockVector3Imp(
         this.y() * other.z() - this.z() * other.y(), this.z() * other.x() - this.x() * other.z(), this.x() * other.y() - this.y() * other.x()
      );
   }

   public boolean containedWithin(BlockVector3 min, BlockVector3 max) {
      return this.x() >= min.x() && this.x() <= max.x() && this.y() >= min.y() && this.y() <= max.y() && this.z() >= min.z() && this.z() <= max.z();
   }

   public BlockVector3 clampY(int min, int max) {
      Preconditions.checkArgument(min <= max, "minimum cannot be greater than maximum");
      if (this.y() < min) {
         return at(this.x(), min, this.z());
      } else {
         return this.y() > max ? at(this.x(), max, this.z()) : this;
      }
   }

   public BlockVector3 floor() {
      return this;
   }

   public BlockVector3 ceil() {
      return this;
   }

   public BlockVector3 round() {
      return this;
   }

   public BlockVector3 abs() {
      return at(Math.abs(this.x()), Math.abs(this.y()), Math.abs(this.z()));
   }

   public BlockVector3 transform2D(double angle, double aboutX, double aboutZ, double translateX, double translateZ) {
      angle = Math.toRadians(angle);
      double x = this.x() - aboutX;
      double z = this.z() - aboutZ;
      double cos = Math.cos(angle);
      double sin = Math.sin(angle);
      double x2 = x * cos - z * sin;
      double z2 = x * sin + z * cos;
      return at(x2 + aboutX + translateX, (double)this.y(), z2 + aboutZ + translateZ);
   }

   public double toPitch() {
      double x = this.x();
      double z = this.z();
      if (x == 0.0 && z == 0.0) {
         return this.y() > 0 ? -90.0 : 90.0;
      } else {
         double x2 = x * x;
         double z2 = z * z;
         double xz = Math.sqrt(x2 + z2);
         return Math.toDegrees(Math.atan(-this.y() / xz));
      }
   }

   public double toYaw() {
      double x = this.x();
      double z = this.z();
      double t = Math.atan2(-x, z);
      double tau = Math.PI * 2;
      return Math.toDegrees((t + tau) % tau);
   }

   public BlockVector3 getMinimum(BlockVector3 v2) {
      return new BlockVector3Imp(Math.min(this.x(), v2.x()), Math.min(this.y(), v2.y()), Math.min(this.z(), v2.z()));
   }

   public BlockVector3 getMaximum(BlockVector3 v2) {
      return new BlockVector3Imp(Math.max(this.x(), v2.x()), Math.max(this.y(), v2.y()), Math.max(this.z(), v2.z()));
   }

   public boolean setOrdinal(Extent orDefault, int ordinal) {
      return orDefault.setBlock(this, BlockState.getFromOrdinal(ordinal));
   }

   public boolean setBlock(Extent orDefault, BlockState state) {
      return orDefault.setBlock(this, state);
   }

   public boolean setFullBlock(Extent orDefault, BaseBlock block) {
      return orDefault.setBlock(this, block);
   }

   public int getOrdinal(Extent orDefault) {
      return this.getBlock(orDefault).getOrdinal();
   }

   public char getOrdinalChar(Extent orDefault) {
      return (char)this.getOrdinal(orDefault);
   }

   public BlockState getBlock(Extent orDefault) {
      return orDefault.getBlock(this);
   }

   public BaseBlock getFullBlock(Extent orDefault) {
      return orDefault.getFullBlock(this);
   }

   public boolean setBiome(Extent orDefault, BiomeType type) {
      return orDefault.setBiome(this, type);
   }

   public BiomeType getBiome(Extent orDefault) {
      return orDefault.getBiome(this);
   }

   @Deprecated(
      forRemoval = true,
      since = "2.11.2"
   )
   public CompoundTag getNbtData(Extent orDefault) {
      return orDefault.getFullBlock(this.x(), this.y(), this.z()).getNbtData();
   }

   public BlockState getOrdinalBelow(Extent orDefault) {
      return orDefault.getBlock(this.x(), this.y() - 1, this.z());
   }

   public BlockState getStateAbove(Extent orDefault) {
      return orDefault.getBlock(this.x(), this.y() + 1, this.z());
   }

   public BlockState getStateRelativeY(Extent orDefault, final int y) {
      return orDefault.getBlock(this.x(), this.y() + y, this.z());
   }

   public BlockVector2 toBlockVector2() {
      return BlockVector2.at(this.x(), this.z());
   }

   public Vector3 toVector3() {
      return Vector3.at(this.x(), this.y(), this.z());
   }

   @Override
   public boolean equals(Object obj) {
      return !(obj instanceof BlockVector3 other) ? false : other.x() == this.x() && other.y() == this.y() && other.z() == this.z();
   }

   public final boolean equals(BlockVector3 other) {
      return other == null ? false : other.x() == this.x() && other.y() == this.y() && other.z() == this.z();
   }

   @Override
   public int hashCode() {
      return this.x() ^ this.z() << 12 ^ this.y() << 24;
   }

   @Override
   public String toString() {
      return "(" + this.x() + ", " + this.y() + ", " + this.z() + ")";
   }

   public String toParserString() {
      return this.x() + "," + this.y() + "," + this.z();
   }

   public BlockVector3 plus(BlockVector3 other) {
      return this.add(other);
   }

   private static final class YzxOrderComparator {
      private static final Comparator<BlockVector3> YZX_ORDER = Comparator.comparingInt(BlockVector3::y)
         .thenComparingInt(BlockVector3::z)
         .thenComparingInt(BlockVector3::x);
   }
}
