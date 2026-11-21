package com.magmaguy.elitemobs.advancedcombat.resources;

import com.magmaguy.elitemobs.advancedcombat.classes.ClassResourceType;
import java.util.Objects;

public record ClassResourceDefinition(
   ClassResourceType type,
   double maximum,
   double initialAmount,
   double inCombatTickDelta,
   double outOfCombatTickDelta,
   double damageDealtFlatGain,
   double damageReceivedHealthEquivalentGain,
   double damageReceivedFlatChange,
   long recoveryDelayAfterDamageTicks,
   double healingHealthEquivalentGain,
   double preventedDamageHealthEquivalentGain,
   double tauntGainPerEnemy,
   double tauntGainCap,
   ClassResourceDefinition.NearbyRecoveryBonus nearbyRecoveryBonus,
   ClassResourceDefinition.DamageFreeRecoveryBonus damageFreeRecoveryBonus
) {
   public ClassResourceDefinition(
      ClassResourceType type,
      double maximum,
      double initialAmount,
      double inCombatTickDelta,
      double outOfCombatTickDelta,
      double damageDealtFlatGain,
      double damageReceivedHealthEquivalentGain,
      double damageReceivedFlatChange,
      long recoveryDelayAfterDamageTicks,
      double healingHealthEquivalentGain,
      double preventedDamageHealthEquivalentGain,
      double tauntGainPerEnemy,
      double tauntGainCap,
      ClassResourceDefinition.NearbyRecoveryBonus nearbyRecoveryBonus,
      ClassResourceDefinition.DamageFreeRecoveryBonus damageFreeRecoveryBonus
   ) {
      type = Objects.requireNonNull(type, "type");
      requireFinitePositive(maximum, "maximum");
      requireFiniteNonNegative(initialAmount, "initialAmount");
      if (initialAmount > maximum) {
         throw new IllegalArgumentException("initialAmount must not exceed maximum");
      } else {
         requireFinite(inCombatTickDelta, "inCombatTickDelta");
         requireFinite(outOfCombatTickDelta, "outOfCombatTickDelta");
         requireFiniteNonNegative(damageDealtFlatGain, "damageDealtFlatGain");
         requireFiniteNonNegative(damageReceivedHealthEquivalentGain, "damageReceivedHealthEquivalentGain");
         requireFinite(damageReceivedFlatChange, "damageReceivedFlatChange");
         if (recoveryDelayAfterDamageTicks < 0L) {
            throw new IllegalArgumentException("recoveryDelayAfterDamageTicks must not be negative");
         } else {
            requireFiniteNonNegative(healingHealthEquivalentGain, "healingHealthEquivalentGain");
            requireFiniteNonNegative(preventedDamageHealthEquivalentGain, "preventedDamageHealthEquivalentGain");
            requireFiniteNonNegative(tauntGainPerEnemy, "tauntGainPerEnemy");
            requireFiniteNonNegative(tauntGainCap, "tauntGainCap");
            nearbyRecoveryBonus = Objects.requireNonNull(nearbyRecoveryBonus, "nearbyRecoveryBonus");
            damageFreeRecoveryBonus = Objects.requireNonNull(damageFreeRecoveryBonus, "damageFreeRecoveryBonus");
            this.type = type;
            this.maximum = maximum;
            this.initialAmount = initialAmount;
            this.inCombatTickDelta = inCombatTickDelta;
            this.outOfCombatTickDelta = outOfCombatTickDelta;
            this.damageDealtFlatGain = damageDealtFlatGain;
            this.damageReceivedHealthEquivalentGain = damageReceivedHealthEquivalentGain;
            this.damageReceivedFlatChange = damageReceivedFlatChange;
            this.recoveryDelayAfterDamageTicks = recoveryDelayAfterDamageTicks;
            this.healingHealthEquivalentGain = healingHealthEquivalentGain;
            this.preventedDamageHealthEquivalentGain = preventedDamageHealthEquivalentGain;
            this.tauntGainPerEnemy = tauntGainPerEnemy;
            this.tauntGainCap = tauntGainCap;
            this.nearbyRecoveryBonus = nearbyRecoveryBonus;
            this.damageFreeRecoveryBonus = damageFreeRecoveryBonus;
         }
      }
   }

   private static void requireFinitePositive(double value, String name) {
      if (!Double.isFinite(value) || value <= 0.0) {
         throw new IllegalArgumentException(name + " must be finite and positive");
      }
   }

   private static void requireFiniteNonNegative(double value, String name) {
      if (!Double.isFinite(value) || value < 0.0) {
         throw new IllegalArgumentException(name + " must be finite and non-negative");
      }
   }

   private static void requireFinite(double value, String name) {
      if (!Double.isFinite(value)) {
         throw new IllegalArgumentException(name + " must be finite");
      }
   }

   public record DamageFreeRecoveryBonus(long requiredTicks, double multiplier) {
      public static final ClassResourceDefinition.DamageFreeRecoveryBonus NONE = new ClassResourceDefinition.DamageFreeRecoveryBonus(0L, 1.0);

      public DamageFreeRecoveryBonus(long requiredTicks, double multiplier) {
         if (requiredTicks < 0L) {
            throw new IllegalArgumentException("requiredTicks must not be negative");
         } else {
            ClassResourceDefinition.requireFinitePositive(multiplier, "multiplier");
            this.requiredTicks = requiredTicks;
            this.multiplier = multiplier;
         }
      }

      public double multiplierAfter(long ticksWithoutDamage) {
         return ticksWithoutDamage > this.requiredTicks ? this.multiplier : 1.0;
      }
   }

   public record NearbyRecoveryBonus(ClassResourceDefinition.NearbyRecoveryBonus.Target target, double radius, double bonusPerEntity, int maximumEntities) {
      public static final ClassResourceDefinition.NearbyRecoveryBonus NONE = new ClassResourceDefinition.NearbyRecoveryBonus(
         ClassResourceDefinition.NearbyRecoveryBonus.Target.ELITES, 0.0, 0.0, 0
      );

      public NearbyRecoveryBonus(ClassResourceDefinition.NearbyRecoveryBonus.Target target, double radius, double bonusPerEntity, int maximumEntities) {
         target = Objects.requireNonNull(target, "target");
         ClassResourceDefinition.requireFiniteNonNegative(radius, "radius");
         ClassResourceDefinition.requireFiniteNonNegative(bonusPerEntity, "bonusPerEntity");
         if (maximumEntities < 0) {
            throw new IllegalArgumentException("maximumEntities must not be negative");
         } else {
            if (maximumEntities > 0) {
               ClassResourceDefinition.requireFinitePositive(radius, "radius");
               ClassResourceDefinition.requireFinitePositive(bonusPerEntity, "bonusPerEntity");
            }

            this.target = target;
            this.radius = radius;
            this.bonusPerEntity = bonusPerEntity;
            this.maximumEntities = maximumEntities;
         }
      }

      public double multiplier(int nearbyEntities) {
         return 1.0 + Math.min(this.maximumEntities, Math.max(0, nearbyEntities)) * this.bonusPerEntity;
      }

      public static enum Target {
         ELITES,
         OTHER_PLAYERS;
      }
   }
}
