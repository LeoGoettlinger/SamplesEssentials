package com.magmaguy.elitemobs.advancedcombat.passives;

public record PassiveConditionContext(
   double playerHealthFraction,
   boolean moving,
   boolean recentlyHit,
   boolean grouped,
   boolean targetPresent,
   double targetHealthFraction,
   boolean targetBoss,
   boolean targetControlled,
   boolean targetIsolated,
   boolean targetGrouped,
   double targetDistance,
   boolean criticalHit,
   boolean rangedAttack,
   boolean classAbilityDamage,
   boolean nonSummonClassAbilityDamage,
   boolean areaClassAbilityDamage,
   boolean trapClassAbilityDamage,
   boolean blastClassAbilityDamage,
   boolean magicWeaponDamage,
   boolean recentEliteKill,
   boolean wardBroken,
   boolean drawingRangedWeapon
) {
   public PassiveConditionContext(
      double playerHealthFraction,
      boolean moving,
      boolean recentlyHit,
      boolean grouped,
      boolean targetPresent,
      double targetHealthFraction,
      boolean targetBoss,
      boolean targetControlled,
      boolean targetIsolated,
      boolean targetGrouped,
      double targetDistance,
      boolean criticalHit,
      boolean rangedAttack,
      boolean classAbilityDamage,
      boolean nonSummonClassAbilityDamage,
      boolean areaClassAbilityDamage,
      boolean trapClassAbilityDamage,
      boolean blastClassAbilityDamage,
      boolean magicWeaponDamage,
      boolean recentEliteKill,
      boolean wardBroken,
      boolean drawingRangedWeapon
   ) {
      playerHealthFraction = fraction(playerHealthFraction);
      targetHealthFraction = fraction(targetHealthFraction);
      if (!Double.isFinite(targetDistance) || targetDistance < 0.0) {
         targetDistance = Double.POSITIVE_INFINITY;
      }

      this.playerHealthFraction = playerHealthFraction;
      this.moving = moving;
      this.recentlyHit = recentlyHit;
      this.grouped = grouped;
      this.targetPresent = targetPresent;
      this.targetHealthFraction = targetHealthFraction;
      this.targetBoss = targetBoss;
      this.targetControlled = targetControlled;
      this.targetIsolated = targetIsolated;
      this.targetGrouped = targetGrouped;
      this.targetDistance = targetDistance;
      this.criticalHit = criticalHit;
      this.rangedAttack = rangedAttack;
      this.classAbilityDamage = classAbilityDamage;
      this.nonSummonClassAbilityDamage = nonSummonClassAbilityDamage;
      this.areaClassAbilityDamage = areaClassAbilityDamage;
      this.trapClassAbilityDamage = trapClassAbilityDamage;
      this.blastClassAbilityDamage = blastClassAbilityDamage;
      this.magicWeaponDamage = magicWeaponDamage;
      this.recentEliteKill = recentEliteKill;
      this.wardBroken = wardBroken;
      this.drawingRangedWeapon = drawingRangedWeapon;
   }

   public PassiveConditionContext(
      double playerHealthFraction,
      boolean moving,
      boolean recentlyHit,
      boolean grouped,
      boolean targetPresent,
      double targetHealthFraction,
      boolean targetBoss,
      boolean targetControlled,
      boolean targetIsolated,
      boolean targetGrouped,
      double targetDistance,
      boolean criticalHit,
      boolean rangedAttack,
      boolean classAbilityDamage
   ) {
      this(
         playerHealthFraction,
         moving,
         recentlyHit,
         grouped,
         targetPresent,
         targetHealthFraction,
         targetBoss,
         targetControlled,
         targetIsolated,
         targetGrouped,
         targetDistance,
         criticalHit,
         rangedAttack,
         classAbilityDamage,
         classAbilityDamage,
         false,
         false,
         false,
         false,
         false,
         false,
         false
      );
   }

   public PassiveConditionContext(
      double playerHealthFraction,
      boolean moving,
      boolean recentlyHit,
      boolean grouped,
      boolean targetPresent,
      double targetHealthFraction,
      boolean targetBoss,
      boolean targetControlled,
      boolean targetIsolated,
      boolean targetGrouped,
      double targetDistance,
      boolean criticalHit,
      boolean rangedAttack,
      boolean classAbilityDamage,
      boolean magicWeaponDamage,
      boolean recentEliteKill,
      boolean wardBroken
   ) {
      this(
         playerHealthFraction,
         moving,
         recentlyHit,
         grouped,
         targetPresent,
         targetHealthFraction,
         targetBoss,
         targetControlled,
         targetIsolated,
         targetGrouped,
         targetDistance,
         criticalHit,
         rangedAttack,
         classAbilityDamage,
         classAbilityDamage,
         false,
         false,
         false,
         magicWeaponDamage,
         recentEliteKill,
         wardBroken,
         false
      );
   }

   public static PassiveConditionContext playerOnly(double playerHealthFraction, boolean moving, boolean recentlyHit, boolean grouped) {
      return playerOnly(playerHealthFraction, moving, recentlyHit, grouped, false, false);
   }

   public static PassiveConditionContext playerOnly(
      double playerHealthFraction, boolean moving, boolean recentlyHit, boolean grouped, boolean recentEliteKill, boolean wardBroken
   ) {
      return playerOnly(playerHealthFraction, moving, recentlyHit, grouped, recentEliteKill, wardBroken, false);
   }

   public static PassiveConditionContext playerOnly(
      double playerHealthFraction,
      boolean moving,
      boolean recentlyHit,
      boolean grouped,
      boolean recentEliteKill,
      boolean wardBroken,
      boolean drawingRangedWeapon
   ) {
      return new PassiveConditionContext(
         playerHealthFraction,
         moving,
         recentlyHit,
         grouped,
         false,
         1.0,
         false,
         false,
         false,
         false,
         Double.POSITIVE_INFINITY,
         false,
         false,
         false,
         false,
         false,
         false,
         false,
         false,
         recentEliteKill,
         wardBroken,
         drawingRangedWeapon
      );
   }

   private static double fraction(double value) {
      return !Double.isFinite(value) ? 1.0 : Math.max(0.0, Math.min(1.0, value));
   }
}
