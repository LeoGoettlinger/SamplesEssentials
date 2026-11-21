package com.magmaguy.elitemobs.config.skillbonuses;

import com.magmaguy.elitemobs.config.CustomConfigFields;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import lombok.Generated;
import org.bukkit.Material;

public class SkillBonusConfigFields extends CustomConfigFields {
   private String name = "Skill";
   private List<String> description = List.of("A skill bonus");
   private SkillType skillType = SkillType.SWORDS;
   private SkillBonusType bonusType = SkillBonusType.PASSIVE;
   private int unlockTier = 1;
   private double baseValue = 1.0;
   private double scalingPerLevel = 0.1;
   private double procChance = 0.0;
   private double cooldownSeconds = 0.0;
   private int maxStacks = 0;
   private double stackBonus = 0.0;
   private Material displayMaterial = Material.IRON_SWORD;
   private int customModelData = 0;
   private String skillId;
   protected List<String> loreTemplates = List.of();
   protected String formattedBonusTemplate = "";

   public SkillBonusConfigFields(String filename, boolean isEnabled) {
      super(filename, isEnabled);
      this.skillId = filename.replace(".yml", "").toLowerCase(Locale.ROOT);
   }

   public SkillBonusConfigFields(
      String filename,
      boolean isEnabled,
      String name,
      List<String> description,
      SkillType skillType,
      SkillBonusType bonusType,
      int unlockTier,
      double baseValue,
      double scalingPerLevel,
      Material displayMaterial
   ) {
      super(filename, isEnabled);
      this.name = name;
      this.description = description;
      this.skillType = skillType;
      this.bonusType = bonusType;
      this.unlockTier = unlockTier;
      this.baseValue = baseValue;
      this.scalingPerLevel = scalingPerLevel;
      this.displayMaterial = displayMaterial;
      this.skillId = filename.replace(".yml", "").toLowerCase(Locale.ROOT);
   }

   public SkillBonusConfigFields(
      String filename,
      boolean isEnabled,
      String name,
      List<String> description,
      SkillType skillType,
      int unlockTier,
      double baseValue,
      double scalingPerLevel,
      double procChance,
      Material displayMaterial
   ) {
      super(filename, isEnabled);
      this.name = name;
      this.description = description;
      this.skillType = skillType;
      this.bonusType = SkillBonusType.PROC;
      this.unlockTier = unlockTier;
      this.baseValue = baseValue;
      this.scalingPerLevel = scalingPerLevel;
      this.procChance = procChance;
      this.displayMaterial = displayMaterial;
      this.skillId = filename.replace(".yml", "").toLowerCase(Locale.ROOT);
   }

   public SkillBonusConfigFields(
      String filename,
      boolean isEnabled,
      String name,
      List<String> description,
      SkillType skillType,
      int unlockTier,
      double baseValue,
      double scalingPerLevel,
      double cooldownSeconds,
      Material displayMaterial,
      boolean isCooldownType
   ) {
      super(filename, isEnabled);
      this.name = name;
      this.description = description;
      this.skillType = skillType;
      this.bonusType = SkillBonusType.COOLDOWN;
      this.unlockTier = unlockTier;
      this.baseValue = baseValue;
      this.scalingPerLevel = scalingPerLevel;
      this.cooldownSeconds = cooldownSeconds;
      this.displayMaterial = displayMaterial;
      this.skillId = filename.replace(".yml", "").toLowerCase(Locale.ROOT);
   }

   public SkillBonusConfigFields(
      String filename,
      boolean isEnabled,
      String name,
      List<String> description,
      SkillType skillType,
      int unlockTier,
      double baseValue,
      double scalingPerLevel,
      double procChance,
      double cooldownSeconds,
      Material displayMaterial,
      boolean isCooldownType
   ) {
      super(filename, isEnabled);
      this.name = name;
      this.description = description;
      this.skillType = skillType;
      this.bonusType = isCooldownType ? SkillBonusType.COOLDOWN : SkillBonusType.PROC;
      this.unlockTier = unlockTier;
      this.baseValue = baseValue;
      this.scalingPerLevel = scalingPerLevel;
      this.procChance = procChance;
      this.cooldownSeconds = cooldownSeconds;
      this.displayMaterial = displayMaterial;
      this.skillId = filename.replace(".yml", "").toLowerCase(Locale.ROOT);
   }

   public SkillBonusConfigFields(
      String filename,
      boolean isEnabled,
      String name,
      List<String> description,
      SkillType skillType,
      int unlockTier,
      int maxStacks,
      double stackBonus,
      double scalingPerLevel,
      Material displayMaterial
   ) {
      super(filename, isEnabled);
      this.name = name;
      this.description = description;
      this.skillType = skillType;
      this.bonusType = SkillBonusType.STACKING;
      this.unlockTier = unlockTier;
      this.maxStacks = maxStacks;
      this.stackBonus = stackBonus;
      this.scalingPerLevel = scalingPerLevel;
      this.displayMaterial = displayMaterial;
      this.skillId = filename.replace(".yml", "").toLowerCase(Locale.ROOT);
   }

