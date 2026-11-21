package com.magmaguy.elitemobs.advancedcombat.damage;

import com.magmaguy.elitemobs.advancedcombat.abilities.AbilityLevelScaling;
import com.magmaguy.elitemobs.combatsystem.LevelScaling;
import com.magmaguy.elitemobs.combatsystem.NaturalEliteCombatTweak;
import com.magmaguy.elitemobs.combatsystem.WeaponOffenseCalculator;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import org.bukkit.Material;

public final class AdvancedDamageScaling {
   private AdvancedDamageScaling() {
   }

   public static double classAbility(EliteEntity target, int effectiveClassLevel, double abilityMultiplier) {
      double baseline = classAbilityUnrankedBaseline(target, effectiveClassLevel);
      return scaleClassAbilityBaseline(baseline, effectiveClassLevel, abilityMultiplier);
   }

   public static double classAbilityUnrankedBaseline(EliteEntity target, int effectiveClassLevel) {
      return calculate(target, effectiveClassLevel, 1.0, 1.0, null);
   }

   public static double magicWeapon(EliteEntity target, int skillLevel, double itemLevel, double attackMultiplier, Material carrierMaterial) {
      if (target != null && target.isValid()) {
         int formulaLevel = formulaLevel(target, skillLevel);
         double itemAdjustment = WeaponOffenseCalculator.getWeaponAdjustment(itemLevel, formulaLevel);
         return calculate(target, skillLevel, itemAdjustment, attackMultiplier, carrierMaterial);
      } else {
         return 0.0;
      }
   }

   private static double calculate(EliteEntity target, int progressionLevel, double equipmentAdjustment, double outputMultiplier, Material carrierMaterial) {
      if (target != null
         && target.isValid()
         && Double.isFinite(equipmentAdjustment)
         && !(equipmentAdjustment < 0.0)
         && Double.isFinite(outputMultiplier)
         && !(outputMultiplier <= 0.0)) {
         int boundedProgressionLevel = Math.max(1, progressionLevel);
         int formulaLevel = formulaLevel(target, boundedProgressionLevel);
         double baseDamage = NaturalEliteCombatTweak.getTweakedBaseDamageToElite(target, formulaLevel);
         double skillAdjustment = LevelScaling.calculateOffensiveSkillAdjustment(boundedProgressionLevel, formulaLevel);
         double scaledCombatAdjustment = scaledCombatAdjustment(target, formulaLevel);
         double bossModifier = target instanceof CustomBossEntity customBoss ? customBoss.getDamageModifier(carrierMaterial) : 1.0;
         double combatMultiplier = combatMultiplier(target);
         return combine(baseDamage, skillAdjustment, equipmentAdjustment, outputMultiplier, scaledCombatAdjustment, bossModifier, combatMultiplier);
      } else {
         return 0.0;
      }
   }

   private static int formulaLevel(EliteEntity target, int progressionLevel) {
      return target.isScaledCombat() ? Math.max(1, progressionLevel) : Math.max(1, target.getLevel());
   }

   private static double scaledCombatAdjustment(EliteEntity target, int formulaLevel) {
      if (!target.isScaledCombat()) {
         return 1.0;
      } else {
         double simulatedHealth = NaturalEliteCombatTweak.getTweakedMobHealthForLevel(target, formulaLevel, target.getHealthMultiplier());
         return simulatedHealth > 0.0 ? target.getMaxHealth() / simulatedHealth : 0.0;
      }
   }

   private static double combatMultiplier(EliteEntity target) {
      if (target.isScaledCombat()) {
         return MobCombatSettingsConfig.getScaledDamageToEliteMultiplier();
      } else {
         return target instanceof CustomBossEntity customBoss && customBoss.isNormalizedCombat()
            ? MobCombatSettingsConfig.getNormalizedDamageToEliteMultiplier()
            : MobCombatSettingsConfig.getDamageToEliteMultiplier();
      }
   }

   static double classRankScale(int effectiveClassLevel) {
      return AbilityLevelScaling.multiplier(effectiveClassLevel);
   }

   static double scaleClassAbilityBaseline(double unrankedBaseline, int effectiveClassLevel, double abilityMultiplier) {
      if (Double.isFinite(unrankedBaseline) && !(unrankedBaseline <= 0.0) && Double.isFinite(abilityMultiplier) && !(abilityMultiplier <= 0.0)) {
         double damage = unrankedBaseline * abilityMultiplier * classRankScale(effectiveClassLevel);
         return Double.isFinite(damage) && damage > 0.0 ? damage : 0.0;
      } else {
         return 0.0;
      }
   }

   static double combine(
      double baseDamage,
      double skillAdjustment,
      double equipmentAdjustment,
      double outputMultiplier,
      double scaledCombatAdjustment,
      double bossModifier,
      double combatMultiplier
   ) {
      double[] factors = new double[]{
         baseDamage, skillAdjustment, equipmentAdjustment, outputMultiplier, scaledCombatAdjustment, bossModifier, combatMultiplier
      };

      for (double factor : factors) {
         if (!Double.isFinite(factor) || factor < 0.0) {
            return 0.0;
         }
      }

      double damage = 1.0;

      for (double factorx : factors) {
         damage *= factorx;
      }

      return Double.isFinite(damage) && damage > 0.0 ? damage : 0.0;
   }
}
