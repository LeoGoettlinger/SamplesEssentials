package com.magmaguy.elitemobs.advancedcombat.constructs;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

final class ConstructLifecycleTracker {
   private final Map<UUID, ConstructLifecycleTracker.Lease> active = new HashMap<>();

   ConstructLifecycleTracker.Lease open(
      UUID constructId, UUID casterId, String abilityId, int durationTicks, ClassConstructVisualRegistry.AnchorMode anchorMode
   ) {
      ConstructLifecycleTracker.Lease lease = new ConstructLifecycleTracker.Lease(constructId, casterId, abilityId, durationTicks, anchorMode);
      if (this.active.putIfAbsent(constructId, lease) != null) {
         throw new IllegalStateException("Duplicate construct lease " + constructId);
      } else {
         return lease;
      }
   }

   Optional<ConstructLifecycleTracker.Lease> close(UUID constructId) {
      return Optional.ofNullable(this.active.remove(constructId));
   }

   int size() {
      return this.active.size();
   }

   record Lease(UUID constructId, UUID casterId, String abilityId, int durationTicks, ClassConstructVisualRegistry.AnchorMode anchorMode) {
      Lease(UUID constructId, UUID casterId, String abilityId, int durationTicks, ClassConstructVisualRegistry.AnchorMode anchorMode) {
         Objects.requireNonNull(constructId, "constructId");
         Objects.requireNonNull(casterId, "casterId");
         Objects.requireNonNull(abilityId, "abilityId");
         Objects.requireNonNull(anchorMode, "anchorMode");
         if (durationTicks < 1) {
            throw new IllegalArgumentException("durationTicks must be positive");
         } else {
            this.constructId = constructId;
            this.casterId = casterId;
            this.abilityId = abilityId;
            this.durationTicks = durationTicks;
            this.anchorMode = anchorMode;
         }
      }
   }
}
