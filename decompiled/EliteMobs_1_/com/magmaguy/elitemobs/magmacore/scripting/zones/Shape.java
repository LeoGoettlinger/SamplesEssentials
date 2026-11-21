package com.magmaguy.elitemobs.magmacore.scripting.zones;

import java.util.List;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.BoundingBox;

public abstract class Shape {
   public abstract boolean contains(Location var1);

   public abstract boolean contains(LivingEntity var1);

   public abstract boolean borderContains(Location var1);

   public abstract boolean borderContains(LivingEntity var1);

   public abstract void visualize(Particle var1);

   public abstract Location getCenter();

   public BoundingBox getEntityQueryBounds() {
      return null;
   }

   public abstract List<Location> getEdgeLocations();

   public abstract List<Location> getLocations();
}
