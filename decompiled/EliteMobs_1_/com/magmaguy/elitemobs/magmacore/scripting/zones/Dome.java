package com.magmaguy.elitemobs.magmacore.scripting.zones;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

public class Dome extends Sphere {
   public Dome(double radius, Location centerLocation, double borderRadius) {
      super(radius, centerLocation, borderRadius);
   }

   @Override
   public boolean contains(Location position) {
      return super.contains(position) && position.getY() > this.centerLocation.getY() - 1.0;
   }

   @Override
   public boolean borderContains(Location position) {
      return super.borderContains(position) && position.getY() > this.centerLocation.getY() - 1.0;
   }

   @Override
   public boolean borderContains(LivingEntity livingEntity) {
      return super.borderContains(livingEntity) && livingEntity.getLocation().getY() > this.centerLocation.getY() - 1.0;
   }

   @Override
   protected List<Vector> getLocationVectors() {
      if (this.locationVectors != null) {
         return this.locationVectors;
      } else {
         this.locationVectors = new ArrayList<>();

         for (int x = (int)(-this.radius); x < (int)this.radius; x++) {
            for (int z = (int)(-this.radius); z < (int)this.radius; z++) {
               for (int y = 0; y < this.radius; y++) {
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
}
