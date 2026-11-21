package com.magmaguy.elitemobs.skills.bonuses;

import com.magmaguy.elitemobs.config.DungeonsConfig;
import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.magmacore.util.ChatColorConverter;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.presentation.actionbar.ActionBarCompositor;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.testing.SkillSystemTest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Generated;
import org.bukkit.entity.Player;

public abstract class SkillBonus {
   private static final Pattern UNRESOLVED_TEMPLATE_TOKEN = Pattern.compile("\\$[A-Za-z][A-Za-z0-9_]*");
   public static final double MAX_DEFENSIVE_REDUCTION = 0.8;
   protected final SkillType skillType;
   protected int requiredLevel;
   protected String bonusName;
   protected String description;
   protected final SkillBonusType bonusType;
   protected int unlockTier;
   protected final String skillId;
   protected boolean enabled = true;
   protected SkillBonusConfigFields configFields;
   private final Map<UUID, Integer> procCounts = new ConcurrentHashMap<>();

   public static double clampDefensiveReduction(double reduction) {
      return Math.max(0.0, Math.min(0.8, reduction));
   }

   protected static double scaled(double base, double perLevel, int level) {
      return base + level * perLevel;
   }

   protected static double scaled(double base, double perLevel, double cap, int level) {
      return Math.min(cap, base + level * perLevel);
   }

   public static void sendSkillActionBar(Player player, SkillBonus skill) {
      ActionBarCompositor.show(
         player,
         ActionBarCompositor.Source.SKILL_FEEDBACK,
         ChatColorConverter.convert(DungeonsConfig.getSkillActivationFormat().replace("$skillName", skill.getBonusName()))
      );
   }

   public void incrementProcCount(Player player) {
      if (SkillSystemTest.hasActiveSession(player.getUniqueId())) {
         this.procCounts.merge(player.getUniqueId(), 1, Integer::sum);
      }
   }

   public int getProcCount(Player player) {
      return this.procCounts.getOrDefault(player.getUniqueId(), 0);
   }

   public void resetProcCount(Player player) {
      this.procCounts.remove(player.getUniqueId());
   }

   public void resetAllProcCounts() {
      this.procCounts.clear();
   }

   protected SkillBonus(SkillType skillType, int requiredLevel, String bonusName, String description, SkillBonusType bonusType, int unlockTier, String skillId) {
      this.skillType = skillType;
      this.requiredLevel = requiredLevel;
      this.bonusName = bonusName;
      this.description = description;
      this.bonusType = bonusType;
      this.unlockTier = unlockTier;
      this.skillId = skillId;
   }

   protected SkillBonus(SkillBonusConfigFields config) {
      this.configFields = config;
      this.skillType = config.getSkillType();
      this.requiredLevel = config.getRequiredLevel();
      this.bonusName = config.getName();
      this.description = String.join(" ", config.getDescription());
      this.bonusType = config.getBonusType();
      this.unlockTier = config.getUnlockTier();
      this.skillId = config.getSkillId();
      this.enabled = config.isEnabled();
   }

   public void setConfigFields(SkillBonusConfigFields configFields) {
      this.configFields = configFields;
      if (configFields != null) {
         this.enabled = configFields.isEnabled();
         if (configFields.getSkillType() != this.skillType || configFields.getBonusType() != this.bonusType) {
            this.enabled = false;
            Logger.warn("Skill " + this.skillId + " has an unsupported skillType/bonusType; expected " + this.skillType + "/" + this.bonusType);
            return;
         }

         this.requiredLevel = configFields.getRequiredLevel();
         this.unlockTier = configFields.getUnlockTier();
         this.bonusName = configFields.getName();
         this.description = String.join(" ", configFields.getDescription());
         String formatted = this.getFormattedBonus(this.requiredLevel);
         if (formatted != null) {
            Matcher unresolved = UNRESOLVED_TEMPLATE_TOKEN.matcher(formatted);
            if (unresolved.find()) {
               Logger.warn("Skill " + this.skillId + " formattedBonusTemplate contains unsupported token " + unresolved.group());
            }
         }
      }
   }

   public static int getLevelForTier(int tier) {
      return switch (tier) {
         case 1 -> 10;
         case 2 -> 25;
         case 3 -> 50;
         case 4 -> 75;
         default -> 100;
      };
   }

