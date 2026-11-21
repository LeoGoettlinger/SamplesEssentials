package com.magmaguy.elitemobs.advancedcombat.abilities;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

final class PiercingLinePolicy {
   private static final double EPSILON = 1.0E-9;

   private PiercingLinePolicy() {
   }

   static <T> List<PiercingLinePolicy.Intersection<T>> intersections(
      PiercingLinePolicy.Point origin, PiercingLinePolicy.Point direction, double maximumDistance, List<PiercingLinePolicy.Candidate<T>> candidates
   ) {
      Objects.requireNonNull(origin, "origin");
      Objects.requireNonNull(direction, "direction");
      Objects.requireNonNull(candidates, "candidates");
      if (Double.isFinite(maximumDistance) && !(maximumDistance <= 0.0)) {
         double length = Math.sqrt(direction.x() * direction.x() + direction.y() * direction.y() + direction.z() * direction.z());
         if (Double.isFinite(length) && !(length < 1.0E-9)) {
            PiercingLinePolicy.Point normalized = new PiercingLinePolicy.Point(direction.x() / length, direction.y() / length, direction.z() / length);
            List<PiercingLinePolicy.Intersection<T>> hits = new ArrayList<>();

            for (PiercingLinePolicy.Candidate<T> candidate : candidates) {
               double distance = entryDistance(origin, normalized, candidate.bounds(), maximumDistance);
               if (Double.isFinite(distance)) {
                  hits.add(new PiercingLinePolicy.Intersection<>(candidate.target(), distance));
               }
            }

            hits.sort(Comparator.comparingDouble(PiercingLinePolicy.Intersection::distance));
            return List.copyOf(hits);
         } else {
            return List.of();
         }
      } else {
         return List.of();
      }
   }

   private static double entryDistance(PiercingLinePolicy.Point origin, PiercingLinePolicy.Point direction, PiercingLinePolicy.Box box, double maximumDistance) {
      double minimum = 0.0;
      double maximum = maximumDistance;
      double[] origins = new double[]{origin.x(), origin.y(), origin.z()};
      double[] directions = new double[]{direction.x(), direction.y(), direction.z()};
      double[] lower = new double[]{box.minimumX(), box.minimumY(), box.minimumZ()};
      double[] upper = new double[]{box.maximumX(), box.maximumY(), box.maximumZ()};

      for (int axis = 0; axis < 3; axis++) {
         if (Math.abs(directions[axis]) < 1.0E-9) {
            if (origins[axis] < lower[axis] || origins[axis] > upper[axis]) {
               return Double.NaN;
            }
         } else {
            double first = (lower[axis] - origins[axis]) / directions[axis];
            double second = (upper[axis] - origins[axis]) / directions[axis];
            if (first > second) {
               double swap = first;
               first = second;
               second = swap;
            }

            minimum = Math.max(minimum, first);
            maximum = Math.min(maximum, second);
            if (maximum + 1.0E-9 < minimum) {
               return Double.NaN;
            }
         }
      }

      return minimum <= maximumDistance + 1.0E-9 ? Math.max(0.0, minimum) : Double.NaN;
   }

   record Box(double minimumX, double minimumY, double minimumZ, double maximumX, double maximumY, double maximumZ) {
      Box(double minimumX, double minimumY, double minimumZ, double maximumX, double maximumY, double maximumZ) {
         if (!(minimumX > maximumX) && !(minimumY > maximumY) && !(minimumZ > maximumZ)) {
            this.minimumX = minimumX;
            this.minimumY = minimumY;
            this.minimumZ = minimumZ;
            this.maximumX = maximumX;
            this.maximumY = maximumY;
            this.maximumZ = maximumZ;
         } else {
            throw new IllegalArgumentException("box minima must not exceed maxima");
         }
      }
   }

   record Candidate<T>(T target, PiercingLinePolicy.Box bounds) {
      Candidate(T target, PiercingLinePolicy.Box bounds) {
         Objects.requireNonNull(target, "target");
         Objects.requireNonNull(bounds, "bounds");
         this.target = target;
         this.bounds = bounds;
      }
   }

   record Intersection<T>(T target, double distance) {
   }

   record Point(double x, double y, double z) {
   }
}
