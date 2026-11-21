package com.magmaguy.elitemobs.advancedcombat.abilities;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import org.bukkit.util.Vector;

final class GroundBlinkPath {
   static final double SCAN_STEP = 0.25;
   static final double MINIMUM_DISTANCE = 0.5;
   private static final double MAXIMUM_HEIGHT_CHANGE = 1.0;
   private static final double EPSILON = 1.0E-7;

   private GroundBlinkPath() {
   }

   static Optional<GroundBlinkPath.Destination> scan(
      Vector start, Vector lookDirection, double maximumDistance, GroundBlinkPath.StandingHeightResolver heightResolver
   ) {
      Objects.requireNonNull(start, "start");
      Objects.requireNonNull(lookDirection, "lookDirection");
      Objects.requireNonNull(heightResolver, "heightResolver");
      if (finite(start) && finite(lookDirection) && Double.isFinite(maximumDistance) && !(maximumDistance < 0.5)) {
         Vector direction = lookDirection.clone().setY(0.0);
         if (direction.lengthSquared() < 1.0E-7) {
            return Optional.empty();
         } else {
            direction.normalize();
            GroundBlinkPath.Destination lastValid = null;
            double previousY = start.getY();
            int steps = Math.max(1, (int)Math.ceil(maximumDistance / 0.25));

            for (int step = 1; step <= steps; step++) {
               double distance = Math.min(step * 0.25, maximumDistance);
               double x = start.getX() + direction.getX() * distance;
               double z = start.getZ() + direction.getZ() * distance;
               OptionalDouble standingY = heightResolver.resolve(x, z, previousY);
               if (standingY.isEmpty() || !Double.isFinite(standingY.getAsDouble()) || Math.abs(standingY.getAsDouble() - previousY) > 1.0000001) {
                  break;
               }

               previousY = standingY.getAsDouble();
               lastValid = new GroundBlinkPath.Destination(x, previousY, z, distance);
            }

            return lastValid != null && !(lastValid.distance() + 1.0E-7 < 0.5) ? Optional.of(lastValid) : Optional.empty();
         }
      } else {
         return Optional.empty();
      }
   }

   private static boolean finite(Vector vector) {
      return Double.isFinite(vector.getX()) && Double.isFinite(vector.getY()) && Double.isFinite(vector.getZ());
   }

   record Destination(double x, double y, double z, double distance) {
      Destination(double x, double y, double z, double distance) {
         if (Double.isFinite(x) && Double.isFinite(y) && Double.isFinite(z) && Double.isFinite(distance) && !(distance < 0.0)) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.distance = distance;
         } else {
            throw new IllegalArgumentException("Blink destination values must be finite");
         }
      }
   }

   @FunctionalInterface
   interface StandingHeightResolver {
      OptionalDouble resolve(double var1, double var3, double var5);
   }
}
