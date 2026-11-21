package com.magmaguy.elitemobs.combatsystem;

import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PotionCombatModifierCalculator {
   private PotionCombatModifierCalculator() {
   }

   private static int getEffectLevel(LivingEntity entity, PotionEffectType effectType) {
      if (entity != null && entity.hasPotionEffect(effectType)) {
         PotionEffect potionEffect = entity.getPotionEffect(effectType);
         return potionEffect == null ? 0 : potionEffect.getAmplifier() + 1;
      } else {
         return 0;
      }
   }

   public static double getIncomingDamageMultiplier(LivingEntity entity) {
      return getIncomingDamageMultiplier(getEffectLevel(entity, PotionEffectType.RESISTANCE));
   }

   public static double getOutgoingDamageMultiplier(LivingEntity entity) {
      return getOutgoingDamageMultiplier(getEffectLevel(entity, PotionEffectType.STRENGTH), getEffectLevel(entity, PotionEffectType.WEAKNESS));
   }

   public static double getCombinedDamageMultiplier(LivingEntity source, LivingEntity target) {
      return getCombinedDamageMultiplier(
         getEffectLevel(source, PotionEffectType.STRENGTH),
         getEffectLevel(source, PotionEffectType.WEAKNESS),
         getEffectLevel(target, PotionEffectType.RESISTANCE)
      );
   }

   static double getIncomingDamageMultiplier(int resistanceLevel) {
      return Math.max(0.0, 1.0 - resistanceLevel * MobCombatSettingsConfig.getResistanceDamageMultiplier());
   }

   static double getOutgoingDamageMultiplier(int strengthLevel, int weaknessLevel) {
      double strengthBonus = strengthLevel * MobCombatSettingsConfig.getStrengthDamageMultiplier();
      double weaknessPenalty = weaknessLevel * MobCombatSettingsConfig.getWeaknessDamageMultiplier();
      return Math.max(0.0, 1.0 + strengthBonus - weaknessPenalty);
   }

   static double getCombinedDamageMultiplier(int sourceStrengthLevel, int sourceWeaknessLevel, int targetResistanceLevel) {
      return getOutgoingDamageMultiplier(sourceStrengthLevel, sourceWeaknessLevel) * getIncomingDamageMultiplier(targetResistanceLevel);
   }
}
