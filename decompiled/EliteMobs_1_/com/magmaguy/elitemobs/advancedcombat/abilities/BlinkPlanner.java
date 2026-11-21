package com.magmaguy.elitemobs.advancedcombat.abilities;

import java.util.Optional;
import java.util.OptionalDouble;
import org.bukkit.Location;
import org.bukkit.entity.Player;

final class BlinkPlanner {
   private static final double MINIMUM_DISTANCE = 0.5;
   private static final double HEIGHT_INCREMENT = 0.0625;
   private static final int HEIGHT_STEPS = 16;
   private static final double POSITION_EPSILON_SQUARED = 1.0E-8;

   private BlinkPlanner() {
   }

   static Optional<BlinkPlanner.Plan> plan(Player player, double maximumDistance) {
      if (Double.isFinite(maximumDistance) && !(maximumDistance < 0.5)) {
         Location start = player.getLocation().clone();
         return !SafeMovement.safeStandingPlayerVolume(player, start)
            ? Optional.empty()
            : GroundBlinkPath.scan(
                  start.toVector(),
                  player.getEyeLocation().getDirection(),
                  maximumDistance,
                  (x, z, previousY) -> standingHeight(player, start, x, z, previousY)
               )
               .map(
                  destination -> new BlinkPlanner.Plan(
                     start,
                     new Location(start.getWorld(), destination.x(), destination.y(), destination.z(), start.getYaw(), start.getPitch()),
                     destination.distance(),
                     maximumDistance
                  )
               );
      } else {
         return Optional.empty();
      }
   }

   static boolean stillValid(Player player, BlinkPlanner.Plan accepted) {
      Location current = player.getLocation();
      if (!samePosition(current, accepted.start())) {
         return false;
      } else {
         Optional<BlinkPlanner.Plan> currentPlan = plan(player, accepted.maximumDistance());
         return currentPlan.isPresent() && samePosition(currentPlan.get().destination(), accepted.destination());
      }
   }

   private static OptionalDouble standingHeight(Player player, Location origin, double x, double z, double previousY) {
      Location candidate = new Location(origin.getWorld(), x, previousY, z, origin.getYaw(), origin.getPitch());
      if (SafeMovement.safeStandingPlayerVolume(player, candidate)) {
         return OptionalDouble.of(previousY);
      } else {
         for (int step = 1; step <= 16; step++) {
            double offset = step * 0.0625;
            candidate.setY(previousY + offset);
            if (SafeMovement.safeStandingPlayerVolume(player, candidate)) {
               return OptionalDouble.of(candidate.getY());
            }

            candidate.setY(previousY - offset);
            if (SafeMovement.safeStandingPlayerVolume(player, candidate)) {
               return OptionalDouble.of(candidate.getY());
            }
         }

         return OptionalDouble.empty();
      }
   }

   private static boolean samePosition(Location first, Location second) {
      return first.getWorld() != null && second.getWorld() != null && first.getWorld().equals(second.getWorld()) && first.distanceSquared(second) <= 1.0E-8;
   }

   record Plan(Location start, Location destination, double distance, double maximumDistance) {
      Plan(Location start, Location destination, double distance, double maximumDistance) {
         start = start.clone();
         destination = destination.clone();
         if (Double.isFinite(distance) && !(distance < 0.0) && Double.isFinite(maximumDistance) && !(maximumDistance < 0.5)) {
            this.start = start;
            this.destination = destination;
            this.distance = distance;
            this.maximumDistance = maximumDistance;
         } else {
            throw new IllegalArgumentException("Blink distances must be finite and valid");
         }
      }

      public Location start() {
         return this.start.clone();
      }

      public Location destination() {
         return this.destination.clone();
      }
   }
}
