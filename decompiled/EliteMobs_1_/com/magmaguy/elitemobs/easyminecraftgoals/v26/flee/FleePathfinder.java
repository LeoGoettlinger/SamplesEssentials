package com.magmaguy.elitemobs.easyminecraftgoals.v26.flee;

import java.util.Objects;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.pathfinder.Path;
import org.bukkit.Location;

public final class FleePathfinder {
   private static final double PATH_DISTANCE = 10.0;

   private FleePathfinder() {
   }

   public static Path findPath(PathfinderMob mob, Location threatLocation) {
      Objects.requireNonNull(mob, "mob");
      Objects.requireNonNull(threatLocation, "threatLocation");

      for (FleeDestinationPlanner.Destination destination : FleeDestinationPlanner.candidates(
         mob.getX(), mob.getY(), mob.getZ(), threatLocation.getX(), threatLocation.getY(), threatLocation.getZ(), 10.0
      )) {
         Path path = mob.getNavigation().createPath(destination.x(), destination.y(), destination.z(), 0);
         if (path != null && path.canReach()) {
            return path;
         }
      }

      return null;
   }
}
