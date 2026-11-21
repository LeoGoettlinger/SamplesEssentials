package com.sk89q.worldedit.math;

import com.fastasyncworldedit.core.math.MutableVector3;
import com.fastasyncworldedit.core.math.Vector3Impl;
import com.fastasyncworldedit.core.util.MathMan;
import com.google.common.base.Preconditions;
import com.google.common.collect.ComparisonChain;
import java.util.Comparator;

public abstract class Vector3 {
   public static final Vector3 ZERO = at(0.0, 0.0, 0.0);
   public static final Vector3 UNIT_X = at(1.0, 0.0, 0.0);
   public static final Vector3 UNIT_Y = at(0.0, 1.0, 0.0);
   public static final Vector3 UNIT_Z = at(0.0, 0.0, 1.0);
   public static final Vector3 ONE = at(1.0, 1.0, 1.0);

   public static Vector3 at(double x, double y, double z) {
      return new Vector3Impl(x, y, z);
   }

   public static Comparator<Vector3> sortByCoordsYzx() {
      return Vector3.YzxOrderComparator.YZX_ORDER;
   }

   public int getBlockX() {
      return MathMan.roundInt(this.x());
   }

   public int getBlockY() {
      return MathMan.roundInt(this.y());
   }

   public int getBlockZ() {
      return MathMan.roundInt(this.z());
   }

   public MutableVector3 setComponents(Vector3 other) {
      return new MutableVector3(other);
   }

   public MutableVector3 setComponents(int x, int y, int z) {
      return new MutableVector3((float)x, (float)y, (float)z);
   }

   public MutableVector3 setComponents(double x, double y, double z) {
      return new MutableVector3(x, y, z);
   }

   public MutableVector3 mutX(int x) {
      return new MutableVector3((double)x, this.y(), this.z());
   }

   public MutableVector3 mutX(double x) {
      return new MutableVector3(x, this.y(), this.z());
   }

   public MutableVector3 mutY(int y) {
      return new MutableVector3(this.x(), (double)y, this.z());
   }

   public MutableVector3 mutY(double y) {
      return new MutableVector3(this.x(), y, this.z());
   }

   public MutableVector3 mutZ(int z) {
      return new MutableVector3(this.x(), this.y(), (double)z);
   }

   public MutableVector3 mutZ(double z) {
      return new MutableVector3(this.x(), this.y(), z);
   }

   public abstract double x();

   public int blockX() {
      return MathMan.roundInt(this.x());
   }

   @Deprecated(
      forRemoval = true,
      since = "2.11.0"
   )
   public double getX() {
      return this.x();
   }

   public Vector3 withX(double x) {
      return at(x, this.y(), this.z());
   }

   public abstract double y();

   public int blockY() {
      return MathMan.roundInt(this.y());
   }

   @Deprecated(
      forRemoval = true,
      since = "2.11.0"
   )
   public double getY() {
      return this.y();
   }

   public Vector3 withY(double y) {
      return at(this.x(), y, this.z());
   }

   public abstract double z();

   public int blockZ() {
      return MathMan.roundInt(this.z());
   }

   @Deprecated(
      forRemoval = true,
      since = "2.11.0"
   )
   public double getZ() {
      return this.z();
   }

   public Vector3 withZ(double z) {
      return at(this.x(), this.y(), z);
   }

   public Vector3 add(Vector3 other) {
      return this.add(other.x(), other.y(), other.z());
   }

   public Vector3 add(double x, double y, double z) {
      return at(this.x() + x, this.y() + y, this.z() + z);
   }

   public Vector3 add(Vector3... others) {
      double newX = this.x();
      double newY = this.y();
      double newZ = this.z();

      for (Vector3 other : others) {
         newX += other.x();
         newY += other.y();
         newZ += other.z();
      }

      return at(newX, newY, newZ);
   }

   public Vector3 subtract(Vector3 other) {
      return this.subtract(other.x(), other.y(), other.z());
   }

   public Vector3 subtract(double x, double y, double z) {
      return at(this.x() - x, this.y() - y, this.z() - z);
   }

   public Vector3 subtract(Vector3... others) {
      double newX = this.x();
      double newY = this.y();
      double newZ = this.z();

      for (Vector3 other : others) {
         newX -= other.x();
         newY -= other.y();
         newZ -= other.z();
      }

      return at(newX, newY, newZ);
   }

   public Vector3 multiply(Vector3 other) {
      return this.multiply(other.x(), other.y(), other.z());
   }

   public Vector3 multiply(double x, double y, double z) {
      return at(this.x() * x, this.y() * y, this.z() * z);
   }

   public Vector3 multiply(Vector3... others) {
      double newX = this.x();
      double newY = this.y();
      double newZ = this.z();

      for (Vector3 other : others) {
         newX *= other.x();
         newY *= other.y();
         newZ *= other.z();
      }

      return at(newX, newY, newZ);
   }

   public Vector3 multiply(double n) {
      return this.multiply(n, n, n);
   }

   public Vector3 divide(Vector3 other) {
      return this.divide(other.x(), other.y(), other.z());
   }

   public Vector3 divide(double x, double y, double z) {
      return at(this.x() / x, this.y() / y, this.z() / z);
   }

