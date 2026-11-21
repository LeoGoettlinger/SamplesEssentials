package com.magmaguy.elitemobs.magmacore.scripting.zones;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

public class Cone extends Shape {
   private final Location top;
   private final Location bottom;
   private final double baseRadius;
   private final double borderRadius;
   private List<Location> locationList = null;
   private List<Location> edgeLocations = null;

   public Cone(Location top, Location bottom, double baseRadius, double borderRadius) {
      this.top = top.clone();
      this.bottom = bottom.clone();
      this.baseRadius = baseRadius;
      this.borderRadius = borderRadius;
   }

   private Vector getAxis() {
      return this.bottom.toVector().subtract(this.top.toVector());
   }

   @Override
   public boolean contains(Location position) {
      if (!position.getWorld().equals(this.top.getWorld())) {
         return false;
      } else {
         Vector apex = this.top.toVector();
         Vector axis = this.getAxis();
         Vector p = position.toVector().subtract(apex);
         double axisLengthSquared = axis.lengthSquared();
         double t = p.dot(axis) / axisLengthSquared;
         if (!(t < 0.0) && !(t > 1.0)) {
            Vector projection = axis.clone().multiply(t);
            double distanceSquared = p.subtract(projection).lengthSquared();
            double allowedRadius = t * this.baseRadius;
            return distanceSquared <= allowedRadius * allowedRadius;
         } else {
            return false;
         }
      }
   }

   @Override
   public boolean contains(LivingEntity livingEntity) {
      BoundingBox bb = livingEntity.getBoundingBox();
      Location feet = livingEntity.getLocation();
      Location center = new Location(feet.getWorld(), bb.getCenterX(), bb.getCenterY(), bb.getCenterZ());
      Location top = new Location(feet.getWorld(), bb.getCenterX(), bb.getMaxY(), bb.getCenterZ());
      return this.contains(feet) || this.contains(center) || this.contains(top);
   }

   @Override
   public boolean borderContains(Location position) {
      Cone innerCone = new Cone(this.top, this.bottom, this.borderRadius, this.borderRadius);
      return this.contains(position) && !innerCone.contains(position);
   }

   @Override
   public boolean borderContains(LivingEntity livingEntity) {
      Cone innerCone = new Cone(this.top, this.bottom, this.borderRadius, this.borderRadius);
      return this.contains(livingEntity) && !innerCone.contains(livingEntity);
   }

   @Override
   public void visualize(Particle particle) {
      for (Location loc : this.getLocations()) {
         loc.getWorld().spawnParticle(particle, loc, 1, 0.0, 0.0, 0.0, 0.0);
      }
   }

   @Override
   public Location getCenter() {
      return this.top.clone().add(this.bottom.toVector().subtract(this.top.toVector()).multiply(0.5));
   }

   @Override
   public BoundingBox getEntityQueryBounds() {
      double extent = Math.abs(this.baseRadius);
      double minX = Math.min(this.top.getX(), this.bottom.getX()) - extent;
      double minY = Math.min(this.top.getY(), this.bottom.getY()) - extent;
      double minZ = Math.min(this.top.getZ(), this.bottom.getZ()) - extent;
      double maxX = Math.max(this.top.getX(), this.bottom.getX()) + extent;
      double maxY = Math.max(this.top.getY(), this.bottom.getY()) + extent;
      double maxZ = Math.max(this.top.getZ(), this.bottom.getZ()) + extent;
      return Double.isFinite(minX) && Double.isFinite(minY) && Double.isFinite(minZ) && Double.isFinite(maxX) && Double.isFinite(maxY) && Double.isFinite(maxZ)
         ? new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ)
         : null;
   }

   @Override
   public List<Location> getEdgeLocations() {
      if (this.edgeLocations != null) {
         return this.edgeLocations;
      } else {
         this.edgeLocations = new ArrayList<>();

         for (Location loc : this.getLocations()) {
            if (this.borderContains(loc)) {
               this.edgeLocations.add(loc);
            }
         }

         return this.edgeLocations;
      }
   }

   @Override
   public List<Location> getLocations() {
      if (this.locationList != null) {
         return this.locationList;
      } else {
         this.locationList = new ArrayList<>();
         double minX = Math.min(this.top.getX(), this.bottom.getX()) - this.baseRadius;
         double maxX = Math.max(this.top.getX(), this.bottom.getX()) + this.baseRadius;
         double minY = Math.min(this.top.getY(), this.bottom.getY()) - this.baseRadius;
         double maxY = Math.max(this.top.getY(), this.bottom.getY()) + this.baseRadius;
         double minZ = Math.min(this.top.getZ(), this.bottom.getZ()) - this.baseRadius;
         double maxZ = Math.max(this.top.getZ(), this.bottom.getZ()) + this.baseRadius;

         for (double x = Math.floor(minX); x <= Math.ceil(maxX); x++) {
            for (double y = Math.floor(minY); y <= Math.ceil(maxY); y++) {
               for (double z = Math.floor(minZ); z <= Math.ceil(maxZ); z++) {
                  Location testLoc = new Location(this.top.getWorld(), x, y, z);
                  if (this.contains(testLoc)) {
                     this.locationList.add(testLoc);
                  }
               }
            }
         }

         return this.locationList;
      }
   }
}
