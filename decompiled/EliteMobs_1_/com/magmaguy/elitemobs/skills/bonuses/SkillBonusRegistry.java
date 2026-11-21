package com.magmaguy.elitemobs.skills.bonuses;

import com.magmaguy.elitemobs.config.SkillsConfig;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.SkillXPCalculator;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ProcCooldownTracker;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;

public class SkillBonusRegistry {
   private static final Map<SkillType, List<SkillBonus>> bonusesBySkill = new EnumMap<>(SkillType.class);
   private static final Map<String, SkillBonus> bonusesById = new ConcurrentHashMap<>();

   private SkillBonusRegistry() {
   }

   public static void registerBonus(SkillBonus bonus) {
      bonusesBySkill.computeIfAbsent(bonus.getSkillType(), k -> new ArrayList<>()).add(bonus);
      bonusesById.put(bonus.getSkillId(), bonus);
   }

   public static void unregisterBonus(SkillBonus bonus) {
      List<SkillBonus> bonuses = bonusesBySkill.get(bonus.getSkillType());
      if (bonuses != null) {
         bonuses.remove(bonus);
      }

      bonusesById.remove(bonus.getSkillId(), bonus);
   }

   public static SkillBonus getSkillById(String skillId) {
      return bonusesById.get(skillId);
   }

   public static List<SkillBonus> getBonuses(SkillType skillType) {
      return bonusesBySkill.getOrDefault(skillType, Collections.emptyList());
   }

   public static List<SkillBonus> getEnabledBonuses(SkillType skillType) {
      return getBonuses(skillType).stream().filter(SkillBonus::isEnabled).toList();
   }

   public static List<SkillBonus> getAllBonuses() {
      List<SkillBonus> all = new ArrayList<>();

      for (List<SkillBonus> bonusList : bonusesBySkill.values()) {
         all.addAll(bonusList);
      }

      return all;
   }

   public static List<SkillBonus> getAllEnabledBonuses() {
      return getAllBonuses().stream().filter(SkillBonus::isEnabled).toList();
   }

   public static List<SkillBonus> getBonusesByTier(SkillType skillType, int unlockTier) {
      return getBonuses(skillType).stream().filter(bonus -> bonus.getUnlockTier() == unlockTier).toList();
   }

   public static List<SkillBonus> getUnlockableBonuses(SkillType skillType, int skillLevel) {
      return getEnabledBonuses(skillType).stream().filter(bonus -> bonus.canUnlock(skillLevel)).toList();
   }

   public static List<SkillBonus> getBonusesSortedByTier(SkillType skillType) {
      return getBonuses(skillType).stream().sorted(Comparator.comparingInt(SkillBonus::getUnlockTier)).toList();
   }

   public static void applyBonuses(Player player, SkillType skillType) {
      if (SkillsConfig.isWorldExcludedFromSkills(player)) {
         removeAllBonuses(player);
      } else {
         int level = getPlayerSkillLevel(player, skillType);

         for (String skillId : PlayerSkillSelection.getActiveSkills(player.getUniqueId(), skillType)) {
            SkillBonus bonus = getSkillById(skillId);
            if (bonus != null && bonus.isEnabled() && bonus.meetsLevelRequirement(level)) {
               bonus.applyBonus(player, level);
            }
         }
      }
   }

   public static void applyAllBonuses(Player player) {
      if (SkillsConfig.isWorldExcludedFromSkills(player)) {
         removeAllBonuses(player);
      } else {
         for (SkillType skillType : SkillType.values()) {
            applyBonuses(player, skillType);
         }
      }
   }

   public static void removeAllBonuses(Player player) {
      ProcCooldownTracker.clear(player);

      for (SkillBonus bonus : getAllBonuses()) {
         bonus.resetProcCount(player);
         if (bonus.isActive(player)) {
            bonus.removeBonus(player);
         }
      }
   }

   public static Map<String, Double> getTotalBonuses(Player player, SkillType skillType) {
      if (SkillsConfig.isWorldExcludedFromSkills(player)) {
         return Collections.emptyMap();
      } else {
         Map<String, Double> totals = new LinkedHashMap<>();
         int level = getPlayerSkillLevel(player, skillType);

         for (String skillId : PlayerSkillSelection.getActiveSkills(player.getUniqueId(), skillType)) {
            SkillBonus bonus = getSkillById(skillId);
            if (bonus != null && bonus.isEnabled() && bonus.meetsLevelRequirement(level)) {
               totals.put(bonus.getBonusName(), bonus.getBonusValue(level));
            }
         }

         return totals;
      }
   }

   public static List<String> getFormattedBonuses(Player player, SkillType skillType) {
      if (SkillsConfig.isWorldExcludedFromSkills(player)) {
         return Collections.emptyList();
      } else {
         List<String> formatted = new ArrayList<>();
         int level = getPlayerSkillLevel(player, skillType);

         for (String skillId : PlayerSkillSelection.getActiveSkills(player.getUniqueId(), skillType)) {
            SkillBonus bonus = getSkillById(skillId);
            if (bonus != null && bonus.isEnabled() && bonus.meetsLevelRequirement(level)) {
               formatted.add(bonus.getFormattedBonus(level));
            }
         }

         return formatted;
      }
   }

   public static int getSkillCount(SkillType skillType) {
      return getBonuses(skillType).size();
   }

   public static int getEnabledSkillCount(SkillType skillType) {
      return getEnabledBonuses(skillType).size();
   }

   public static int getPlayerSkillLevel(Player player, SkillType skillType) {
      long xp = PlayerData.getSkillXP(player.getUniqueId(), skillType);
      return SkillXPCalculator.levelFromTotalXP(xp);
   }

   public static void shutdown() {
      ProcCooldownTracker.clearAll();

      for (SkillBonus bonus : getAllBonuses()) {
         bonus.shutdown();
      }

      bonusesBySkill.clear();
      bonusesById.clear();
   }
}