   @Override
   public void processConfigFields() {
      this.migrateLegacyDisplayDefaults();
      this.isEnabled = this.processBoolean("isEnabled", this.isEnabled, true, true);
      this.name = this.translatable(this.filename, "name", this.processString("name", this.name, "Skill", true));
      this.description = this.translatable(this.filename, "description", this.processStringList("description", this.description, this.description, true));
      String skillTypeStr = this.processString("skillType", this.skillType.name(), this.skillType.name(), true);

      try {
         this.skillType = SkillType.valueOf(skillTypeStr.toUpperCase(Locale.ROOT));
      } catch (IllegalArgumentException var7) {
         this.skillType = SkillType.SWORDS;
      }

      String bonusTypeStr = this.processString("bonusType", this.bonusType.name(), this.bonusType.name(), true);

      try {
         this.bonusType = SkillBonusType.valueOf(bonusTypeStr.toUpperCase(Locale.ROOT));
      } catch (IllegalArgumentException var6) {
         this.bonusType = SkillBonusType.PASSIVE;
      }

      this.unlockTier = this.processInt("unlockTier", this.unlockTier, 1, true);
      this.baseValue = this.processDouble("baseValue", this.baseValue, 1.0, true);
      this.scalingPerLevel = this.processDouble("scalingPerLevel", this.scalingPerLevel, 0.1, true);
      this.procChance = this.processDouble("procChance", this.procChance, 0.0, false);
      this.cooldownSeconds = this.processDouble("cooldownSeconds", this.cooldownSeconds, 0.0, false);
      this.maxStacks = this.processInt("maxStacks", this.maxStacks, 0, false);
      this.stackBonus = this.processDouble("stackBonus", this.stackBonus, 0.0, false);
      String materialStr = this.processString("displayMaterial", this.displayMaterial.name(), this.displayMaterial.name(), true);

      try {
         this.displayMaterial = Material.valueOf(materialStr.toUpperCase(Locale.ROOT));
      } catch (IllegalArgumentException var5) {
         this.displayMaterial = Material.IRON_SWORD;
      }

      this.customModelData = this.processInt("customModelData", this.customModelData, 0, false);
      this.skillId = this.filename.replace(".yml", "").toLowerCase(Locale.ROOT);
      this.loreTemplates = this.translatable(
         this.filename, "loreTemplates", this.processStringList("loreTemplates", this.loreTemplates, List.of(), !this.loreTemplates.isEmpty())
      );
      this.formattedBonusTemplate = this.translatable(
         this.filename,
         "formattedBonusTemplate",
         this.processString("formattedBonusTemplate", this.formattedBonusTemplate, "", !this.formattedBonusTemplate.isEmpty())
      );
      this.processAdditionalFields();
   }

   protected void migrateLegacyDisplayDefaults() {
   }

   protected final void migrateStringIfExact(String path, String legacyValue, String replacement) {
      if (Objects.equals(this.fileConfiguration.getString(path), legacyValue)) {
         this.fileConfiguration.set(path, replacement);
      }
   }

   protected final void migrateStringListIfExact(String path, List<String> legacyValue, List<String> replacement) {
      if (Objects.equals(this.fileConfiguration.getStringList(path), legacyValue)) {
         this.fileConfiguration.set(path, replacement);
      }
   }

   public void processAdditionalFields() {
   }

   public int getRequiredLevel() {
      return switch (this.unlockTier) {
         case 1 -> 10;
         case 2 -> 25;
         case 3 -> 50;
         case 4 -> 75;
         default -> 100;
      };
   }

   public double calculateValue(int skillLevel) {
      return Math.min(10.0, this.baseValue + skillLevel * this.scalingPerLevel);
   }

   public double calculateProcChance(int skillLevel) {
      return Math.min(1.0, this.procChance + skillLevel * 0.002);
   }

   public double calculateCooldown(int skillLevel) {
      double reduction = 1.0 - skillLevel * 0.005;
      return Math.max(this.cooldownSeconds * 0.5, this.cooldownSeconds * reduction);
   }

   @Generated
   public String getName() {
      return this.name;
   }

   @Generated
   public List<String> getDescription() {
      return this.description;
   }

   @Generated
   public SkillType getSkillType() {
      return this.skillType;
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
   public double getBaseValue() {
      return this.baseValue;
   }

   @Generated
   public double getScalingPerLevel() {
      return this.scalingPerLevel;
   }

   @Generated
   public double getProcChance() {
      return this.procChance;
   }

   @Generated
   public double getCooldownSeconds() {
      return this.cooldownSeconds;
   }

   @Generated
   public int getMaxStacks() {
      return this.maxStacks;
   }

   @Generated
   public double getStackBonus() {
      return this.stackBonus;
   }

   @Generated
   public Material getDisplayMaterial() {
      return this.displayMaterial;
   }

   @Generated
   public int getCustomModelData() {
      return this.customModelData;
   }

   @Generated
   public String getSkillId() {
      return this.skillId;
   }

   @Generated
   public List<String> getLoreTemplates() {
      return this.loreTemplates;
   }

   @Generated
   public String getFormattedBonusTemplate() {
      return this.formattedBonusTemplate;
   }
}
