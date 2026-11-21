package com.magmaguy.elitemobs.magmacore.projectiles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

final class MagicImpactLedger<T> {
   private final Map<UUID, T> active = new HashMap<>();

   void register(UUID projectileId, T flight) {
      Objects.requireNonNull(projectileId, "projectileId");
      Objects.requireNonNull(flight, "flight");
      if (this.active.putIfAbsent(projectileId, flight) != null) {
         throw new IllegalStateException("Magic marker is already registered: " + projectileId);
      }
   }

   T lookup(UUID projectileId) {
      return this.active.get(projectileId);
   }

   boolean claim(UUID projectileId, T expected) {
      return this.active.remove(projectileId, expected);
   }

   List<T> snapshot() {
      return List.copyOf(new ArrayList<>(this.active.values()));
   }
}
