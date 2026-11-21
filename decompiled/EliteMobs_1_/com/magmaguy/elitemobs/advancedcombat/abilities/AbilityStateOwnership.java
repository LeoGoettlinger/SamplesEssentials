package com.magmaguy.elitemobs.advancedcombat.abilities;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

final class AbilityStateOwnership {
   private final Map<AbilityStateOwnership.Bucket, AbilityStateOwnership.Binding> bindings;

   private AbilityStateOwnership(Map<AbilityStateOwnership.Bucket, AbilityStateOwnership.Binding> bindings) {
      this.bindings = Map.copyOf(bindings);
   }

   static AbilityStateOwnership.Builder builder() {
      return new AbilityStateOwnership.Builder();
   }

   void clearSource(UUID sourceId) {
      Objects.requireNonNull(sourceId, "sourceId");
      this.bindings.values().forEach(binding -> binding.clearSource().accept(sourceId));
   }

   void clearAll() {
      this.bindings.values().forEach(binding -> binding.clearAll().run());
   }

   private record Binding(Consumer<UUID> clearSource, Runnable clearAll) {
   }

   static enum Bucket {
      REDIRECTS,
      DEATH_GUARDS,
      TRIGGERED_BARRIERS,
      TRIGGERED_PROTECTIONS,
      DAMAGE_SHARES,
      LIFESTEAL_WINDOWS,
      CONTROL_IMMUNITY,
      DEBUFF_IMMUNITY,
      PLANTED_GUARDS,
      DETONATING_MARKS,
      RETALIATION,
      RECENT_ATTACKERS,
      TAUNTS,
      DEFENSE_BREAKS,
      ACTIVE_FIELDS,
      ACTIVE_WIND_UPS,
      PENDING_HEAL_ECHOES,
      LIFECYCLE_GENERATIONS,
      REDISTRIBUTION_GUARDS,
      OWNED_VELOCITIES;
   }

   static final class Builder {
      private final Map<AbilityStateOwnership.Bucket, AbilityStateOwnership.Binding> bindings = new EnumMap<>(AbilityStateOwnership.Bucket.class);

      AbilityStateOwnership.Builder bind(AbilityStateOwnership.Bucket bucket, Consumer<UUID> clearSource, Runnable clearAll) {
         AbilityStateOwnership.Binding previous = this.bindings
            .putIfAbsent(
               Objects.requireNonNull(bucket, "bucket"),
               new AbilityStateOwnership.Binding(Objects.requireNonNull(clearSource, "clearSource"), Objects.requireNonNull(clearAll, "clearAll"))
            );
         if (previous != null) {
            throw new IllegalStateException("Duplicate state binding for " + bucket);
         } else {
            return this;
         }
      }

      AbilityStateOwnership build() {
         EnumSet<AbilityStateOwnership.Bucket> missing = EnumSet.allOf(AbilityStateOwnership.Bucket.class);
         missing.removeAll(this.bindings.keySet());
         if (!missing.isEmpty()) {
            throw new IllegalStateException("Missing state cleanup bindings: " + missing);
         } else {
            return new AbilityStateOwnership(this.bindings);
         }
      }
   }
}
