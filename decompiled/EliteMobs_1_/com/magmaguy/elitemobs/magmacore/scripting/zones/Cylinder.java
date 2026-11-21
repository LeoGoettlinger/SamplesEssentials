package com.magmaguy.elitemobs.magmacore.scripting.zones;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

public class Cylinder extends Shape {
   private final Vector center;
   private final double radius;
   private final double height;
   private final List<Vector> edgeVectors = null;
   private double borderRadius = 1.0;
   private Location centerLocation = null;
   private List<Vector> locationVectors = null;

   public Cylinder(Location centerLocation, double radius, double height, double borderRadius) {
      this.centerLocation = centerLocation.clone();
      this.center = centerLocation.toVector();
      this.radius = radius;
      this.height = height;
      this.borderRadius = borderRadius;
   }

   public Cylinder(Vector center, double radius, double height) {
      this.center = center;
      this.radius = radius;
      this.height = height;
   }

   public boolean contains(Vector position) {
      double dX = position.getX() - this.center.getX();
      double dZ = position.getZ() - this.center.getZ();
      return dX * dX + dZ * dZ < this.radius * this.radius && position.getY() < this.center.getY() + this.height && position.getY() >= this.center.getY();
   }

   @Override
   public boolean borderContains(Location position) {
      Cylinder innerCylinder = new Cylinder(this.centerLocation, this.borderRadius, this.height, this.borderRadius);
      return this.contains(position) && !innerCylinder.contains(position);
   }

   @Override
   public boolean borderContains(LivingEntity livingEntity) {
      Cylinder innerCylinder = new Cylinder(this.centerLocation, this.borderRadius, this.height, this.borderRadius);
      return this.contains(livingEntity) && !innerCylinder.contains(livingEntity);
   }

   @Override
   public boolean contains(Location position) {
      return this.contains(position.toVector());
   }

   @Override
   public boolean contains(LivingEntity livingEntity) {
      BoundingBox boundingBox = livingEntity.getBoundingBox();
      if (!(this.centerLocation.getY() > boundingBox.getMinY()) && !(this.centerLocation.getY() + this.height < boundingBox.getMaxY())) {
         double circleDistanceX = Math.abs(this.centerLocation.getX() - boundingBox.getCenterX());
         double circleDistanceZ = Math.abs(this.centerLocation.getZ() - boundingBox.getCenterZ());
         if (circleDistanceX > boundingBox.getWidthX() / 2.0 + this.radius) {
            return false;
         } else if (circleDistanceZ > boundingBox.getWidthZ() / 2.0 + this.radius) {
            return false;
         } else if (circleDistanceX <= boundingBox.getWidthX() / 2.0) {
            return true;
         } else if (circleDistanceZ <= boundingBox.getWidthZ() / 2.0) {
            return true;
         } else {
            double cornerDistanceSquared = Math.pow((circleDistanceX - boundingBox.getWidthX()) / 2.0, 2.0)
               + Math.pow((circleDistanceZ - boundingBox.getWidthZ()) / 2.0, 2.0);
            return cornerDistanceSquared <= this.radius * this.radius;
         }
      } else {
         return false;
      }
   }

   @Override
   public void visualize(Particle particle) {
      this.getLocations().forEach(newLocation -> newLocation.getWorld().spawnParticle(particle, newLocation, 1, 0.0, 0.0, 0.0, 0.0));
   }

   @Override
   public Location getCenter() {
      return this.centerLocation;
   }

   @Override
   public BoundingBox getEntityQueryBounds() {
      if (this.centerLocation == null) {
         return null;
      } else {
         double extent = Math.abs(this.radius);
         double minX = this.centerLocation.getX() - extent;
         double minY = this.centerLocation.getY();
         double minZ = this.centerLocation.getZ() - extent;
         double maxX = this.centerLocation.getX() + extent;
         double maxY = this.centerLocation.getY() + this.height;
         double maxZ = this.centerLocation.getZ() + extent;
         return Double.isFinite(minX)
               && Double.isFinite(minY)
               && Double.isFinite(minZ)
               && Double.isFinite(maxX)
               && Double.isFinite(maxY)
               && Double.isFinite(maxZ)
            ? new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ)
            : null;
      }
   }

   @Override
   public List<Location> getEdgeLocations() {
      if (this.edgeVectors != null) {
         return this.convert(this.edgeVectors);
      } else {
         List<Location> edgeLocations = new ArrayList<>();
         this.getLocations().forEach(iteratedLocation -> {
            if (this.borderContains(iteratedLocation)) {
               edgeLocations.add(iteratedLocation);
            }
         });
         return edgeLocations;
      }
   }

   private List<Vector> getLocationVectors() {
      if (this.locationVectors != null) {
         return this.locationVectors;
      } else {
         this.locationVectors = new ArrayList<>();

         for (int x = (int)(-this.radius); x < (int)this.radius; x++) {
            for (int z = (int)(-this.radius); z < (int)this.radius; z++) {
               for (int y = 0; y < this.height; y++) {
                  Vector newVector = new Vector(x, y, z);
                  Location newLocation = this.centerLocation.clone().add(newVector);
                  if (this.contains(newLocation)) {
                     this.locationVectors.add(newVector);
                  }
               }
            }
         }

         return this.locationVectors;
      }
   }

   @Override
   public List<Location> getLocations() {
      return this.locationVectors != null ? this.convert(this.locationVectors) : this.convert(this.getLocationVectors());
   }

   private List<Location> convert(List<Vector> vectors) {
      return vectors.stream().map(edge -> this.centerLocation.clone().add(edge)).collect(Collectors.toList());
   }
}
