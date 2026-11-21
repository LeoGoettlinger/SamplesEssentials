package com.magmaguy.elitemobs.advancedcombat.abilities;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public record AbilityRuntimeObservation(
   AbilityRuntimeObservation.Kind kind,
   UUID casterId,
   UUID targetId,
   String abilityId,
   double amount,
   int durationTicks,
   int count,
   Set<AbilityEffect> effects,
   Set<AbilityMechanic> mechanics
) {
   public AbilityRuntimeObservation(
      AbilityRuntimeObservation.Kind kind,
      UUID casterId,
      UUID targetId,
      String abilityId,
      double amount,
      int durationTicks,
      int count,
      Set<AbilityEffect> effects,
      Set<AbilityMechanic> mechanics
   ) {
      kind = Objects.requireNonNull(kind, "kind");
      casterId = Objects.requireNonNull(casterId, "casterId");
      abilityId = Objects.requireNonNull(abilityId, "abilityId");
      if (!Double.isFinite(amount)) {
         throw new IllegalArgumentException("amount must be finite");
      } else if (durationTicks >= 0 && count >= 0) {
         effects = Set.copyOf(effects);
         mechanics = Set.copyOf(mechanics);
         this.kind = kind;
         this.casterId = casterId;
         this.targetId = targetId;
         this.abilityId = abilityId;
         this.amount = amount;
         this.durationTicks = durationTicks;
         this.count = count;
         this.effects = effects;
         this.mechanics = mechanics;
      } else {
         throw new IllegalArgumentException("duration and count must be non-negative");
      }
   }

   public static AbilityRuntimeObservation effect(
      AbilityRuntimeObservation.Kind kind, UUID casterId, UUID targetId, String abilityId, double amount, int durationTicks, AbilityEffect effect
   ) {
      return new AbilityRuntimeObservation(kind, casterId, targetId, abilityId, amount, durationTicks, 1, Set.of(effect), Set.of());
   }

   public static AbilityRuntimeObservation state(
      AbilityRuntimeObservation.Kind kind, UUID casterId, UUID targetId, String abilityId, double amount, int durationTicks, AbilityMechanic mechanic
   ) {
      if (kind != AbilityRuntimeObservation.Kind.STATE_ARMED
         && kind != AbilityRuntimeObservation.Kind.STATE_CONSUMED
         && kind != AbilityRuntimeObservation.Kind.RETALIATION_STORED
         && kind != AbilityRuntimeObservation.Kind.RETALIATION_RELEASED) {
         throw new IllegalArgumentException("Not a state observation kind: " + kind);
      } else {
         return new AbilityRuntimeObservation(kind, casterId, targetId, abilityId, amount, durationTicks, 1, Set.of(), Set.of(mechanic));
      }
   }

   public static enum Kind {
      DAMAGE,
      HEAL,
      SHIELD,
      CONTROL,
      TAUNT,
      STATUS_APPLIED,
      MODIFIER_APPLIED,
      MODIFIER_TRIGGERED,
      STATE_ARMED,
      STATE_CONSUMED,
      DAMAGE_REDIRECTED,
      DAMAGE_SHARED,
      STATUS_BLOCKED,
      RETALIATION_STORED,
      RETALIATION_RELEASED,
      SUMMON_SPAWNED,
      SUMMON_CLEARED,
      CONSTRUCT_SPAWNED,
      CONSTRUCT_CLEARED,
      FIELD_PULSE,
      PROJECTILE_IMPACT,
      CAST_FAILED,
      RESOURCE_SPENT,
      RESOURCE_GAINED,
      RESOURCE_DELTA,
      LIFECYCLE_CLEARED;
   }
}
