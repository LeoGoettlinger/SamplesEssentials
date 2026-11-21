package com.magmaguy.elitemobs.advancedcombat.abilities;

import java.util.ArrayList;
import java.util.List;

final class ClusterImpactPlan {
   private ClusterImpactPlan() {
   }

   static List<ClusterImpactPlan.Impact> create(double authoredRadius, int impactCount, double phaseRadians) {
      double radius = Math.max(1.0, authoredRadius);
      int count = Math.max(1, Math.min(8, impactCount));
      double blastRadius = Math.max(1.25, radius * 0.35);
      double availableOffset = Math.max(0.0, radius - blastRadius);
      List<ClusterImpactPlan.Impact> impacts = new ArrayList<>(count);
      impacts.add(new ClusterImpactPlan.Impact(0.0, 0.0, 0, blastRadius));

      for (int index = 1; index < count; index++) {
         double angle = phaseRadians + (index - 1.0) * Math.PI * 2.0 / Math.max(1, count - 1);
         double distance = availableOffset * (0.62 + 0.3 * (index % 2));
         impacts.add(new ClusterImpactPlan.Impact(Math.cos(angle) * distance, Math.sin(angle) * distance, 3 + index * 3, blastRadius));
      }

      return List.copyOf(impacts);
   }

   record Impact(double offsetX, double offsetZ, int delayTicks, double blastRadius) {
      Impact(double offsetX, double offsetZ, int delayTicks, double blastRadius) {
         if (Double.isFinite(offsetX) && Double.isFinite(offsetZ) && Double.isFinite(blastRadius) && !(blastRadius <= 0.0) && delayTicks >= 0) {
            this.offsetX = offsetX;
            this.offsetZ = offsetZ;
            this.delayTicks = delayTicks;
            this.blastRadius = blastRadius;
         } else {
            throw new IllegalArgumentException("invalid cluster impact");
         }
      }
   }
}
