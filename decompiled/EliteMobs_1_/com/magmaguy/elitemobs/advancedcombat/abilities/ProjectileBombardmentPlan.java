package com.magmaguy.elitemobs.advancedcombat.abilities;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

public record ProjectileBombardmentPlan(List<ProjectileBombardmentPlan.Impact> impacts, double impactRadius) {
   public ProjectileBombardmentPlan(List<ProjectileBombardmentPlan.Impact> impacts, double impactRadius) {
      impacts = List.copyOf(impacts);
      if (impacts.isEmpty()) {
         throw new IllegalArgumentException("At least one impact is required");
      } else if (Double.isFinite(impactRadius) && !(impactRadius <= 0.0)) {
         this.impacts = impacts;
         this.impactRadius = impactRadius;
      } else {
         throw new IllegalArgumentException("impactRadius must be positive and finite");
      }
   }

   public static ProjectileBombardmentPlan create(
      double areaRadius, int durationTicks, int volleyCount, int projectilesPerVolley, int fallTicks, long spreadSeed
   ) {
      if (!Double.isFinite(areaRadius) || areaRadius <= 0.0) {
         throw new IllegalArgumentException("areaRadius must be positive and finite");
      } else if (volleyCount >= 1 && projectilesPerVolley >= 1) {
         if (fallTicks >= 1 && durationTicks >= fallTicks) {
            SplittableRandom random = new SplittableRandom(spreadSeed);
            List<ProjectileBombardmentPlan.Impact> impacts = new ArrayList<>(volleyCount * projectilesPerVolley);
            int deliveryIndex = 0;

            for (int volley = 0; volley < volleyCount; volley++) {
               int impactTick = volleyCount == 1
                  ? durationTicks
                  : fallTicks + (int)Math.round((double)((durationTicks - fallTicks) * volley) / (volleyCount - 1));
               int launchTick = impactTick - fallTicks;

               for (int projectile = 0; projectile < projectilesPerVolley; projectile++) {
                  double radialDistance = areaRadius * Math.sqrt(random.nextDouble());
                  double angle = random.nextDouble(0.0, Math.PI * 2);
                  impacts.add(
                     new ProjectileBombardmentPlan.Impact(
                        deliveryIndex++, volley, projectile, launchTick, impactTick, Math.cos(angle) * radialDistance, Math.sin(angle) * radialDistance
                     )
                  );
               }
            }

            double impactRadius = Math.max(1.0, Math.min(1.5, areaRadius / 5.0));
            return new ProjectileBombardmentPlan(impacts, impactRadius);
         } else {
            throw new IllegalArgumentException("Duration must contain the projectile fall time");
         }
      } else {
         throw new IllegalArgumentException("Volley and projectile counts must be positive");
      }
   }

   public int maximumDamageDeliveries() {
      return this.impacts.size();
   }

   public record Impact(int deliveryIndex, int volleyIndex, int projectileIndex, int launchTick, int impactTick, double offsetX, double offsetZ) {
      public Impact(int deliveryIndex, int volleyIndex, int projectileIndex, int launchTick, int impactTick, double offsetX, double offsetZ) {
         if (deliveryIndex < 0 || volleyIndex < 0 || projectileIndex < 0 || launchTick < 0 || impactTick <= launchTick) {
            throw new IllegalArgumentException("Impact indices and timing must be valid");
         } else if (Double.isFinite(offsetX) && Double.isFinite(offsetZ)) {
            this.deliveryIndex = deliveryIndex;
            this.volleyIndex = volleyIndex;
            this.projectileIndex = projectileIndex;
            this.launchTick = launchTick;
            this.impactTick = impactTick;
            this.offsetX = offsetX;
            this.offsetZ = offsetZ;
         } else {
            throw new IllegalArgumentException("Impact offsets must be finite");
         }
      }
   }
}
