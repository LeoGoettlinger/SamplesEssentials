package com.magmaguy.elitemobs.magmacore.scripting.zones;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

public class Cuboid extends Shape {
   private final Float x;
   private final Float y;
   private final Float xBorder;
   private final Float yBorder;
   private Location centerLocation = null;
   private List<Vector> locationVectors = null;
   private List<Vector> edgeVectors = null;
   private Float z;
   private Float zBorder;

   public Cuboid(Float x, Float y, Float z, Float xBorder, Float yBorder, Float zBorder, Location centerLocation) {
      this.x = x;
      this.y = y;
      this.z = z;
      this.xBorder = xBorder;
      this.yBorder = yBorder;
      this.zBorder = zBorder;
      this.centerLocation = centerLocation;
      if (x != 0.0F && z == 0.0F) {
         this.z = x;
      }

      if (xBorder != 0.0F && zBorder == 0.0F) {
         this.zBorder = xBorder;
      }
   }

   @Override
   public boolean contains(Location position) {
      Vector localVector = position.clone().subtract(this.centerLocation).toVector();
      if (Math.abs(localVector.getX()) > this.x.floatValue()) {
         return false;
      } else {
         return Math.abs(localVector.getY()) > this.y.floatValue() ? false : !(Math.abs(localVector.getZ()) > this.z.floatValue());
      }
   }

   @Override
   public boolean contains(LivingEntity livingEntity) {
      return livingEntity.getBoundingBox().overlaps(this.outerBoundingBox());
   }

   private BoundingBox outerBoundingBox() {
      return new BoundingBox(
         Math.floor(this.x.floatValue() / 2.0 + this.centerLocation.getBlockX()),
         this.y.floatValue() / 2.0 + this.centerLocation.getBlockY(),
         Math.floor(this.z.floatValue() / 2.0 + this.centerLocation.getBlockZ()),
         Math.floor(-this.x / 2.0 + this.centerLocation.getBlockX()),
         this.centerLocation.getBlockY(),
         Math.floor(-this.z / 2.0 + this.centerLocation.getBlockZ())
      );
   }

   @Override
   public BoundingBox getEntityQueryBounds() {
      return Float.isFinite(this.x) && Float.isFinite(this.y) && Float.isFinite(this.z) ? this.outerBoundingBox() : null;
   }

   @Override
   public boolean borderContains(Location position) {
      Vector localVector = position.clone().subtract(this.centerLocation).toVector();
      return Math.abs(localVector.getX()) <= this.x.floatValue() / 2.0 && Math.abs(localVector.getX()) >= this.xBorder.floatValue() / 2.0
            || Math.abs(localVector.getZ()) <= this.z.floatValue() / 2.0 && Math.abs(localVector.getZ()) >= this.zBorder.floatValue() / 2.0
         ? !(this.y > 0.0F) || !(Math.abs(localVector.getY()) <= this.y.floatValue()) || !(Math.abs(localVector.getY()) >= this.yBorder.floatValue())
         : false;
   }

   @Override
   public boolean borderContains(LivingEntity livingEntity) {
      return this.contains(livingEntity) && !this.innerCuboidContains(livingEntity);
   }

   private boolean innerCuboidContains(LivingEntity livingEntity) {
      BoundingBox boundingBox = new BoundingBox(
         Math.floor(this.xBorder.floatValue() / 2.0 + this.centerLocation.getBlockX()),
         this.yBorder.floatValue() / 2.0 + this.centerLocation.getBlockY(),
         Math.floor(this.zBorder.floatValue() / 2.0 + this.centerLocation.getBlockZ()),
         Math.floor(-this.xBorder / 2.0 + this.centerLocation.getBlockX()),
         this.centerLocation.getBlockY(),
         Math.floor(-this.zBorder / 2.0 + this.centerLocation.getBlockZ())
      );
      return livingEntity.getBoundingBox().overlaps(boundingBox);
   }

   @Override
   public void visualize(Particle particle) {
   }

   @Override
   public Location getCenter() {
      return this.centerLocation;
   }

   @Override
   public List<Location> getEdgeLocations() {
      return this.convert(this.getEdgeVectors());
   }

   public List<Vector> getEdgeVectors() {
      if (this.edgeVectors != null) {
         return this.edgeVectors;
      } else {
         this.edgeVectors = new ArrayList<>();

         for (Vector vector : this.getLocationVectors()) {
            if (vector.getY() <= this.yBorder.floatValue()
               && (
                  Math.abs(vector.getX()) >= Math.floor(this.xBorder.floatValue() / 2.0)
                     || Math.abs(vector.getZ()) >= Math.floor(this.zBorder.floatValue() / 2.0)
               )) {
               this.edgeVectors.add(vector);
            }
         }

         return this.edgeVectors;
      }
   }

   private List<Vector> getLocationVectors() {
      if (this.locationVectors != null) {
         return this.locationVectors;
      } else {
         this.locationVectors = new ArrayList<>();
         Float xHalf = (float)Math.floor(this.x / 2.0F);
         Float zHalf = (float)Math.floor(this.z / 2.0F);

         for (int localX = 0; localX < this.x; localX++) {
            for (int localZ = 0; localZ < this.z; localZ++) {
               for (int localY = 0; localY < this.y; localY++) {
                  this.locationVectors.add(new Vector(localX - xHalf, localY, localZ - zHalf));
               }
            }
         }

         return this.locationVectors;
      }
   }

   @Override
   public List<Location> getLocations() {
      return this.convert(this.getLocationVectors());
   }

   private List<Location> convert(List<Vector> vectors) {
      return vectors.stream().map(edge -> this.centerLocation.clone().add(edge)).collect(Collectors.toList());
   }
}
