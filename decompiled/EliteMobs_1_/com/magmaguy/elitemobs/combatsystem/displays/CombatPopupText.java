package com.magmaguy.elitemobs.combatsystem.displays;

import com.magmaguy.elitemobs.advancedcombat.presentation.ClassPresentationTheme;

final class CombatPopupText {
   private CombatPopupText() {
   }

   static String damageAmount(boolean scaledCombat, double maximumHealth, double totalDamage, double classAbilityBonusDamage) {
      String total = amount(scaledCombat, maximumHealth, totalDamage);
      if (Double.isFinite(classAbilityBonusDamage) && !(classAbilityBonusDamage <= 1.0E-6)) {
         String bonus = amount(scaledCombat, maximumHealth, classAbilityBonusDamage);
         return total + " &8[" + ClassPresentationTheme.abilityDamage("+" + bonus) + "&8]";
      } else {
         return total;
      }
   }

   private static String amount(boolean scaledCombat, double maximumHealth, double damage) {
      return scaledCombat ? DisplayTextFormatter.percentage(damage / Math.max(1.0, maximumHealth)) : DisplayTextFormatter.number(damage);
   }
}
