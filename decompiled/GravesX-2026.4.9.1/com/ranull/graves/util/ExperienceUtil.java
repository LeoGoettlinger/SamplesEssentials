package com.ranull.graves.util;

import org.bukkit.entity.Player;

public class ExperienceUtil {
   private ExperienceUtil() {
   }

   public static int getPlayerExperience(Player player) {
      int experience = Math.round(getExperienceAtLevel(player.getLevel()) * player.getExp());
      int level = player.getLevel();

      while (level > 0) {
         experience += getExperienceAtLevel(--level);
      }

      return experience < 0 ? -1 : experience;
   }

   public static int getExperienceAtLevel(int level) {
      if (level <= 15) {
         return 2 * level + 7;
      } else {
         return level <= 30 ? 5 * level - 38 : 9 * level - 158;
      }
   }

   public static long getLevelFromExperience(long experience) {
      double result = 0.0;
      if (experience > 1395L) {
         result = (Math.sqrt(72L * experience - 54215L) + 325.0) / 18.0;
      } else if (experience > 315L) {
         result = Math.sqrt(40L * experience - 7839L) / 10.0 + 8.1;
      } else if (experience > 0L) {
         result = Math.sqrt(experience + 9L) - 3.0;
      }

      return (long)(Math.round(result * 100.0) / 100.0);
   }

   public static int getDropPercent(int experience, float percent) {
      if (experience <= 0) {
         return 0;
      } else {
         float clamped = Math.max(0.0F, Math.min(1.0F, percent));
         return (int)(experience * clamped);
      }
   }

   @Deprecated
   public static int getPlayerDropExperience(Player player, float expStorePercent) {
      int experience = getPlayerExperience(player);
      return getDropPercent(experience, expStorePercent);
   }
}