   public Vector3 divide(double n) {
      return this.divide(n, n, n);
   }

   public double length() {
      return Math.sqrt(this.lengthSq());
   }

   public double lengthSq() {
      return this.x() * this.x() + this.y() * this.y() + this.z() * this.z();
   }

   public double distance(Vector3 other) {
      return Math.sqrt(this.distanceSq(other));
   }

   public double distanceSq(Vector3 other) {
      double dx = other.x() - this.x();
      double dy = other.y() - this.y();
      double dz = other.z() - this.z();
      return dx * dx + dy * dy + dz * dz;
   }

   public Vector3 normalize() {
      return this.divide(this.length());
   }

   public double dot(Vector3 other) {
      return this.x() * other.x() + this.y() * other.y() + this.z() * other.z();
   }

   public Vector3 cross(Vector3 other) {
      return at(this.y() * other.z() - this.z() * other.y(), this.z() * other.x() - this.x() * other.z(), this.x() * other.y() - this.y() * other.x());
   }

   public boolean containedWithin(Vector3 min, Vector3 max) {
      return this.x() >= min.x() && this.x() <= max.x() && this.y() >= min.y() && this.y() <= max.y() && this.z() >= min.z() && this.z() <= max.z();
   }

   public Vector3 clampY(int min, int max) {
      Preconditions.checkArgument(min <= max, "minimum cannot be greater than maximum");
      if (this.y() < min) {
         return at(this.x(), min, this.z());
      } else {
         return this.y() > max ? at(this.x(), max, this.z()) : this;
      }
   }

   public Vector3 floor() {
      return at(Math.floor(this.x()), Math.floor(this.y()), Math.floor(this.z()));
   }

   public Vector3 ceil() {
      return at(Math.ceil(this.x()), Math.ceil(this.y()), Math.ceil(this.z()));
   }

   public Vector3 round() {
      return at(Math.floor(this.x() + 0.5), Math.floor(this.y() + 0.5), Math.floor(this.z() + 0.5));
   }

   public Vector3 roundHalfUp() {
      return at(MathUtils.roundHalfUp(this.x()), MathUtils.roundHalfUp(this.y()), MathUtils.roundHalfUp(this.z()));
   }

   public Vector3 abs() {
      return at(Math.abs(this.x()), Math.abs(this.y()), Math.abs(this.z()));
   }

   public Vector3 transform2D(double angle, double aboutX, double aboutZ, double translateX, double translateZ) {
      angle = Math.toRadians(angle);
      double x = this.x() - aboutX;
      double z = this.z() - aboutZ;
      double cos = Math.cos(angle);
      double sin = Math.sin(angle);
      double x2 = x * cos - z * sin;
      double z2 = x * sin + z * cos;
      return at(x2 + aboutX + translateX, this.y(), z2 + aboutZ + translateZ);
   }

   public double toPitch() {
      double x = this.x();
      double z = this.z();
      if (x == 0.0 && z == 0.0) {
         return this.y() > 0.0 ? -90.0 : 90.0;
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

   public Vector3 getMinimum(Vector3 v2) {
      return at(Math.min(this.x(), v2.x()), Math.min(this.y(), v2.y()), Math.min(this.z(), v2.z()));
   }

   public Vector3 getMaximum(Vector3 v2) {
      return at(Math.max(this.x(), v2.x()), Math.max(this.y(), v2.y()), Math.max(this.z(), v2.z()));
   }

   public static BlockVector3 toBlockPoint(double x, double y, double z) {
      return BlockVector3.at(x, y, z);
   }

   public BlockVector3 toBlockPoint() {
      return toBlockPoint(this.x(), this.y(), this.z());
   }

   public Vector2 toVector2() {
      return Vector2.at(this.x(), this.z());
   }

   @Override
   public boolean equals(Object obj) {
      return !(obj instanceof Vector3 other) ? false : other.x() == this.x() && other.y() == this.y() && other.z() == this.z();
   }

   public boolean equalsFuzzy(Vector3 other) {
      if (this.equals(other)) {
         return true;
      } else if (Math.abs(this.x() - other.x()) > 1.0E-6) {
         return false;
      } else {
         return Math.abs(this.y() - other.y()) > 1.0E-6 ? false : !(Math.abs(this.z() - other.z()) > 1.0E-6);
      }
   }

   @Override
   public int hashCode() {
      return (int)this.x() ^ (int)this.z() << 12 ^ (int)this.y() << 24;
   }

   @Override
   public String toString() {
      String x = this.x() == this.blockX() ? this.blockX() + "" : this.x() + "";
      String y = this.y() == this.blockY() ? this.blockY() + "" : this.y() + "";
      String z = this.z() == this.blockZ() ? this.blockZ() + "" : this.z() + "";
      return "(" + x + ", " + y + ", " + z + ")";
   }

   public String toParserString() {
      return this.x() + "," + this.y() + "," + this.z();
   }

   private static final class YzxOrderComparator {
      private static final Comparator<Vector3> YZX_ORDER = (a, b) -> ComparisonChain.start()
         .compare(a.y(), b.y())
         .compare(a.z(), b.z())
         .compare(a.x(), b.x())
         .result();
   }
}
