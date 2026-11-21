package com.magmaguy.elitemobs.magmacore.ai.route;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.util.Vector;

public final class CurvedRoute {
   private final List<Vector> samples;
   private final double[] distances;

   public CurvedRoute(List<Vector> points) {
      if (points.size() < 2) {
         throw new IllegalArgumentException("A route needs at least two waypoints");
      } else {
         List<Vector> nodes = points.stream().<Vector>map(Vector::clone).toList();

         for (int i = 0; i < nodes.size(); i++) {
            Vector p = nodes.get(i);
            if (!Double.isFinite(p.getX()) || !Double.isFinite(p.getY()) || !Double.isFinite(p.getZ())) {
               throw new IllegalArgumentException("Waypoint coordinates must be finite");
            }

            if (i > 0) {
               double length = p.distance(nodes.get(i - 1));
               if (!Double.isFinite(length) || length == 0.0) {
                  throw new IllegalArgumentException("Consecutive waypoints must have a finite, nonzero distance");
               }
            }
         }

         ArrayList<Vector> result = new ArrayList<>();
         result.add(nodes.getFirst().clone());

         for (int i = 0; i < nodes.size() - 1; i++) {
            Vector b = nodes.get(i);
            Vector c = nodes.get(i + 1);
            Vector a = i == 0 ? b.clone().multiply(2).subtract(c) : nodes.get(i - 1);
            Vector d = i + 2 == nodes.size() ? c.clone().multiply(2).subtract(b) : nodes.get(i + 2);
            int steps = Math.max(16, (int)Math.ceil(b.distance(c) * 8.0));

            for (int step = 1; step <= steps; step++) {
               result.add(interpolate(a, b, c, d, (double)step / steps));
            }
         }

         this.samples = List.copyOf(result);
         this.distances = new double[this.samples.size()];

         for (int i = 1; i < this.distances.length; i++) {
            this.distances[i] = this.distances[i - 1] + this.samples.get(i).distance(this.samples.get(i - 1));
         }
      }
   }

   public double length() {
      return this.distances[this.distances.length - 1];
   }

   public Vector at(double distance) {
      if (!Double.isFinite(distance)) {
         throw new IllegalArgumentException("Distance must be finite");
      } else if (distance <= 0.0) {
         return this.samples.getFirst().clone();
      } else if (distance >= this.length()) {
         return this.samples.getLast().clone();
      } else {
         int index = Arrays.binarySearch(this.distances, distance);
         if (index >= 0) {
            return this.samples.get(index).clone();
         } else {
            int next = -index - 1;
            double fraction = (distance - this.distances[next - 1]) / (this.distances[next] - this.distances[next - 1]);
            return mix(this.samples.get(next - 1), this.samples.get(next), fraction);
         }
      }
   }

   private static Vector interpolate(Vector a, Vector b, Vector c, Vector d, double fraction) {
      double t0 = 0.0;
      double t1 = Math.sqrt(a.distance(b));
      double t2 = t1 + Math.sqrt(b.distance(c));
      double t3 = t2 + Math.sqrt(c.distance(d));
      double t = t1 + fraction * (t2 - t1);
      Vector a1 = mix(a, b, (t - t0) / (t1 - t0));
      Vector a2 = mix(b, c, (t - t1) / (t2 - t1));
      Vector a3 = mix(c, d, (t - t2) / (t3 - t2));
      Vector b1 = mix(a1, a2, (t - t0) / (t2 - t0));
      Vector b2 = mix(a2, a3, (t - t1) / (t3 - t1));
      return mix(b1, b2, (t - t1) / (t2 - t1));
   }

   private static Vector mix(Vector a, Vector b, double fraction) {
      return a.clone().multiply(1.0 - fraction).add(b.clone().multiply(fraction));
   }
}
