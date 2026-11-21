package com.magmaguy.elitemobs.pathfinding;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

final class LeashReturnTracker {
   private static final int SOFT_RETURN = 1;
   private static final int HARD_RETURN = 2;
   private final Map<UUID, Integer> activeReturns = new HashMap<>();

   void begin(UUID entityUuid, boolean hardObjective) {
      int returnType = hardObjective ? 2 : 1;
      this.activeReturns.merge(entityUuid, returnType, (current, added) -> current | added);
   }

   void end(UUID entityUuid, boolean hardObjective) {
      int returnType = hardObjective ? 2 : 1;
      this.activeReturns.computeIfPresent(entityUuid, (ignored, current) -> {
         int remaining = current & ~returnType;
         return remaining == 0 ? null : remaining;
      });
   }

   boolean isReturning(UUID entityUuid) {
      return entityUuid != null && this.activeReturns.containsKey(entityUuid);
   }

   boolean isEmpty() {
      return this.activeReturns.isEmpty();
   }

   void clear(UUID entityUuid) {
      if (entityUuid != null) {
         this.activeReturns.remove(entityUuid);
      }
   }

   void clear() {
      this.activeReturns.clear();
   }
}
