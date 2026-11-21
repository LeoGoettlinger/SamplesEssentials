package com.magmaguy.elitemobs.config.skillbonuses;

import com.magmaguy.elitemobs.magmacore.config.CustomConfig;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import lombok.Generated;

public class SkillBonusesConfig extends CustomConfig {
   private static HashMap<String, SkillBonusConfigFields> skillBonuses = new HashMap<>();

   public SkillBonusesConfig() {
      super("skillbonuses", "com.magmaguy.elitemobs.config.skillbonuses.premade", SkillBonusConfigFields.class);
      skillBonuses = new HashMap<>();

      for (String key : super.getCustomConfigFieldsHashMap().keySet()) {
         skillBonuses.put(key.toLowerCase(Locale.ROOT), (SkillBonusConfigFields)super.getCustomConfigFieldsHashMap().get(key));
      }

      Logger.info("Loaded " + skillBonuses.size() + " skill bonus configurations.");
   }

   public static SkillBonusConfigFields getSkillBonus(String filename) {
      String key = filename.toLowerCase(Locale.ROOT);
      if (!key.endsWith(".yml")) {
         key = key + ".yml";
      }

      return skillBonuses.get(key);
   }

   public static SkillBonusConfigFields getBySkillId(String skillId) {
      return getSkillBonus(skillId + ".yml");
   }

   public static List<SkillBonusConfigFields> getBySkillType(SkillType skillType) {
      List<SkillBonusConfigFields> result = new ArrayList<>();

      for (SkillBonusConfigFields config : skillBonuses.values()) {
         if (config.getSkillType() == skillType) {
            result.add(config);
         }
      }

      return result;
   }

   public static List<SkillBonusConfigFields> getEnabledBySkillType(SkillType skillType) {
      List<SkillBonusConfigFields> result = new ArrayList<>();

      for (SkillBonusConfigFields config : skillBonuses.values()) {
         if (config.getSkillType() == skillType && config.isEnabled()) {
            result.add(config);
         }
      }

      return result;
   }

   public static List<SkillBonusConfigFields> getByBonusType(SkillBonusType bonusType) {
      List<SkillBonusConfigFields> result = new ArrayList<>();

      for (SkillBonusConfigFields config : skillBonuses.values()) {
         if (config.getBonusType() == bonusType) {
            result.add(config);
         }
      }

      return result;
   }

   public static List<SkillBonusConfigFields> getByTier(SkillType skillType, int unlockTier) {
      List<SkillBonusConfigFields> result = new ArrayList<>();

      for (SkillBonusConfigFields config : skillBonuses.values()) {
         if (config.getSkillType() == skillType && config.getUnlockTier() == unlockTier) {
            result.add(config);
         }
      }

      return result;
   }

   public static List<SkillBonusConfigFields> getAllEnabled() {
      List<SkillBonusConfigFields> result = new ArrayList<>();

      for (SkillBonusConfigFields config : skillBonuses.values()) {
         if (config.isEnabled()) {
            result.add(config);
         }
      }

      return result;
   }

   public static int getSkillCount() {
      return skillBonuses.size();
   }

   public static int getEnabledSkillCount() {
      int count = 0;

      for (SkillBonusConfigFields config : skillBonuses.values()) {
         if (config.isEnabled()) {
            count++;
         }
      }

      return count;
   }

   public static void shutdown() {
      skillBonuses.clear();
   }

   @Generated
   public static HashMap<String, SkillBonusConfigFields> getSkillBonuses() {
      return skillBonuses;
   }
}
