package com.magmaguy.elitemobs.easyminecraftgoals.v26.flee;

import java.util.ArrayList;
import java.util.List;

final class FleeDestinationPlanner {
   private static final double[] OFFSETS_DEGREES = new double[]{0.0, 25.0, -25.0, 50.0, -50.0, 75.0, -75.0};

   private FleeDestinationPlanner() {
   }

   static List<FleeDestinationPlanner.Destination> candidates(
      double bodyX, double bodyY, double bodyZ, double threatX, double threatY, double threatZ, double distance
   ) {
      if (Double.isFinite(distance) && !(distance <= 0.0)) {
         double awayX = bodyX - threatX;
         double awayZ = bodyZ - threatZ;
         double magnitude = Math.hypot(awayX, awayZ);
         if (magnitude < 1.0E-6) {
            awayX = 0.0;
            awayZ = 1.0;
         } else {
            awayX /= magnitude;
            awayZ /= magnitude;
         }

         List<FleeDestinationPlanner.Destination> candidates = new ArrayList<>(OFFSETS_DEGREES.length);

         for (double offsetDegrees : OFFSETS_DEGREES) {
            double radians = Math.toRadians(offsetDegrees);
            double cos = Math.cos(radians);
            double sin = Math.sin(radians);
            double directionX = awayX * cos - awayZ * sin;
            double directionZ = awayX * sin + awayZ * cos;
            candidates.add(new FleeDestinationPlanner.Destination(bodyX + directionX * distance, bodyY, bodyZ + directionZ * distance));
         }

         return List.copyOf(candidates);
      } else {
         throw new IllegalArgumentException("distance must be finite and positive");
      }
   }

   record Destination(double x, double y, double z) {
   }
}
