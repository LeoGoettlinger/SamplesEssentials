package com.magmaguy.elitemobs.combatsystem;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public final class ClassAbilityDamageAttribution {
   private final Set<String> sources = new LinkedHashSet<>();
   private double damageWithoutAbilities = Double.NaN;

   public double applyMultiplier(String abilityId, double currentDamage, double multiplier) {
      Objects.requireNonNull(abilityId, "abilityId");
      if (!abilityId.isBlank() && Double.isFinite(currentDamage) && !(currentDamage < 0.0) && Double.isFinite(multiplier) && !(multiplier <= 1.0)) {
         double modifiedDamage = currentDamage * multiplier;
         if (!Double.isFinite(modifiedDamage)) {
            return currentDamage;
         } else {
            if (!this.isTracking()) {
               this.damageWithoutAbilities = currentDamage;
            }

            this.sources.add(abilityId);
            return modifiedDamage;
         }
      } else {
         return currentDamage;
      }
   }

   public void observeReplacement(double previousDamage, double replacementDamage) {
      if (this.isTracking()) {
         if (!Double.isFinite(previousDamage) || !Double.isFinite(replacementDamage)) {
            this.clear();
         } else if (!(previousDamage <= 0.0) && !(replacementDamage <= 0.0)) {
            this.damageWithoutAbilities = Math.max(0.0, this.damageWithoutAbilities * replacementDamage / previousDamage);
         } else {
            this.damageWithoutAbilities = Math.max(0.0, replacementDamage);
         }
      }
   }

   public double bonusDamage(double finalDamage) {
      return this.isTracking() && Double.isFinite(finalDamage) && !(finalDamage <= 0.0) ? Math.max(0.0, finalDamage - this.damageWithoutAbilities) : 0.0;
   }

   public Set<String> sources() {
      return Set.copyOf(this.sources);
   }

   private boolean isTracking() {
      return Double.isFinite(this.damageWithoutAbilities);
   }

   private void clear() {
      this.damageWithoutAbilities = Double.NaN;
      this.sources.clear();
   }
}
