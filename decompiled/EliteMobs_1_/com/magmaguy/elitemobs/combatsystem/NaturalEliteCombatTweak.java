package com.magmaguy.elitemobs.combatsystem;

import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;

public class NaturalEliteCombatTweak {
   public static final double LEGACY_DAMAGE_PER_LEVEL = 2.0;

   private NaturalEliteCombatTweak() {
   }

   public static boolean shouldApply(EliteEntity eliteEntity) {
      return eliteEntity != null && eliteEntity.isNaturalEntity() && !MobCombatSettingsConfig.isUseRecommendedHealthScaling();
   }

   public static double getTweakedMobHealth(EliteEntity eliteEntity, int level, double recommendedHealth) {
      return !shouldApply(eliteEntity) ? recommendedHealth : getLegacyMobHealth(level);
   }

   public static double getTweakedBaseDamageToElite(EliteEntity eliteEntity, int level) {
      return !shouldApply(eliteEntity) ? LevelScaling.calculateBaseDamageToElite(level) : getLegacyBaseDamage(level);
   }

   public static double getTweakedMobHealthForLevel(EliteEntity eliteEntity, int level, double healthMultiplier) {
      double baseHealth = shouldApply(eliteEntity) ? getLegacyMobHealth(level) : LevelScaling.calculateMobHealth(level, 0.0);
      return baseHealth * healthMultiplier;
   }

   private static double getLegacyBaseDamage(int level) {
      return Math.max(1, level) * 2.0;
   }

   private static double getLegacyMobHealth(int level) {
      return getLegacyBaseDamage(level) * 3.0;
   }
}
