package com.magmaguy.elitemobs.combatsystem;

import org.bukkit.Bukkit;

public class LevelScaling {
   public static final double SCALING_BASE = 2.0;
   public static final double LEVELS_PER_POWER_DOUBLE = 5.0;
   public static final double DEFAULT_MINECRAFT_MAX_HEALTH = 2048.0;
   public static final double BASE_MOB_HP = 2.1875;
   public static final double TARGET_HITS_TO_KILL_PLAYER = 5.0;
   public static final double SKILL_SCALING_RATE = 7.5;
   public static final double TARGET_HITS_TO_KILL_MOB = 3.0;
   public static final double REFERENCE_ATTACK_SPEED = 1.6;
   public static final double OFFENSIVE_SKILL_SCALING_RATE = 7.5;

   public static double getMinecraftMaxHealth() {
      try {
         double configuredMaxHealth = Bukkit.getServer().spigot().getConfig().getDouble("settings.attribute.maxHealth.max", 2048.0);
         if (Double.isFinite(configuredMaxHealth) && configuredMaxHealth > 0.0) {
            return configuredMaxHealth;
         }
      } catch (Exception var2) {
      }

      return 2048.0;
   }

   private LevelScaling() {
   }

   public static double calculateMobHealth(int level, double baseEntityHP) {
      double health = 2.1875 * Math.pow(2.0, level / 5.0);
      return Math.min(health, getMinecraftMaxHealth());
   }

   public static double calculateBaseDamageToElite(int mobLevel) {
      return calculateMobHealth(mobLevel, 0.0) / 3.0;
   }

   public static double calculateOffensiveSkillAdjustment(int weaponSkillLevel, int mobLevel) {
      return Math.pow(2.0, (weaponSkillLevel - mobLevel) / 7.5);
   }
}
