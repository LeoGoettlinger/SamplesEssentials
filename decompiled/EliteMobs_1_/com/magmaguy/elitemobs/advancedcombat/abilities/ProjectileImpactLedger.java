package com.magmaguy.elitemobs.advancedcombat.abilities;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

final class ProjectileImpactLedger<T> {
   private final Map<UUID, ProjectileImpactLedger.Entry<T>> entries = new LinkedHashMap<>();

   boolean register(UUID projectileId, UUID casterId, T payload) {
      return projectileId != null && casterId != null && payload != null
         ? this.entries.putIfAbsent(projectileId, new ProjectileImpactLedger.Entry<>(casterId, payload)) == null
         : false;
   }

   Optional<T> claim(UUID projectileId) {
      return this.retire(projectileId);
   }

   Optional<T> retire(UUID projectileId) {
      ProjectileImpactLedger.Entry<T> removed = this.entries.remove(projectileId);
      return removed == null ? Optional.empty() : Optional.of(removed.payload());
   }

   List<T> retireCaster(UUID casterId) {
      List<T> retired = new ArrayList<>();
      this.entries.entrySet().removeIf(entry -> {
         if (!entry.getValue().casterId().equals(casterId)) {
            return false;
         } else {
            retired.add(entry.getValue().payload());
            return true;
         }
      });
      return List.copyOf(retired);
   }

   List<T> retireAll() {
      List<T> retired = this.entries.values().stream().map(ProjectileImpactLedger.Entry::payload).toList();
      this.entries.clear();
      return retired;
   }

   boolean contains(UUID projectileId) {
      return this.entries.containsKey(projectileId);
   }

   private record Entry<T>(UUID casterId, T payload) {
   }
}
