package com.magmaguy.elitemobs.magmacore.scripting.zones;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

public class Ray extends Shape {
   protected final double maxDistance = 500.0;
   protected final boolean ignoresSolidBlocks;
   protected double thickness;
   protected Location centerLocation;
   protected Location initialTargetLocation;
   protected List<Location> locations = new ArrayList<>();
   protected Location currentSource;
   protected Location currentTarget;

   public Ray(boolean ignoresSolidBlocks, double pointRadius, Location centerLocation, Location initialTargetLocation) {
      this.ignoresSolidBlocks = ignoresSolidBlocks;
      this.thickness = pointRadius;
      this.centerLocation = centerLocation;
      this.initialTargetLocation = initialTargetLocation;
      this.currentSource = centerLocation;
      this.currentTarget = initialTargetLocation;
   }

   protected List<Location> drawLine(Location location1, Location location2) {
      this.currentSource = location1;
      this.currentTarget = location2;
      List<Location> locations = new ArrayList<>();
      Vector raySegment = location2.clone().subtract(location1).toVector().normalize().multiply(this.thickness);
      Location currentLocation = location1.clone();
      locations.add(location1);

      for (int i = 0; i < 500.0 && !(currentLocation.distanceSquared(location2) < Math.pow(this.thickness, 2.0)); i++) {
         currentLocation.add(raySegment);
         if (!this.ignoresSolidBlocks && currentLocation.getBlock().getType().isSolid()) {
            break;
         }

         locations.add(currentLocation.clone());
      }

      return locations;
   }

   @Override
   public boolean contains(Location position) {
      for (Location location : this.locations) {
         if (location.distanceSquared(position) < Math.pow(this.thickness, 2.0)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean contains(LivingEntity livingEntity) {
      for (Location location : this.locations) {
         double closestX = this.clamp(location.getX(), livingEntity.getBoundingBox().getMinX(), livingEntity.getBoundingBox().getMaxX());
         double closestY = this.clamp(location.getY(), livingEntity.getBoundingBox().getMinY(), livingEntity.getBoundingBox().getMaxY());
         double closestZ = this.clamp(location.getZ(), livingEntity.getBoundingBox().getMinZ(), livingEntity.getBoundingBox().getMaxZ());
         double distanceSquared = location.distanceSquared(new Location(location.getWorld(), closestX, closestY, closestZ));
         if (distanceSquared <= this.thickness * this.thickness) {
            return true;
         }
      }

      return false;
   }

   private double clamp(double value, double min, double max) {
      if (value < min) {
         return min;
      } else {
         return value > max ? max : value;
      }
   }

   @Override
   public boolean borderContains(Location position) {
      return false;
   }

   @Override
   public boolean borderContains(LivingEntity livingEntity) {
      return false;
   }

   @Override
   public void visualize(Particle particle) {
   }

   @Override
   public Location getCenter() {
      return this.centerLocation;
   }

   @Override
   public BoundingBox getEntityQueryBounds() {
      if (!this.locations.isEmpty() && Double.isFinite(this.thickness)) {
         double minX = Double.POSITIVE_INFINITY;
         double minY = Double.POSITIVE_INFINITY;
         double minZ = Double.POSITIVE_INFINITY;
         double maxX = Double.NEGATIVE_INFINITY;
         double maxY = Double.NEGATIVE_INFINITY;
         double maxZ = Double.NEGATIVE_INFINITY;

         for (Location location : this.locations) {
            minX = Math.min(minX, location.getX());
            minY = Math.min(minY, location.getY());
            minZ = Math.min(minZ, location.getZ());
            maxX = Math.max(maxX, location.getX());
            maxY = Math.max(maxY, location.getY());
            maxZ = Math.max(maxZ, location.getZ());
         }

         double extent = Math.abs(this.thickness);
         minX -= extent;
         minY -= extent;
         minZ -= extent;
         maxX += extent;
         maxY += extent;
         maxZ += extent;
         return Double.isFinite(minX)
               && Double.isFinite(minY)
               && Double.isFinite(minZ)
               && Double.isFinite(maxX)
               && Double.isFinite(maxY)
               && Double.isFinite(maxZ)
            ? new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ)
            : null;
      } else {
         return null;
      }
   }

   @Override
   public List<Location> getEdgeLocations() {
      return null;
   }

   @Override
   public List<Location> getLocations() {
      return this.locations;
   }
}
