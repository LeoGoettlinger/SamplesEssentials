package com.sk89q.worldedit.util;

import com.fastasyncworldedit.core.math.Vector3Impl;
import com.google.common.base.Preconditions;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.Vector3;

public class Location extends Vector3Impl {
   private final Extent extent;
   private final float pitch;
   private final float yaw;

   public Location(Extent extent) {
      this(extent, Vector3.ZERO, 0.0F, 90.0F);
   }

   public Location(Extent extent, double x, double y, double z) {
      this(extent, Vector3.at(x, y, z), 0.0F, 90.0F);
   }

   public Location(Extent extent, Vector3 position) {
      this(extent, position, 0.0F, 90.0F);
   }

   public Location(Extent extent, double x, double y, double z, Vector3 direction) {
      this(extent, Vector3.at(x, y, z), direction);
   }

   public Location(Extent extent, double x, double y, double z, float yaw, float pitch) {
      this(extent, Vector3.at(x, y, z), yaw, pitch);
   }

   public Location(Extent extent, Vector3 position, Vector3 direction) {
      this(extent, position, (float)direction.toYaw(), (float)direction.toPitch());
   }

   public Location(Extent extent, Vector3 position, float yaw, float pitch) {
      super(position);
      Preconditions.checkNotNull(extent);
      Preconditions.checkNotNull(position);
      this.extent = extent;
      this.pitch = pitch;
      this.yaw = yaw;
   }

   public Extent getExtent() {
      return this.extent;
   }

   public Location setExtent(Extent extent) {
      return new Location(extent, this, this.getDirection());
   }

   public float getYaw() {
      return this.yaw;
   }

   public Location setYaw(float yaw) {
      return new Location(this.extent, this, yaw, this.pitch);
   }

   public float getPitch() {
      return this.pitch;
   }

   public Location setPitch(float pitch) {
      return new Location(this.extent, this, this.yaw, pitch);
   }

   public Location setDirection(float yaw, float pitch) {
      return new Location(this.extent, this, yaw, pitch);
   }

   public Vector3 getDirection() {
      if (Float.isNaN(this.getYaw()) && Float.isNaN(this.getPitch())) {
         return Vector3.ZERO;
      } else {
         double yaw = Math.toRadians(this.getYaw());
         double pitch = Math.toRadians(this.getPitch());
         double xz = Math.cos(pitch);
         return Vector3.at(-xz * Math.sin(yaw), -Math.sin(pitch), xz * Math.cos(yaw));
      }
   }

   public Direction getDirectionEnum() {
      return Direction.findClosest(this.getDirection(), Direction.Flag.ALL);
   }

   public Location setDirection(Vector3 direction) {
      return new Location(this.extent, this, (float)direction.toYaw(), (float)direction.toPitch());
   }

   public Vector3 toVector() {
      return this;
   }

   public Location setX(double x) {
      return new Location(this.extent, this.withX(x), this.yaw, this.pitch);
   }

   public Location setY(double y) {
      return new Location(this.extent, this.withY(y), this.yaw, this.pitch);
   }

   public Location setZ(double z) {
      return new Location(this.extent, this.withZ(z), this.yaw, this.pitch);
   }

   public Location setPosition(Vector3 position) {
      return new Location(this.extent, position, this.yaw, this.pitch);
   }

   public Location clampY(int min, int max) {
      Preconditions.checkArgument(min <= max, "minimum cannot be greater than maximum");
      if (this.y() < min) {
         return new Location(this.extent, this.x(), min, this.z());
      } else {
         return this.y() > max ? new Location(this.extent, this.x(), max, this.z()) : this;
      }
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         Location location = (Location)o;
         if (Double.doubleToLongBits(this.pitch) != Double.doubleToLongBits(location.pitch)) {
            return false;
         } else if (Double.doubleToLongBits(this.yaw) != Double.doubleToLongBits(location.yaw)) {
            return false;
         } else if (this.x() != location.x()) {
            return false;
         } else if (this.z() != location.z()) {
            return false;
         } else {
            return this.y() != location.y() ? false : this.extent.equals(location.extent);
         }
      } else {
         return false;
      }
   }
}
