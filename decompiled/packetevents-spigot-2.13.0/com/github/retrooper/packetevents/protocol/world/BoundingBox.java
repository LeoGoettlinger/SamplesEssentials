package com.github.retrooper.packetevents.protocol.world;

import com.github.retrooper.packetevents.util.Vector3d;
import java.util.LinkedHashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class BoundingBox {
   private double minX;
   private double minY;
   private double minZ;
   private double maxX;
   private double maxY;
   private double maxZ;

   public BoundingBox() {
      this.resize(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
   }

   public BoundingBox(double x1, double y1, double z1, double x2, double y2, double z2) {
      this.resize(x1, y1, z1, x2, y2, z2);
   }

   @NotNull
   public static BoundingBox of(@NotNull Vector3d corner1, @NotNull Vector3d corner2) {
      return new BoundingBox(corner1.getX(), corner1.getY(), corner1.getZ(), corner2.getX(), corner2.getY(), corner2.getZ());
   }

   @NotNull
   public static BoundingBox of(@NotNull Vector3d center, double x, double y, double z) {
      return new BoundingBox(center.getX() - x, center.getY() - y, center.getZ() - z, center.getX() + x, center.getY() + y, center.getZ() + z);
   }

   @NotNull
   public static BoundingBox deserialize(@NotNull Map<String, Object> args) {
      double minX = 0.0;
      double minY = 0.0;
      double minZ = 0.0;
      double maxX = 0.0;
      double maxY = 0.0;
      double maxZ = 0.0;
      if (args.containsKey("minX")) {
         minX = ((Number)args.get("minX")).doubleValue();
      }

      if (args.containsKey("minY")) {
         minY = ((Number)args.get("minY")).doubleValue();
      }

      if (args.containsKey("minZ")) {
         minZ = ((Number)args.get("minZ")).doubleValue();
      }

      if (args.containsKey("maxX")) {
         maxX = ((Number)args.get("maxX")).doubleValue();
      }

      if (args.containsKey("maxY")) {
         maxY = ((Number)args.get("maxY")).doubleValue();
      }

      if (args.containsKey("maxZ")) {
         maxZ = ((Number)args.get("maxZ")).doubleValue();
      }

      return new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
   }

   @NotNull
   public BoundingBox resize(double x1, double y1, double z1, double x2, double y2, double z2) {
      this.minX = Math.min(x1, x2);
      this.minY = Math.min(y1, y2);
      this.minZ = Math.min(z1, z2);
      this.maxX = Math.max(x1, x2);
      this.maxY = Math.max(y1, y2);
      this.maxZ = Math.max(z1, z2);
      return this;
   }

   public double getMinX() {
      return this.minX;
   }

   public double getMinY() {
      return this.minY;
   }

   public double getMinZ() {
      return this.minZ;
   }

   @NotNull
   public Vector3d getMin() {
      return new Vector3d(this.minX, this.minY, this.minZ);
   }

   public double getMaxX() {
      return this.maxX;
   }

   public double getMaxY() {
      return this.maxY;
   }

   public double getMaxZ() {
      return this.maxZ;
   }

   @NotNull
   public Vector3d getMax() {
      return new Vector3d(this.maxX, this.maxY, this.maxZ);
   }

   public double getWidthX() {
      return this.maxX - this.minX;
   }

   public double getWidthZ() {
      return this.maxZ - this.minZ;
   }

   public double getHeight() {
      return this.maxY - this.minY;
   }

   public double getVolume() {
      return this.getHeight() * this.getWidthX() * this.getWidthZ();
   }

   public double getCenterX() {
      return this.minX + this.getWidthX() * 0.5;
   }

   public double getCenterY() {
      return this.minY + this.getHeight() * 0.5;
   }

   public double getCenterZ() {
      return this.minZ + this.getWidthZ() * 0.5;
   }

   @NotNull
   public Vector3d getCenter() {
      return new Vector3d(this.getCenterX(), this.getCenterY(), this.getCenterZ());
   }

   @NotNull
   public BoundingBox copy(@NotNull BoundingBox other) {
      return this.resize(other.getMinX(), other.getMinY(), other.getMinZ(), other.getMaxX(), other.getMaxY(), other.getMaxZ());
   }

   @NotNull
   public BoundingBox expand(double negativeX, double negativeY, double negativeZ, double positiveX, double positiveY, double positiveZ) {
      if (negativeX == 0.0 && negativeY == 0.0 && negativeZ == 0.0 && positiveX == 0.0 && positiveY == 0.0 && positiveZ == 0.0) {
         return this;
      } else {
         double newMinX = this.minX - negativeX;
         double newMinY = this.minY - negativeY;
         double newMinZ = this.minZ - negativeZ;
         double newMaxX = this.maxX + positiveX;
         double newMaxY = this.maxY + positiveY;
         double newMaxZ = this.maxZ + positiveZ;
         if (newMinX > newMaxX) {
            double centerZ = this.getCenterX();
            if (newMaxX >= centerZ) {
               newMinX = newMaxX;
            } else if (newMinX <= centerZ) {
               newMaxX = newMinX;
            } else {
               newMinX = centerZ;
               newMaxX = centerZ;
            }
         }

         if (newMinY > newMaxY) {
            double centerZ = this.getCenterY();
            if (newMaxY >= centerZ) {
               newMinY = newMaxY;
            } else if (newMinY <= centerZ) {
               newMaxY = newMinY;
            } else {
               newMinY = centerZ;
               newMaxY = centerZ;
            }
         }

         if (newMinZ > newMaxZ) {
            double centerZ = this.getCenterZ();
            if (newMaxZ >= centerZ) {
               newMinZ = newMaxZ;
            } else if (newMinZ <= centerZ) {
               newMaxZ = newMinZ;
            } else {
               newMinZ = centerZ;
               newMaxZ = centerZ;
            }
         }

         return this.resize(newMinX, newMinY, newMinZ, newMaxX, newMaxY, newMaxZ);
      }
   }

   @NotNull
   public BoundingBox expand(double x, double y, double z) {
      return this.expand(x, y, z, x, y, z);
   }

   @NotNull
   public BoundingBox expand(@NotNull Vector3d expansion) {
      double x = expansion.getX();
      double y = expansion.getY();
      double z = expansion.getZ();
      return this.expand(x, y, z, x, y, z);
   }

   @NotNull
   public BoundingBox expand(double expansion) {
      return this.expand(expansion, expansion, expansion, expansion, expansion, expansion);
   }

   @NotNull
   public BoundingBox expand(double dirX, double dirY, double dirZ, double expansion) {
      if (expansion == 0.0) {
         return this;
      } else if (dirX == 0.0 && dirY == 0.0 && dirZ == 0.0) {
         return this;
      } else {
         double negativeX = dirX < 0.0 ? -dirX * expansion : 0.0;
         double negativeY = dirY < 0.0 ? -dirY * expansion : 0.0;
         double negativeZ = dirZ < 0.0 ? -dirZ * expansion : 0.0;
         double positiveX = dirX > 0.0 ? dirX * expansion : 0.0;
         double positiveY = dirY > 0.0 ? dirY * expansion : 0.0;
         double positiveZ = dirZ > 0.0 ? dirZ * expansion : 0.0;
         return this.expand(negativeX, negativeY, negativeZ, positiveX, positiveY, positiveZ);
      }
   }

   @NotNull
   public BoundingBox expand(@NotNull Vector3d direction, double expansion) {
      return this.expand(direction.getX(), direction.getY(), direction.getZ(), expansion);
   }

   @NotNull
   public BoundingBox expandDirectional(double dirX, double dirY, double dirZ) {
      return this.expand(dirX, dirY, dirZ, 1.0);
   }

   @NotNull
   public BoundingBox expandDirectional(@NotNull Vector3d direction) {
      return this.expand(direction.getX(), direction.getY(), direction.getZ(), 1.0);
   }

   @NotNull
   public BoundingBox union(double posX, double posY, double posZ) {
      double newMinX = Math.min(this.minX, posX);
      double newMinY = Math.min(this.minY, posY);
      double newMinZ = Math.min(this.minZ, posZ);
      double newMaxX = Math.max(this.maxX, posX);
      double newMaxY = Math.max(this.maxY, posY);
      double newMaxZ = Math.max(this.maxZ, posZ);
      return newMinX == this.minX && newMinY == this.minY && newMinZ == this.minZ && newMaxX == this.maxX && newMaxY == this.maxY && newMaxZ == this.maxZ
         ? this
         : this.resize(newMinX, newMinY, newMinZ, newMaxX, newMaxY, newMaxZ);
   }

   @NotNull
   public BoundingBox union(@NotNull Vector3d position) {
      return this.union(position.getX(), position.getY(), position.getZ());
   }

   @NotNull
   public BoundingBox union(@NotNull BoundingBox other) {
      if (this.contains(other)) {
         return this;
      } else {
         double newMinX = Math.min(this.minX, other.minX);
         double newMinY = Math.min(this.minY, other.minY);
         double newMinZ = Math.min(this.minZ, other.minZ);
         double newMaxX = Math.max(this.maxX, other.maxX);
         double newMaxY = Math.max(this.maxY, other.maxY);
         double newMaxZ = Math.max(this.maxZ, other.maxZ);
         return this.resize(newMinX, newMinY, newMinZ, newMaxX, newMaxY, newMaxZ);
      }
   }

   @NotNull
   public BoundingBox intersection(@NotNull BoundingBox other) {
      double newMinX = Math.max(this.minX, other.minX);
      double newMinY = Math.max(this.minY, other.minY);
      double newMinZ = Math.max(this.minZ, other.minZ);
      double newMaxX = Math.min(this.maxX, other.maxX);
      double newMaxY = Math.min(this.maxY, other.maxY);
      double newMaxZ = Math.min(this.maxZ, other.maxZ);
      return this.resize(newMinX, newMinY, newMinZ, newMaxX, newMaxY, newMaxZ);
   }

   @NotNull
   public BoundingBox shift(double shiftX, double shiftY, double shiftZ) {
      return shiftX == 0.0 && shiftY == 0.0 && shiftZ == 0.0
         ? this
         : this.resize(this.minX + shiftX, this.minY + shiftY, this.minZ + shiftZ, this.maxX + shiftX, this.maxY + shiftY, this.maxZ + shiftZ);
   }

   @NotNull
   public BoundingBox shift(@NotNull Vector3d shift) {
      return this.shift(shift.getX(), shift.getY(), shift.getZ());
   }

   private boolean overlaps(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
      return this.minX < maxX && this.maxX > minX && this.minY < maxY && this.maxY > minY && this.minZ < maxZ && this.maxZ > minZ;
   }

   public boolean overlaps(@NotNull BoundingBox other) {
      return this.overlaps(other.minX, other.minY, other.minZ, other.maxX, other.maxY, other.maxZ);
   }

   public boolean overlaps(@NotNull Vector3d min, @NotNull Vector3d max) {
      double x1 = min.getX();
      double y1 = min.getY();
      double z1 = min.getZ();
      double x2 = max.getX();
      double y2 = max.getY();
      double z2 = max.getZ();
      return this.overlaps(Math.min(x1, x2), Math.min(y1, y2), Math.min(z1, z2), Math.max(x1, x2), Math.max(y1, y2), Math.max(z1, z2));
   }

   public boolean contains(double x, double y, double z) {
      return x >= this.minX && x < this.maxX && y >= this.minY && y < this.maxY && z >= this.minZ && z < this.maxZ;
   }

   public boolean contains(@NotNull Vector3d position) {
      return this.contains(position.getX(), position.getY(), position.getZ());
   }

   private boolean contains(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
      return this.minX <= minX && this.maxX >= maxX && this.minY <= minY && this.maxY >= maxY && this.minZ <= minZ && this.maxZ >= maxZ;
   }

   public boolean contains(@NotNull BoundingBox other) {
      return this.contains(other.minX, other.minY, other.minZ, other.maxX, other.maxY, other.maxZ);
   }

   public boolean contains(@NotNull Vector3d min, @NotNull Vector3d max) {
      double x1 = min.getX();
      double y1 = min.getY();
      double z1 = min.getZ();
      double x2 = max.getX();
      double y2 = max.getY();
      double z2 = max.getZ();
      return this.contains(Math.min(x1, x2), Math.min(y1, y2), Math.min(z1, z2), Math.max(x1, x2), Math.max(y1, y2), Math.max(z1, z2));
   }

   @Override
   public int hashCode() {
      long temp = Double.doubleToLongBits(this.maxX);
      int result = 31 + (int)(temp ^ temp >>> 32);
      temp = Double.doubleToLongBits(this.maxY);
      result = 31 * result + (int)(temp ^ temp >>> 32);
      temp = Double.doubleToLongBits(this.maxZ);
      result = 31 * result + (int)(temp ^ temp >>> 32);
      temp = Double.doubleToLongBits(this.minX);
      result = 31 * result + (int)(temp ^ temp >>> 32);
      temp = Double.doubleToLongBits(this.minY);
      result = 31 * result + (int)(temp ^ temp >>> 32);
      temp = Double.doubleToLongBits(this.minZ);
      return 31 * result + (int)(temp ^ temp >>> 32);
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (!(obj instanceof BoundingBox)) {
         return false;
      } else {
         BoundingBox other = (BoundingBox)obj;
         if (Double.doubleToLongBits(this.maxX) != Double.doubleToLongBits(other.maxX)) {
            return false;
         } else if (Double.doubleToLongBits(this.maxY) != Double.doubleToLongBits(other.maxY)) {
            return false;
         } else if (Double.doubleToLongBits(this.maxZ) != Double.doubleToLongBits(other.maxZ)) {
            return false;
         } else if (Double.doubleToLongBits(this.minX) != Double.doubleToLongBits(other.minX)) {
            return false;
         } else {
            return Double.doubleToLongBits(this.minY) != Double.doubleToLongBits(other.minY)
               ? false
               : Double.doubleToLongBits(this.minZ) == Double.doubleToLongBits(other.minZ);
         }
      }
   }

   @Override
   public String toString() {
      return "BoundingBox [minX="
         + this.minX
         + ", minY="
         + this.minY
         + ", minZ="
         + this.minZ
         + ", maxX="
         + this.maxX
         + ", maxY="
         + this.maxY
         + ", maxZ="
         + this.maxZ
         + "]";
   }

   @NotNull
   public BoundingBox clone() {
      try {
         return (BoundingBox)super.clone();
      } catch (CloneNotSupportedException var2) {
         throw new Error(var2);
      }
   }

   @NotNull
   public Map<String, Object> serialize() {
      Map<String, Object> result = new LinkedHashMap<>();
      result.put("minX", this.minX);
      result.put("minY", this.minY);
      result.put("minZ", this.minZ);
      result.put("maxX", this.maxX);
      result.put("maxY", this.maxY);
      result.put("maxZ", this.maxZ);
      return result;
   }
}
