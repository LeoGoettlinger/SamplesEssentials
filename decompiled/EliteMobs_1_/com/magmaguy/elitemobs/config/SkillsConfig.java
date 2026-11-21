package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.magmacore.config.ConfigurationFile;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.Generated;
import org.bukkit.entity.Player;

public class SkillsConfig extends ConfigurationFile {
   private static boolean skillSystemEnabled;
   private static String skillsMenuTitle;
   private static boolean showCombatLevelDisplay;
   private static boolean showXPBar;
   private static boolean armorSkillHealthBonusEnabled;
   private static boolean scalePlayerHealthDisplayToVanilla;
   private static boolean forceDefaultHealthWhenArmorSkillHealthBonusDisabled;
   private static List<String> skillWorldExclusions;
   private static List<String> classLabelWorldExclusions;
   private static String skillBarTitleFormat;
   private static String skillLevelUpTitleFormat;
   private static String combatLevelFormat;
   private static double customXpPerkMultiplier = 1.0;
   private static SkillsConfig instance;

   public SkillsConfig() {
      super("skills.yml");
   }

   public static void toggleSkillSystem(boolean enabled) {
      skillSystemEnabled = enabled;
      instance.fileConfiguration.set("skillSystemEnabled", enabled);
      ConfigurationEngine.fileSaverCustomValues(instance.fileConfiguration, instance.file);
   }

   public static boolean isWorldExcludedFromSkills(Player player) {
      return player != null && player.getWorld() != null ? isWorldExcludedFromSkills(player.getWorld().getName()) : false;
   }

   public static boolean isWorldExcludedFromSkills(String worldName) {
      return worldIsExcluded(worldName, skillWorldExclusions);
   }

   public static boolean isWorldExcludedFromClassLabels(Player player) {
      return player != null && player.getWorld() != null ? worldIsExcluded(player.getWorld().getName(), classLabelWorldExclusions) : false;
   }

   private static boolean worldIsExcluded(String worldName, List<String> excludedWorlds) {
      if (excludedWorlds == null || excludedWorlds.isEmpty()) {
         return false;
      } else if (worldName != null && !worldName.isBlank()) {
         String normalizedWorldName = worldName.trim();
         return excludedWorlds.stream()
            .filter(Objects::nonNull)
            .map(String::trim)
            .anyMatch(excludedWorld -> !excludedWorld.isEmpty() && excludedWorld.equalsIgnoreCase(normalizedWorldName));
      } else {
         return false;
      }
   }

