package com.magmaguy.elitemobs.easyminecraftgoals.v1_21_R4.flee;

import java.util.Objects;
import net.minecraft.world.entity.EntityCreature;
import net.minecraft.world.level.pathfinder.PathEntity;
import org.bukkit.Location;

public final class FleePathfinder {
   private static final double PATH_DISTANCE = 10.0;

   private FleePathfinder() {
   }

   public static PathEntity findPath(EntityCreature mob, Location threatLocation) {
      Objects.requireNonNull(mob, "mob");
      Objects.requireNonNull(threatLocation, "threatLocation");

      for (FleeDestinationPlanner.Destination destination : FleeDestinationPlanner.candidates(
         mob.dA(), mob.dC(), mob.dG(), threatLocation.getX(), threatLocation.getY(), threatLocation.getZ(), 10.0
      )) {
         PathEntity path = mob.N().a(destination.x(), destination.y(), destination.z(), 0);
         if (path != null && path.j()) {
            return path;
         }
      }

      return null;
   }
}