   public static void sendStackingSkillActionBar(Player player, SkillBonus skill, int currentStacks, int maxStacks) {
      ActionBarCompositor.show(
         player,
         ActionBarCompositor.Source.SKILL_FEEDBACK,
         ChatColorConverter.convert(
            DungeonsConfig.getSkillStackFormat()
               .replace("$skillName", skill.getBonusName())
               .replace("$current", String.valueOf(currentStacks))
               .replace("$max", String.valueOf(maxStacks))
         )
      );
   }

   protected List<String> applyLoreTemplates(Map<String, String> replacements) {
      if (this.configFields != null && !this.configFields.getLoreTemplates().isEmpty()) {
         List<String> result = new ArrayList<>();

         for (String template : this.configFields.getLoreTemplates()) {
            String line = template;

            for (Entry<String, String> entry : replacements.entrySet()) {
               line = line.replace("$" + entry.getKey(), entry.getValue());
            }

            result.add(line);
         }

         return result;
      } else {
         return List.of();
      }
   }

   public abstract void applyBonus(Player var1, int var2);

   public abstract void removeBonus(Player var1);

   public abstract void onActivate(Player var1);

   public abstract void onDeactivate(Player var1);

   public abstract boolean isActive(Player var1);

   public abstract List<String> getLoreDescription(int var1);

   public abstract double getBonusValue(int var1);

   public abstract String getFormattedBonus(int var1);

   public boolean meetsLevelRequirement(int skillLevel) {
      return skillLevel >= this.requiredLevel;
   }

   public boolean canUnlock(int skillLevel) {
      return this.meetsLevelRequirement(skillLevel);
   }

   protected String applyFormattedBonusTemplate(Map<String, String> replacements) {
      if (this.configFields != null && !this.configFields.getFormattedBonusTemplate().isEmpty()) {
         String result = this.configFields.getFormattedBonusTemplate();

         for (Entry<String, String> entry : replacements.entrySet()) {
            result = result.replace("$" + entry.getKey(), entry.getValue());
         }

         return result;
      } else {
         return "";
      }
   }

   public SkillBonus.TestStrategy getTestStrategy() {
      return SkillBonus.TestStrategy.PROC_COUNT;
   }

   public void shutdown() {
      this.procCounts.clear();
   }

   protected double getScaledValue(Player player) {
      return this.getScaledValue(SkillBonusRegistry.getPlayerSkillLevel(player, this.skillType));
   }

   protected double getScaledValue(int skillLevel) {
      return this.configFields == null ? 1.0 : this.configFields.calculateValue(skillLevel);
   }

   protected double getScaledProcChance(Player player) {
      return this.configFields == null ? 0.1 : this.configFields.calculateProcChance(SkillBonusRegistry.getPlayerSkillLevel(player, this.skillType));
   }

   protected double getScaledCooldown(Player player) {
      return this.configFields == null ? 30.0 : this.configFields.calculateCooldown(SkillBonusRegistry.getPlayerSkillLevel(player, this.skillType));
   }

   protected double getScaledCooldown(int skillLevel) {
      return this.configFields == null ? 30.0 : this.configFields.calculateCooldown(skillLevel);
   }

   public boolean affectsDamage() {
      return true;
   }

   @Generated
   public SkillType getSkillType() {
      return this.skillType;
   }

   @Generated
   public int getRequiredLevel() {
      return this.requiredLevel;
   }

   @Generated
   public String getBonusName() {
      return this.bonusName;
   }

   @Generated
   public String getDescription() {
      return this.description;
   }

   @Generated
   public SkillBonusType getBonusType() {
      return this.bonusType;
   }

   @Generated
   public int getUnlockTier() {
      return this.unlockTier;
   }

   @Generated
   public String getSkillId() {
      return this.skillId;
   }

   @Generated
   public boolean isEnabled() {
      return this.enabled;
   }

   @Generated
   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }

   @Generated
   public SkillBonusConfigFields getConfigFields() {
      return this.configFields;
   }

   public static enum TestStrategy {
      PROC_COUNT,
      ATTRIBUTE_CHECK,
      CONDITION_SETUP;
   }
}
