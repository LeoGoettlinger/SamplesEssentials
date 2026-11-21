package com.magmaguy.elitemobs.advancedcombat.abilities;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.LongSupplier;
import java.util.function.Predicate;

final class TimedOutgoingDamageReduction implements AutoCloseable {
   private static final long NANOS_PER_TICK = 50000000L;
   private final LongSupplier nanoTime;
   private final Map<UUID, Map<TimedOutgoingDamageReduction.LeaseKey, TimedOutgoingDamageReduction.Lease>> leasesByTarget = new HashMap<>();
   private boolean closed;

   TimedOutgoingDamageReduction(LongSupplier nanoTime) {
      this.nanoTime = Objects.requireNonNull(nanoTime, "nanoTime");
   }

   double register(UUID sourceId, UUID targetId, String abilityId, double authoredMultiplier, int effectiveLevel, int durationTicks) {
      return this.register(sourceId, targetId, abilityId, authoredMultiplier, effectiveLevel, 1.0, durationTicks);
   }

   double register(UUID sourceId, UUID targetId, String abilityId, double authoredMultiplier, int effectiveLevel, double potencyMultiplier, int durationTicks) {
      Objects.requireNonNull(sourceId, "sourceId");
      Objects.requireNonNull(targetId, "targetId");
      Objects.requireNonNull(abilityId, "abilityId");
      if (!Double.isFinite(authoredMultiplier) || authoredMultiplier < 0.0 || authoredMultiplier >= 1.0) {
         throw new IllegalArgumentException("authoredMultiplier must be within [0, 1)");
      } else if (effectiveLevel < 1) {
         throw new IllegalArgumentException("effectiveLevel must be positive");
      } else if (!Double.isFinite(potencyMultiplier) || potencyMultiplier <= 0.0) {
         throw new IllegalArgumentException("potencyMultiplier must be finite and positive");
      } else if (durationTicks <= 0) {
         throw new IllegalArgumentException("durationTicks must be positive");
      } else if (this.closed) {
         throw new IllegalStateException("Damage-reduction ledger is closed");
      } else {
         double potencyAdjusted = 1.0 - Math.min(0.95, (1.0 - authoredMultiplier) * potencyMultiplier);
         double multiplier = ActiveAbilityLevelScaling.modifier(potencyAdjusted, effectiveLevel);
         TimedOutgoingDamageReduction.LeaseKey key = new TimedOutgoingDamageReduction.LeaseKey(sourceId, abilityId);
         this.leasesByTarget
            .computeIfAbsent(targetId, ignored -> new HashMap<>())
            .put(key, new TimedOutgoingDamageReduction.Lease(sourceId, abilityId, multiplier, this.expiresAt(durationTicks)));
         return multiplier;
      }
   }

   Optional<TimedOutgoingDamageReduction.Application> apply(UUID targetId, double originalDamage, boolean recipientEligible, Predicate<UUID> liveSource) {
      Objects.requireNonNull(targetId, "targetId");
      Objects.requireNonNull(liveSource, "liveSource");
      if (this.closed) {
         return Optional.empty();
      } else if (!recipientEligible) {
         return Optional.empty();
      } else if (Double.isFinite(originalDamage) && !(originalDamage <= 0.0)) {
         Map<TimedOutgoingDamageReduction.LeaseKey, TimedOutgoingDamageReduction.Lease> leases = this.leasesByTarget.get(targetId);
         if (leases == null) {
            return Optional.empty();
         } else {
            long now = this.nanoTime.getAsLong();
            leases.entrySet().removeIf(entry -> entry.getValue().expiresAtNanos() <= now || !liveSource.test(entry.getValue().sourceId()));
            if (leases.isEmpty()) {
               this.leasesByTarget.remove(targetId);
               return Optional.empty();
            } else {
               TimedOutgoingDamageReduction.Lease strongest = leases.values()
                  .stream()
                  .min(Comparator.comparingDouble(TimedOutgoingDamageReduction.Lease::multiplier))
                  .orElseThrow();
               double modifiedDamage = Math.max(0.0, originalDamage * strongest.multiplier());
               return Optional.of(
                  new TimedOutgoingDamageReduction.Application(
                     strongest.sourceId(), targetId, strongest.abilityId(), strongest.multiplier(), originalDamage, modifiedDamage
                  )
               );
            }
         }
      } else {
         return Optional.empty();
      }
   }

   void clearSource(UUID sourceId) {
      Objects.requireNonNull(sourceId, "sourceId");
      if (!this.closed) {
         this.leasesByTarget.entrySet().removeIf(entry -> {
            entry.getValue().entrySet().removeIf(lease -> lease.getKey().sourceId().equals(sourceId));
            return entry.getValue().isEmpty();
         });
      }
   }

   void clearTarget(UUID targetId) {
      Objects.requireNonNull(targetId, "targetId");
      if (!this.closed) {
         this.leasesByTarget.remove(targetId);
      }
   }

   @Override
   public void close() {
      if (!this.closed) {
         this.closed = true;
         this.leasesByTarget.clear();
      }
   }

   private long expiresAt(int durationTicks) {
      long now = this.nanoTime.getAsLong();
      long durationNanos = durationTicks * 50000000L;

      try {
         return Math.addExact(now, durationNanos);
      } catch (ArithmeticException var7) {
         return Long.MAX_VALUE;
      }
   }

   record Application(UUID sourceId, UUID targetId, String abilityId, double multiplier, double originalDamage, double modifiedDamage) {
      double reducedDamage() {
         return Math.max(0.0, this.originalDamage - this.modifiedDamage);
      }
   }

   private record Lease(UUID sourceId, String abilityId, double multiplier, long expiresAtNanos) {
   }

   private record LeaseKey(UUID sourceId, String abilityId) {
   }
}
