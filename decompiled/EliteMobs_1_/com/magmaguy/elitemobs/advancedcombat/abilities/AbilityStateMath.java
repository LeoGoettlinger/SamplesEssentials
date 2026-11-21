package com.magmaguy.elitemobs.advancedcombat.abilities;

final class AbilityStateMath {
   private AbilityStateMath() {
   }

   static double deathGuardDamageCeiling(double health, double absorption) {
      return Double.isFinite(health) && Double.isFinite(absorption) ? Math.max(0.0, Math.max(0.0, health) + Math.max(0.0, absorption) - 1.0) : 0.0;
   }

   static double retaliationHealing(double storedDamage, double healingFraction) {
      return Double.isFinite(storedDamage) && Double.isFinite(healingFraction) ? Math.max(0.0, storedDamage) * Math.max(0.0, healingFraction) : 0.0;
   }

   static double healthScaledMark(double baseMultiplier, double targetHealthFraction) {
      if (Double.isFinite(baseMultiplier) && Double.isFinite(targetHealthFraction)) {
         double baseBonus = Math.max(0.0, baseMultiplier - 1.0);
         double missing = 1.0 - Math.max(0.0, Math.min(1.0, targetHealthFraction));
         return 1.0 + baseBonus * (1.0 + missing * 1.5);
      } else {
         return 1.0;
      }
   }

   static boolean breaksOwnedBarrier(double incomingDamage, double ownedAbsorptionRemaining) {
      return Double.isFinite(incomingDamage)
         && Double.isFinite(ownedAbsorptionRemaining)
         && ownedAbsorptionRemaining > 0.0
         && incomingDamage >= ownedAbsorptionRemaining;
   }

   static boolean isMajorThreat(double incomingDamage, double health, double maximumHealth) {
      return Double.isFinite(incomingDamage) && Double.isFinite(health) && Double.isFinite(maximumHealth) && !(maximumHealth <= 0.0)
         ? incomingDamage >= maximumHealth * 0.15 || incomingDamage >= health
         : false;
   }
}