   @Override
   public void initializeValues() {
      instance = this;
      skillSystemEnabled = ConfigurationEngine.setBoolean(
         List.of("Enables or disables the skill leveling system.", "When enabled, players will earn XP for different weapon types and armor."),
         this.fileConfiguration,
         "skillSystemEnabled",
         true
      );
      customXpPerkMultiplier = ConfigurationEngine.setDouble(
         List.of(
            "Multiplier for elitemobs.perks.xp.customboost.<skill> and .all.",
            "Overrides fixed XP perks. Must be finite and non-negative; 1.25 gives +25%.",
            "Applies only to eligible combat weapon/armor skill XP, before the public award event."
         ),
         this.fileConfiguration,
         "customXpPerkMultiplier",
         1.0
      );
      if (!Double.isFinite(customXpPerkMultiplier) || customXpPerkMultiplier < 0.0) {
         Logger.warn("Invalid customXpPerkMultiplier in skills.yml; using 1.0.");
         customXpPerkMultiplier = 1.0;
      }

      armorSkillHealthBonusEnabled = ConfigurationEngine.setBoolean(
         List.of("Whether Armor skill grants bonus max health (+1 heart per level above 1).", "Set to false to disable this mechanic."),
         this.fileConfiguration,
         "armorSkillHealthBonusEnabled",
         true
      );
      scalePlayerHealthDisplayToVanilla = ConfigurationEngine.setBoolean(
         List.of(
            "Whether EliteMobs should scale the player's client-side health display back to the vanilla 10 hearts while keeping the real Armor skill max health.",
            "This is purely visual: players keep their full EliteMobs HP and combat calculations are unchanged."
         ),
         this.fileConfiguration,
         "scalePlayerHealthDisplayToVanilla",
         false
      );
      forceDefaultHealthWhenArmorSkillHealthBonusDisabled = ConfigurationEngine.setBoolean(
         List.of(
            "Only applies when armorSkillHealthBonusEnabled is false.",
            "If true, players are forced to vanilla 20 max health on join/updates.",
            "If false, existing max health values are left untouched."
         ),
         this.fileConfiguration,
         "forceDefaultHealthWhenArmorSkillHealthBonusDisabled",
         false
      );
      skillWorldExclusions = ConfigurationEngine.setList(
         List.of(
            "Sets worlds where the EliteMobs skill system is inactive for players.",
            "Players in these worlds do not receive EliteMobs skill bonuses, armor skill bonus health, skill XP, skill XP bars or combat level displays.",
            "Use this for event worlds where every player should have the same attributes.",
            "World names are exact and case-insensitive. Leave this empty to allow skills in every world.",
            "Example:",
            "- event_world"
         ),
         this.file,
         this.fileConfiguration,
         "skillWorldExclusions",
         Collections.emptyList(),
         false
      );
      classLabelWorldExclusions = ConfigurationEngine.setList(
         List.of(
            "Hides only the class label above players in these worlds.",
            "Player names, combat health displays, skills and XP are unchanged.",
            "Players without a selected class keep their normal combat level display.",
            "World names are exact and case-insensitive; surrounding spaces are ignored.",
            "Leave empty to show class labels in every world. showCombatLevelDisplay still controls all these displays."
         ),
         this.file,
         this.fileConfiguration,
         "classLabelWorldExclusions",
         Collections.emptyList(),
         false
      );
      skillsMenuTitle = ConfigurationEngine.setString(
         List.of("Title of the skills menu."), this.file, this.fileConfiguration, "skillsMenuTitle", "&5&lYour Skills", true
      );
      showCombatLevelDisplay = ConfigurationEngine.setBoolean(
         List.of(
            "Whether to show the class or combat level display above players.",
            "Combat level is the average of the two highest weapon skills and armor.",
            "The display follows player visibility and scoreboard team name-tag visibility exposed by Spigot.",
            "Packet-only name-tag changes cannot be detected; disable this setting if another plugin hides names that way."
         ),
         this.fileConfiguration,
         "showCombatLevelDisplay",
         true
      );
      showXPBar = ConfigurationEngine.setBoolean(
         List.of("Whether to show an animated XP progress bar when gaining skill XP.", "The bar shows level progress and plays effects on level up."),
         this.fileConfiguration,
         "showXPBar",
         true
      );
      skillBarTitleFormat = ConfigurationEngine.setString(
         List.of("Format for the skill XP bar title.", "Use $skillName for the skill name, $level for the level, $xpText for the XP gained text."),
         this.file,
         this.fileConfiguration,
         "skillBarTitleFormat",
         "&6$skillName &7Lv.$level$xpText",
         true
      );
      skillLevelUpTitleFormat = ConfigurationEngine.setString(
         List.of("Format for the skill level up boss bar title.", "Use $skillName for the skill name, $level for the new level."),
         this.file,
         this.fileConfiguration,
         "skillLevelUpTitleFormat",
         "&e&l✦ $skillName LEVEL UP! &7→ &eLv.$level &e&l✦",
         true
      );
      combatLevelFormat = ConfigurationEngine.setString(
         List.of("Format for displaying the combat level.", "Use $level for the combat level number."),
         this.file,
         this.fileConfiguration,
         "combatLevelFormat",
         "&6&lCombat Lv. $level",
         true
      );
   }

   @Generated
   public static boolean isSkillSystemEnabled() {
      return skillSystemEnabled;
   }

   @Generated
   public static String getSkillsMenuTitle() {
      return skillsMenuTitle;
   }

   @Generated
   public static boolean isShowCombatLevelDisplay() {
      return showCombatLevelDisplay;
   }

   @Generated
   public static boolean isShowXPBar() {
      return showXPBar;
   }

   @Generated
   public static boolean isArmorSkillHealthBonusEnabled() {
      return armorSkillHealthBonusEnabled;
   }

   @Generated
   public static boolean isScalePlayerHealthDisplayToVanilla() {
      return scalePlayerHealthDisplayToVanilla;
   }

   @Generated
   public static boolean isForceDefaultHealthWhenArmorSkillHealthBonusDisabled() {
      return forceDefaultHealthWhenArmorSkillHealthBonusDisabled;
   }

   @Generated
   public static List<String> getSkillWorldExclusions() {
      return skillWorldExclusions;
   }

   @Generated
   public static List<String> getClassLabelWorldExclusions() {
      return classLabelWorldExclusions;
   }

   @Generated
   public static String getSkillBarTitleFormat() {
      return skillBarTitleFormat;
   }

   @Generated
   public static String getSkillLevelUpTitleFormat() {
      return skillLevelUpTitleFormat;
   }

   @Generated
   public static String getCombatLevelFormat() {
      return combatLevelFormat;
   }

   @Generated
   public static double getCustomXpPerkMultiplier() {
      return customXpPerkMultiplier;
   }
}
