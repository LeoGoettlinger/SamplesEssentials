package com.magmaguy.elitemobs.config.menus.premade;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.menus.MenusConfigFields;
import com.magmaguy.elitemobs.magmacore.util.ItemStackGenerator;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lombok.Generated;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SkillBonusMenuConfig extends MenusConfigFields {
   private static String weaponSelectMenuName;
   private static String skillSelectMenuName;
   private static ItemStack swordsItem;
   private static int swordsSlot;
   private static ItemStack axesItem;
   private static int axesSlot;
   private static ItemStack bowsItem;
   private static int bowsSlot;
   private static ItemStack crossbowsItem;
   private static int crossbowsSlot;
   private static ItemStack tridentsItem;
   private static int tridentsSlot;
   private static ItemStack hoesItem;
   private static int hoesSlot;
   private static ItemStack armorItem;
   private static int armorSlot;
   private static ItemStack macesItem;
   private static int macesSlot;
   private static ItemStack spearsItem;
   private static int spearsSlot;
   private static ItemStack stavesItem;
   private static int stavesSlot;
   private static Material defaultStavesMaterial;
   private static ItemStack wandsItem;
   private static int wandsSlot;
   private static Material defaultWandsMaterial = Material.BLAZE_ROD;
   private static ItemStack backItem;
   private static int backSlot;
   private static int infoSlot;
   private static String lockedPrefix;
   private static String unlockedPrefix;
   private static String activePrefix;
   private static String lockedLore;
   private static String clickToActivate;
   private static String clickToDeactivate;
   private static String maxActiveSkillsReached;
   private static String skillTypeLabel;
   private static String skillTierLabel;
   private static String skillTierLevelFormat;
   private static String skillValueLabel;
   private static String infoItemTitle;
   private static String infoSkillTypeLabel;
   private static String infoYourLevelLabel;
   private static String infoActiveSkillsLabel;
   private static String infoActiveSkillsHeader;
   private static String infoActiveSkillPrefix;
   private static String infoUnlockLevelsHeader;
   private static String infoTier1Unlock;
   private static String infoTier2Unlock;
   private static String infoTier3Unlock;
   private static String infoTier4Unlock;
   private static String skillRequiresLevelMessage;
   private static String skillDeactivatedMessage;
   private static String skillActivatedMessage;
   private static final Map<SkillType, String> skillTypeDisplayNames = new EnumMap<>(SkillType.class);
   private static final Map<SkillBonusType, String> bonusTypeDisplayNames = new EnumMap<>(SkillBonusType.class);

   public static String getSkillTypeDisplayName(SkillType type) {
      return skillTypeDisplayNames.getOrDefault(type, type.getDisplayName());
   }

   public static String getBonusTypeDisplayName(SkillBonusType type) {
      return bonusTypeDisplayNames.getOrDefault(type, type.name());
   }

   public SkillBonusMenuConfig() {
      super("skill_bonus_menu", true);
   }

   private static void hideAttributes(ItemStack item) {
      ItemMeta meta = item.getItemMeta();
      if (meta != null) {
         meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ATTRIBUTES});
         item.setItemMeta(meta);
      }
   }

   @Override
   public void processAdditionalFields() {
      weaponSelectMenuName = ConfigurationEngine.setString(this.file, this.fileConfiguration, "weaponSelectMenuName", "&6Select Weapon Type", true);
      skillSelectMenuName = ConfigurationEngine.setString(this.file, this.fileConfiguration, "skillSelectMenuName", "&6%skill_type% Skills", true);
      swordsItem = ConfigurationEngine.setItemStack(
         this.file,
         this.fileConfiguration,
         "swordsItem",
         ItemStackGenerator.generateItemStack(Material.DIAMOND_SWORD, "&6Swords", List.of("&7View sword skills", "&7Click to select")),
         true
      );
      swordsSlot = ConfigurationEngine.setInt(this.fileConfiguration, "swordsSlot", 10);
      axesItem = ConfigurationEngine.setItemStack(
         this.file,
         this.fileConfiguration,
         "axesItem",
         ItemStackGenerator.generateItemStack(Material.DIAMOND_AXE, "&6Axes", List.of("&7View axe skills", "&7Click to select")),
         true
      );
      axesSlot = ConfigurationEngine.setInt(this.fileConfiguration, "axesSlot", 11);
      bowsItem = ConfigurationEngine.setItemStack(
         this.file,
         this.fileConfiguration,
         "bowsItem",
         ItemStackGenerator.generateItemStack(Material.BOW, "&6Bows", List.of("&7View bow skills", "&7Click to select")),
         true
      );
      bowsSlot = ConfigurationEngine.setInt(this.fileConfiguration, "bowsSlot", 12);
      crossbowsItem = ConfigurationEngine.setItemStack(
         this.file,
         this.fileConfiguration,
         "crossbowsItem",
         ItemStackGenerator.generateItemStack(Material.CROSSBOW, "&6Crossbows", List.of("&7View crossbow skills", "&7Click to select")),
         true
      );
      crossbowsSlot = ConfigurationEngine.setInt(this.fileConfiguration, "crossbowsSlot", 13);
      tridentsItem = ConfigurationEngine.setItemStack(
         this.file,
         this.fileConfiguration,
         "tridentsItem",
         ItemStackGenerator.generateItemStack(Material.TRIDENT, "&6Tridents", List.of("&7View trident skills", "&7Click to select")),
         true
      );
      tridentsSlot = ConfigurationEngine.setInt(this.fileConfiguration, "tridentsSlot", 14);
      hoesItem = ConfigurationEngine.setItemStack(
         this.file,
         this.fileConfiguration,
         "hoesItem",
         ItemStackGenerator.generateItemStack(Material.DIAMOND_HOE, "&6Hoes (Scythes)", List.of("&7View hoe/scythe skills", "&7Click to select")),
         true
      );
      hoesSlot = ConfigurationEngine.setInt(this.fileConfiguration, "hoesSlot", 15);

      Material macesMaterial;
      try {
         macesMaterial = Material.MACE;
      } catch (NoSuchFieldError var12) {
         macesMaterial = Material.IRON_BLOCK;
      }

      macesItem = ConfigurationEngine.setItemStack(
         this.file,
         this.fileConfiguration,
         "macesItem",
         ItemStackGenerator.generateItemStack(macesMaterial, "&6Maces", List.of("&7View mace skills", "&7Click to select")),
         true
      );
      macesSlot = ConfigurationEngine.setInt(this.fileConfiguration, "macesSlot", 16);

      Material spearsMaterial;
      try {
         spearsMaterial = Material.IRON_SPEAR;
      } catch (NoSuchFieldError var11) {
         spearsMaterial = Material.IRON_SWORD;
      }

      spearsItem = ConfigurationEngine.setItemStack(
         this.file,
         this.fileConfiguration,
         "spearsItem",
         ItemStackGenerator.generateItemStack(spearsMaterial, "&6Spears", List.of("&7View spear skills", "&7Click to select")),
         true
      );
      spearsSlot = ConfigurationEngine.setInt(this.fileConfiguration, "spearsSlot", 20);

      Material stavesMaterial;
      try {
         stavesMaterial = Material.WOODEN_SPEAR;
      } catch (NoSuchFieldError var10) {
         stavesMaterial = Material.STICK;
      }

      defaultStavesMaterial = stavesMaterial;
      stavesItem = ConfigurationEngine.setItemStack(
         this.file,
         this.fileConfiguration,
         "stavesItem",
         ItemStackGenerator.generateItemStack(stavesMaterial, "&6Staves", List.of("&7View staff progression", "&7Click to select")),
         true
      );
      stavesSlot = ConfigurationEngine.setInt(this.fileConfiguration, "stavesSlot", 23);
      wandsItem = ConfigurationEngine.setItemStack(
         this.file,
         this.fileConfiguration,
         "wandsItem",
         ItemStackGenerator.generateItemStack(defaultWandsMaterial, "&6Wands", List.of("&7View wand progression", "&7Click to select")),
         true
      );
      wandsSlot = ConfigurationEngine.setInt(this.fileConfiguration, "wandsSlot", 24);
      armorItem = ConfigurationEngine.setItemStack(
         this.file,
         this.fileConfiguration,
         "armorItem",
         ItemStackGenerator.generateItemStack(Material.DIAMOND_CHESTPLATE, "&6Armor", List.of("&7View armor skills", "&7Click to select")),
         true
      );
      armorSlot = ConfigurationEngine.setInt(this.fileConfiguration, "armorSlot", 21);
      hideAttributes(swordsItem);
      hideAttributes(axesItem);
      hideAttributes(bowsItem);
      hideAttributes(crossbowsItem);
      hideAttributes(tridentsItem);
      hideAttributes(hoesItem);
      hideAttributes(macesItem);
      hideAttributes(spearsItem);
      hideAttributes(stavesItem);
      hideAttributes(wandsItem);
      hideAttributes(armorItem);
      backItem = ConfigurationEngine.setItemStack(
         this.file,
         this.fileConfiguration,
         "backItem",
         ItemStackGenerator.generateItemStack(Material.ARROW, "&cBack", List.of("&7Return to weapon selection")),
         true
      );
      backSlot = ConfigurationEngine.setInt(this.fileConfiguration, "backSlot", 45);
      infoSlot = ConfigurationEngine.setInt(this.fileConfiguration, "infoSlot", 49);
      lockedPrefix = ConfigurationEngine.setString(this.file, this.fileConfiguration, "lockedPrefix", "&8[LOCKED] ", true);
      unlockedPrefix = ConfigurationEngine.setString(this.file, this.fileConfiguration, "unlockedPrefix", "&a[UNLOCKED] ", true);
      activePrefix = ConfigurationEngine.setString(this.file, this.fileConfiguration, "activePrefix", "&6[ACTIVE] ", true);
      lockedLore = ConfigurationEngine.setString(this.file, this.fileConfiguration, "lockedLore", "&cRequires level %level%", true);
      clickToActivate = ConfigurationEngine.setString(this.file, this.fileConfiguration, "clickToActivate", "&aClick to activate this skill", true);
      clickToDeactivate = ConfigurationEngine.setString(this.file, this.fileConfiguration, "clickToDeactivate", "&eClick to deactivate this skill", true);
      maxActiveSkillsReached = ConfigurationEngine.setString(
         this.file, this.fileConfiguration, "maxActiveSkillsReached", "&cYou already have 3 active skills! Deactivate one first.", true
      );
      skillTypeLabel = ConfigurationEngine.setString(
         List.of("Label for the skill bonus type in the skill item lore."), this.file, this.fileConfiguration, "skillTypeLabel", "&7Type: &f", true
      );
      skillTierLabel = ConfigurationEngine.setString(
         List.of("Label for the skill tier in the skill item lore."), this.file, this.fileConfiguration, "skillTierLabel", "&7Tier: &f", true
      );
      skillTierLevelFormat = ConfigurationEngine.setString(
         List.of("Format for the tier level display. %tier% = tier number, %level% = required level."),
         this.file,
         this.fileConfiguration,
         "skillTierLevelFormat",
         "%tier% (Level %level%)",
         true
      );
      skillValueLabel = ConfigurationEngine.setString(
         List.of("Label for the skill value at the player's current level."),
         this.file,
         this.fileConfiguration,
         "skillValueLabel",
         "&7Value at your level: &a",
         true
      );
      infoItemTitle = ConfigurationEngine.setString(
         List.of("Title of the info item in the skill selection menu."), this.file, this.fileConfiguration, "infoItemTitle", "&eSkill Info", true
      );
      infoSkillTypeLabel = ConfigurationEngine.setString(
         List.of("Label for the skill type in the info item lore."), this.file, this.fileConfiguration, "infoSkillTypeLabel", "&7Skill Type: &f", true
      );
      infoYourLevelLabel = ConfigurationEngine.setString(
         List.of("Label for the player's level in the info item lore."), this.file, this.fileConfiguration, "infoYourLevelLabel", "&7Your Level: &f", true
      );
      infoActiveSkillsLabel = ConfigurationEngine.setString(
         List.of("Label for the active skills count in the info item lore. %count% = current count."),
         this.file,
         this.fileConfiguration,
         "infoActiveSkillsLabel",
         "&7Active Skills: &f%count%/3",
         true
      );
      infoActiveSkillsHeader = ConfigurationEngine.setString(
         List.of("Header for the list of active skills in the info item lore."),
         this.file,
         this.fileConfiguration,
         "infoActiveSkillsHeader",
         "&6Active Skills:",
         true
      );
      infoActiveSkillPrefix = ConfigurationEngine.setString(
         List.of("Prefix for each active skill name in the info item lore."), this.file, this.fileConfiguration, "infoActiveSkillPrefix", "&7- ", true
      );
      infoUnlockLevelsHeader = ConfigurationEngine.setString(
         List.of("Header for the skill unlock levels section in the info item lore."),
         this.file,
         this.fileConfiguration,
         "infoUnlockLevelsHeader",
         "&7Skills unlock at levels:",
         true
      );
      infoTier1Unlock = ConfigurationEngine.setString(
         List.of("Tier 1 unlock level display in the info item lore."), this.file, this.fileConfiguration, "infoTier1Unlock", "&a Tier 1: &fLevel 10", true
      );
      infoTier2Unlock = ConfigurationEngine.setString(
         List.of("Tier 2 unlock level display in the info item lore."), this.file, this.fileConfiguration, "infoTier2Unlock", "&e Tier 2: &fLevel 25", true
      );
      infoTier3Unlock = ConfigurationEngine.setString(
         List.of("Tier 3 unlock level display in the info item lore."), this.file, this.fileConfiguration, "infoTier3Unlock", "&6 Tier 3: &fLevel 50", true
      );
      infoTier4Unlock = ConfigurationEngine.setString(
         List.of("Tier 4 unlock level display in the info item lore."), this.file, this.fileConfiguration, "infoTier4Unlock", "&c Tier 4: &fLevel 75", true
      );
      skillRequiresLevelMessage = ConfigurationEngine.setString(
         List.of("Message sent when a player tries to activate a skill they haven't unlocked. %level% = required level."),
         this.file,
         this.fileConfiguration,
         "skillRequiresLevelMessage",
         "&cThis skill requires level %level%!",
         true
      );
      skillDeactivatedMessage = ConfigurationEngine.setString(
         List.of("Message sent when a player deactivates a skill. %skill% = skill name."),
         this.file,
         this.fileConfiguration,
         "skillDeactivatedMessage",
         "&eDeactivated skill: %skill%",
         true
      );
      skillActivatedMessage = ConfigurationEngine.setString(
         List.of("Message sent when a player activates a skill. %skill% = skill name."),
         this.file,
         this.fileConfiguration,
         "skillActivatedMessage",
         "&aActivated skill: %skill%",
         true
      );
      skillTypeDisplayNames.clear();

      for (SkillType type : SkillType.values()) {
         String key = "skillTypeName." + type.name().toLowerCase();
         String translated = ConfigurationEngine.setString(this.file, this.fileConfiguration, key, type.getDisplayName(), true);
         skillTypeDisplayNames.put(type, translated);
      }

      bonusTypeDisplayNames.clear();
      bonusTypeDisplayNames.put(SkillBonusType.PROC, ConfigurationEngine.setString(this.file, this.fileConfiguration, "bonusTypeName.proc", "Proc", true));
      bonusTypeDisplayNames.put(
         SkillBonusType.PASSIVE, ConfigurationEngine.setString(this.file, this.fileConfiguration, "bonusTypeName.passive", "Passive", true)
      );
      bonusTypeDisplayNames.put(
         SkillBonusType.CONDITIONAL, ConfigurationEngine.setString(this.file, this.fileConfiguration, "bonusTypeName.conditional", "Conditional", true)
      );
      bonusTypeDisplayNames.put(
         SkillBonusType.STACKING, ConfigurationEngine.setString(this.file, this.fileConfiguration, "bonusTypeName.stacking", "Stacking", true)
      );
      bonusTypeDisplayNames.put(
         SkillBonusType.COOLDOWN, ConfigurationEngine.setString(this.file, this.fileConfiguration, "bonusTypeName.cooldown", "Cooldown", true)
      );
   }

   @Generated
   public static String getWeaponSelectMenuName() {
      return weaponSelectMenuName;
   }

   @Generated
   public static String getSkillSelectMenuName() {
      return skillSelectMenuName;
   }

   @Generated
   public static ItemStack getSwordsItem() {
      return swordsItem;
   }

   @Generated
   public static int getSwordsSlot() {
      return swordsSlot;
   }

   @Generated
   public static ItemStack getAxesItem() {
      return axesItem;
   }

   @Generated
   public static int getAxesSlot() {
      return axesSlot;
   }

   @Generated
   public static ItemStack getBowsItem() {
      return bowsItem;
   }

   @Generated
   public static int getBowsSlot() {
      return bowsSlot;
   }

   @Generated
   public static ItemStack getCrossbowsItem() {
      return crossbowsItem;
   }

   @Generated
   public static int getCrossbowsSlot() {
      return crossbowsSlot;
   }

   @Generated
   public static ItemStack getTridentsItem() {
      return tridentsItem;
   }

   @Generated
   public static int getTridentsSlot() {
      return tridentsSlot;
   }

   @Generated
   public static ItemStack getHoesItem() {
      return hoesItem;
   }

   @Generated
   public static int getHoesSlot() {
      return hoesSlot;
   }

   @Generated
   public static ItemStack getArmorItem() {
      return armorItem;
   }

   @Generated
   public static int getArmorSlot() {
      return armorSlot;
   }

   @Generated
   public static ItemStack getMacesItem() {
      return macesItem;
   }

   @Generated
   public static int getMacesSlot() {
      return macesSlot;
   }

   @Generated
   public static ItemStack getSpearsItem() {
      return spearsItem;
   }

   @Generated
   public static int getSpearsSlot() {
      return spearsSlot;
   }

   @Generated
   public static ItemStack getStavesItem() {
      return stavesItem;
   }

   @Generated
   public static int getStavesSlot() {
      return stavesSlot;
   }

   @Generated
   public static Material getDefaultStavesMaterial() {
      return defaultStavesMaterial;
   }

   @Generated
   public static ItemStack getWandsItem() {
      return wandsItem;
   }

   @Generated
   public static int getWandsSlot() {
      return wandsSlot;
   }

   @Generated
   public static Material getDefaultWandsMaterial() {
      return defaultWandsMaterial;
   }

   @Generated
   public static ItemStack getBackItem() {
      return backItem;
   }

   @Generated
   public static int getBackSlot() {
      return backSlot;
   }

   @Generated
   public static int getInfoSlot() {
      return infoSlot;
   }

   @Generated
   public static String getLockedPrefix() {
      return lockedPrefix;
   }

   @Generated
   public static String getUnlockedPrefix() {
      return unlockedPrefix;
   }

   @Generated
   public static String getActivePrefix() {
      return activePrefix;
   }

   @Generated
   public static String getLockedLore() {
      return lockedLore;
   }

   @Generated
   public static String getClickToActivate() {
      return clickToActivate;
   }

   @Generated
   public static String getClickToDeactivate() {
      return clickToDeactivate;
   }

   @Generated
   public static String getMaxActiveSkillsReached() {
      return maxActiveSkillsReached;
   }

   @Generated
   public static String getSkillTypeLabel() {
      return skillTypeLabel;
   }

   @Generated
   public static String getSkillTierLabel() {
      return skillTierLabel;
   }

   @Generated
   public static String getSkillTierLevelFormat() {
      return skillTierLevelFormat;
   }

   @Generated
   public static String getSkillValueLabel() {
      return skillValueLabel;
   }

   @Generated
   public static String getInfoItemTitle() {
      return infoItemTitle;
   }

   @Generated
   public static String getInfoSkillTypeLabel() {
      return infoSkillTypeLabel;
   }

   @Generated
   public static String getInfoYourLevelLabel() {
      return infoYourLevelLabel;
   }

   @Generated
   public static String getInfoActiveSkillsLabel() {
      return infoActiveSkillsLabel;
   }

   @Generated
   public static String getInfoActiveSkillsHeader() {
      return infoActiveSkillsHeader;
   }

   @Generated
   public static String getInfoActiveSkillPrefix() {
      return infoActiveSkillPrefix;
   }

   @Generated
   public static String getInfoUnlockLevelsHeader() {
      return infoUnlockLevelsHeader;
   }

   @Generated
   public static String getInfoTier1Unlock() {
      return infoTier1Unlock;
   }

   @Generated
   public static String getInfoTier2Unlock() {
      return infoTier2Unlock;
   }

   @Generated
   public static String getInfoTier3Unlock() {
      return infoTier3Unlock;
   }

   @Generated
   public static String getInfoTier4Unlock() {
      return infoTier4Unlock;
   }

   @Generated
   public static String getSkillRequiresLevelMessage() {
      return skillRequiresLevelMessage;
   }

   @Generated
   public static String getSkillDeactivatedMessage() {
      return skillDeactivatedMessage;
   }

   @Generated
   public static String getSkillActivatedMessage() {
      return skillActivatedMessage;
   }
}
