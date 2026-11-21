package com.magmaguy.elitemobs.magmacore.scripting.zones;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

public class Sphere extends Shape {
   protected double radius;
   protected List<Vector> locationVectors = null;
   protected List<Vector> edgeVectors = null;
   protected double borderRadius = 1.0;
   protected Location centerLocation;

   public Sphere(double radius, Location centerLocation, double borderRadius) {
      this.radius = radius;
      this.centerLocation = centerLocation.clone();
      this.borderRadius = borderRadius;
   }

   @Override
   public boolean contains(Location position) {
      return this.centerLocation.distanceSquared(position) < Math.pow(this.radius, 2.0);
   }

   @Override
   public boolean contains(LivingEntity livingEntity) {
      double entityRadius = (livingEntity.getEyeLocation().getY() - livingEntity.getLocation().getY()) / 2.0;
      Location EntityCenterLocation = livingEntity.getLocation().add(new Vector(0.0, entityRadius, 0.0));
      double radiusSum = this.radius + entityRadius;
      return EntityCenterLocation.distanceSquared(this.centerLocation) <= Math.pow(radiusSum, 2.0);
   }

   @Override
   public boolean borderContains(Location position) {
      Sphere innerSphere = new Sphere(this.borderRadius, this.centerLocation, this.borderRadius);
      return this.contains(position) && !innerSphere.contains(position);
   }

   @Override
   public boolean borderContains(LivingEntity livingEntity) {
      Sphere innerSphere = new Sphere(this.borderRadius, this.centerLocation, this.borderRadius);
      return this.contains(livingEntity) && !innerSphere.contains(livingEntity);
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

   protected List<Vector> getLocationVectors() {
      if (this.locationVectors != null) {
         return this.locationVectors;
      } else {
         this.locationVectors = new ArrayList<>();

         for (int x = (int)(-this.radius); x < (int)this.radius; x++) {
            for (int z = (int)(-this.radius); z < (int)this.radius; z++) {
               for (int y = (int)(-this.radius); y < this.radius; y++) {
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
