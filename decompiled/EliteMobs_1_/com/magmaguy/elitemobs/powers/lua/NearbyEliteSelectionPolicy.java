package com.magmaguy.elitemobs.powers.lua;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

final class NearbyEliteSelectionPolicy {
   static final double MAXIMUM_RADIUS = 64.0;
   static final int MAXIMUM_RESULTS = 32;

   private NearbyEliteSelectionPolicy() {
   }

   static List<NearbyEliteSelectionPolicy.Candidate> select(UUID sourceWorldId, double radius, List<NearbyEliteSelectionPolicy.Candidate> candidates) {
      Objects.requireNonNull(sourceWorldId, "sourceWorldId");
      Objects.requireNonNull(candidates, "candidates");
      validateRadius(radius);
      double radiusSquared = radius * radius;
      return candidates.stream()
         .filter(NearbyEliteSelectionPolicy.Candidate::tracked)
         .filter(NearbyEliteSelectionPolicy.Candidate::alive)
         .filter(candidate -> candidate.worldId().equals(sourceWorldId))
         .filter(candidate -> candidate.distanceSquared() <= radiusSquared)
         .sorted(
            Comparator.comparingDouble(NearbyEliteSelectionPolicy.Candidate::distanceSquared).thenComparing(NearbyEliteSelectionPolicy.Candidate::entityId)
         )
         .limit(32L)
         .toList();
   }

   static void validateRadius(double radius) {
      if (!Double.isFinite(radius) || radius <= 0.0 || radius > 64.0) {
         throw new IllegalArgumentException("Elite query radius must be finite and between 0 and 64.0");
      }
   }

   record Candidate(UUID entityId, UUID worldId, boolean tracked, boolean alive, double distanceSquared) {
      Candidate(UUID entityId, UUID worldId, boolean tracked, boolean alive, double distanceSquared) {
         Objects.requireNonNull(entityId, "entityId");
         Objects.requireNonNull(worldId, "worldId");
         if (Double.isFinite(distanceSquared) && !(distanceSquared < 0.0)) {
            this.entityId = entityId;
            this.worldId = worldId;
            this.tracked = tracked;
            this.alive = alive;
            this.distanceSquared = distanceSquared;
         } else {
            throw new IllegalArgumentException("Candidate distance must be finite and non-negative");
         }
      }
   }
}
