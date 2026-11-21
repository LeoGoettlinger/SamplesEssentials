package com.magmaguy.elitemobs.skills;

public class SkillXPCalculator {
   private static final int MAX_REPRESENTABLE_LEVEL = levelFromTotalXP(Long.MAX_VALUE);

   private SkillXPCalculator() {
   }

   public static long calculateMobXP(int mobLevel) {
      return mobLevel <= 0 ? 0L : (long)mobLevel * mobLevel;
   }

   public static long xpToNextLevel(int currentLevel) {
      if (currentLevel < 1) {
         currentLevel = 1;
      }

      long baseXP = (long)(8.5 * currentLevel * currentLevel * currentLevel);
      if (currentLevel <= 100) {
         return baseXP;
      } else {
         double softCapMultiplier = 1.0 + Math.pow(currentLevel - 100, 2.0) / 100.0;
         return (long)(baseXP * softCapMultiplier);
      }
   }

   public static long totalXPForLevel(int targetLevel) {
      if (targetLevel <= 1) {
         return 0L;
      } else if (targetLevel > MAX_REPRESENTABLE_LEVEL) {
         throw new IllegalArgumentException("Skill level must be at most " + MAX_REPRESENTABLE_LEVEL + " for the supported XP range.");
      } else {
         long total = 0L;

         for (int level = 1; level < targetLevel; level++) {
            total = Math.addExact(total, xpToNextLevel(level));
         }

         return total;
      }
   }

   public static int levelFromTotalXP(long totalXP) {
      if (totalXP <= 0L) {
         return 1;
      } else {
         int level = 1;
         long accumulatedXP = 0L;

         for (long requiredXP = xpToNextLevel(level); requiredXP <= totalXP - accumulatedXP; requiredXP = xpToNextLevel(++level)) {
            accumulatedXP += requiredXP;
         }

         return level;
      }
   }

   public static long xpProgressInCurrentLevel(long totalXP) {
      if (totalXP <= 0L) {
         return 0L;
      } else {
         int currentLevel = levelFromTotalXP(totalXP);
         long xpForCurrentLevel = totalXPForLevel(currentLevel);
         return totalXP - xpForCurrentLevel;
      }
   }

   public static double levelProgress(long totalXP) {
      int currentLevel = levelFromTotalXP(totalXP);
      long progressXP = xpProgressInCurrentLevel(totalXP);
      long requiredXP = xpToNextLevel(currentLevel);
      return requiredXP == 0L ? 0.0 : (double)progressXP / requiredXP;
   }

   public static long killsToNextLevel(int currentLevel, int mobLevel) {
      long xpPerKill = calculateMobXP(mobLevel);
      if (xpPerKill == 0L) {
         return Long.MAX_VALUE;
      } else {
         long xpNeeded = xpToNextLevel(currentLevel);
         return (xpNeeded + xpPerKill - 1L) / xpPerKill;
      }
   }

   public static long calculateArmorXP(long baseXP) {
      return baseXP / 3L;
   }

   public static long applySkillMultiplier(SkillType skillType, long baseXP) {
      return (long)(baseXP * skillType.getXpMultiplier());
   }

   public static double calculateBossXPMultiplier(double healthMultiplier, double damageMultiplier) {
      double hpComponent = Math.pow(Math.max(healthMultiplier, 0.01), 0.6);
      double dmgComponent = 1.0 + (damageMultiplier - 1.0) * 0.15;
      if (dmgComponent < 0.1) {
         dmgComponent = 0.1;
      }

      return hpComponent * dmgComponent;
   }
}
