package com.magmaguy.elitemobs.config.menus.premade;

import com.magmaguy.elitemobs.config.AdventurersGuildConfig;
import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.menus.MenusConfigFields;
import com.magmaguy.elitemobs.magmacore.util.ItemStackGenerator;
import com.magmaguy.elitemobs.playerdata.statusscreen.PlayerStatusScreen;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import lombok.Generated;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

public class PlayerStatusMenuConfig extends MenusConfigFields {
   private static final String ROOT_TITLE = "<g:#8B0000:#CC4400:#DAA520>EliteMobs</g> &7Menu";
   private static final String STATS_TITLE = "<g:#B8860B:#F0C040>★ Adventure Record</g>";
   private static final String GEAR_TITLE = "<g:#A63D2F:#E97932>⚔ Combat Gear</g>";
   private static final String TELEPORTS_TITLE = "<g:#355CA8:#5FA9E8>↔ Teleports</g>";
   private static final String COMMANDS_TITLE = "<g:#267A78:#58B8A9>⚡ Quick Actions</g>";
   private static final String QUESTS_TITLE = "<g:#2E7D4F:#69C56F>✉ Quests</g>";
   private static final String BOSS_TITLE = "<g:#7A1F2B:#C2414A>☠ Boss Tracker</g>";
   private static final String SKILLS_TITLE = "<g:#6D3AA8:#A855F7>⚗ Skills</g>";
   private static final String PARTY_TITLE = "<g:#A04468:#E07A9A>♥ Party</g>";
   private static final String[] indexTextLines = new String[14];
   private static final String[] indexHoverLines = new String[14];
   private static final String[] indexCommandLines = new String[14];
   private static final String[] statsTextLines = new String[13];
   private static final String[] statsHoverLines = new String[13];
   private static final String[] statsCommandLines = new String[13];
   private static final String[] gearTextLines = new String[13];
   private static final String[] gearHoverLines = new String[13];
   private static final String[] teleportTextLines = new String[13];
   private static final String[] teleportHoverLines = new String[13];
   private static final String[] teleportCommandLines = new String[13];
   private static final String[] commandsTextLines = new String[13];
   private static final String[] commandsHoverLines = new String[13];
   private static final String[] commandsCommandLines = new String[13];
   private static final String[] bossTrackerTextLines = new String[13];
   private static final String[] bossTrackerHoverLines = new String[13];
   private static final String[] bossTrackerCommandLines = new String[13];
   private static boolean doStatsPage;
   private static boolean doGearPage;
   private static boolean doTeleportsPage;
   private static boolean doCommandsPage;
   private static boolean doQuestTrackingPage;
   private static boolean doBossTrackingPage;
   private static List<String> landingOverviewLines;
   private static List<String> dialogGearSummaryLines;
   private static String gearUnarmedLabel;
   private static String gearNoSelectedPerksLabel;
   private static String dialogGearHelmetLabel;
   private static String dialogGearChestplateLabel;
   private static String dialogGearLeggingsLabel;
   private static String dialogGearBootsLabel;
   private static String dialogGearMainHandLabel;
   private static String dialogGearOffHandLabel;
   private static String dialogQuestFallbackFormat;
   private static String teleportChestMenuName;
   private static ItemStack teleportSpawnItem;
   private static int teleportSpawnSlot;
   private static ItemStack teleportGuildItem;
   private static int teleportGuildSlot;
   private static String bossTrackerChestMenuName;
   private static String onBossTrackHover;
   private static String onTeleportHover;
   private static String indexChestMenuName;
   private static ItemStack backItem;
   private static ItemStack indexHeaderItem;
   private static int indexHeaderSlot;
   private static ItemStack indexStatsItem;
   private static int indexStatsSlot;
   private static ItemStack indexGearItem;
   private static int indexGearSlot;
   private static ItemStack indexTeleportsItem;
   private static int indexTeleportsSlot;
   private static ItemStack indexCommandsItem;
   private static int indexCommandsSlot;
   private static ItemStack indexQuestTrackingItem;
   private static int indexQuestTrackingSlot;
   private static ItemStack indexBossTrackingItem;
   private static int indexBossTrackingSlot;
   private static ItemStack indexSkillsItem;
   private static int indexSkillsSlot;
   private static ItemStack indexPartyItem;
   private static int indexPartySlot;
   private static String gearChestMenuName;
   private static ItemStack gearDamageItem;
   private static int gearDamageSlot;
   private static ItemStack gearArmorItem;
   private static int gearArmorSlot;
   private static ItemStack gearThreatItem;
   private static int gearThreatSlot;
   private static String statsChestMenuName;
   private static String statsRankUnavailableText;
   private static String statsRankTotalUnavailableText;
   private static ItemStack statsRankItem;
   private static int statsRankSlot;
   private static ItemStack statsDungeonsCompletedItem;
   private static int statsDungeonsCompletedSlot;
   private static ItemStack statsCombatLevelItem;
   private static int statsCombatLevelSlot;
   private static ItemStack statsEliteKillsItem;
   private static int statsEliteKillsSlot;
   private static ItemStack statsMaxEliteLevelKilledItem;
   private static int statsMaxEliteLevelKilledSlot;
   private static ItemStack statsQuestsCompletedItem;
   private static int statsQuestsCompletedSlot;
   private static ItemStack statsScoreItem;
   private static int statsScoreSlot;
   private static String commandsChestMenuName;
   private static ItemStack commandsAGItem;
   private static int commandsAGSlot;
   private static ItemStack commandsSpawnItem;
   private static int commandsSpawnSlot;
   private static ItemStack commandsShareItemItem;
   private static int commandsShareItemSlot;
   private static String skillsItemDisplayName;
   private static String skillsItemLore1;
   private static String skillsItemLore2;
   private static String skillsItemClickLore;
   private static String skillsPageHeader;
   private static String skillsPageLevelFormat;
   private static String skillsPageXpFormat;
   private static String skillItemDisplayNameFormat;
   private static String skillItemSelectLore1;
   private static String skillItemSelectLore2;
   private static String dialogTitlePlayerStatus;
   private static String dialogTitleStats;
   private static String dialogTitleGear;
   private static String dialogTitleTeleports;
   private static String dialogTitleCommands;
   private static String dialogTitleQuests;
   private static String dialogTitleBossTracking;
   private static String dialogTitleSkills;
   private static String dialogTitleParty;
   private static String dialogPartyDescription;
   private static String dialogPartyCreateButton;
   private static String dialogPartyInviteButton;
   private static String dialogPartyLeaveButton;
   private static String dialogPartyInviteTitle;
   private static String dialogPartyInvitePlayerButton;
   private static String dialogPartyInvitePlayerTooltip;
   private static String dialogPartyInviteNoPlayers;
   private static String dialogNoActiveQuests;
   private static String dialogNoTrackableBosses;
   private static String dialogActiveQuestsFormat;
   private static String dialogBackButton;

   public PlayerStatusMenuConfig() {
      super("player_status_screen", true);
   }

   private static void indexLineCreator(int line, String text, String hover, String command, FileConfiguration fileConfiguration, File file) {
      indexTextLines[line] = ConfigurationEngine.setString(file, fileConfiguration, "indexTexts" + line, text, true);
      indexHoverLines[line] = ConfigurationEngine.setString(file, fileConfiguration, "indexHovers" + line, hover, true);
      indexCommandLines[line] = ConfigurationEngine.setString(file, fileConfiguration, "indexCommands" + line, command, false);
   }

   private static void statsLineCreator(int line, String text, String hover, String command, FileConfiguration fileConfiguration, File file) {
      statsTextLines[line] = ConfigurationEngine.setString(file, fileConfiguration, "statsText" + line, text, true);
      statsHoverLines[line] = ConfigurationEngine.setString(file, fileConfiguration, "statsHover" + line, hover, true);
      statsCommandLines[line] = ConfigurationEngine.setString(file, fileConfiguration, "statsCommand" + line, command, false);
   }

   private static void gearLineCreator(int line, String text, String hover, String command, FileConfiguration fileConfiguration, File file) {
      gearTextLines[line] = ConfigurationEngine.setString(file, fileConfiguration, "gearText" + line, text, true);
      gearHoverLines[line] = ConfigurationEngine.setString(file, fileConfiguration, "gearHover" + line, hover, true);
   }

   private static void teleportLineCreator(int line, String text, String hover, String command, FileConfiguration fileConfiguration, File file) {
      teleportTextLines[line] = ConfigurationEngine.setString(file, fileConfiguration, "teleportTextV2" + line, text, true);
      teleportHoverLines[line] = ConfigurationEngine.setString(file, fileConfiguration, "teleportHoverV2" + line, hover, true);
      teleportCommandLines[line] = ConfigurationEngine.setString(file, fileConfiguration, "teleportCommandV2" + line, command, false);
   }

   private static void commandsLineCreator(int line, String text, String hover, String command, FileConfiguration fileConfiguration, File file) {
      commandsTextLines[line] = ConfigurationEngine.setString(file, fileConfiguration, "commandsText" + line, text, true);
      commandsHoverLines[line] = ConfigurationEngine.setString(file, fileConfiguration, "commandsHover" + line, hover, true);
      commandsCommandLines[line] = ConfigurationEngine.setString(file, fileConfiguration, "commandsCommand" + line, command, false);
   }

   private static void bossTrackerLineCreator(int line, String text, String hover, String command, FileConfiguration fileConfiguration, File file) {
      bossTrackerTextLines[line] = ConfigurationEngine.setString(file, fileConfiguration, "bossTrackerText" + line, text, true);
      bossTrackerHoverLines[line] = ConfigurationEngine.setString(file, fileConfiguration, "bossTrackerHover" + line, hover, true);
      bossTrackerCommandLines[line] = ConfigurationEngine.setString(file, fileConfiguration, "bossTrackerCommand" + line, command, false);
   }

   private void migrateLegacyQuickActionDefaults() {
      this.migrateLegacyStatsDefaults();
      this.migrateExactString("indexTexts9", "&bp. $commandsPage &8- &6Commands", "&bp. $commandsPage &8- &6Quick Actions");
      this.migrateExactString("indexHovers9", "Click to go!", "Open useful EliteMobs actions");
      this.migrateExactString("commandsText1", "&3&lCommands:", "&3&lQuick Actions:");
      this.migrateExactString("commandsText4", "&5/ag", "&5/ag &7- EliteMobs Hub");
      this.migrateLegacySpawnAction();
      this.migrateExactString("commandsText6", "&5/shareitem", "&5/em shareitem &7- Share Item");
      this.migrateExactString("commandsCommand6", "/shareitem", "/em shareitem");
      this.migrateExactString("commandsChestMenuName", "&2EliteMobs Commands", "&2EliteMobs Quick Actions");
      this.migrateExactColorString("commandsShareItemItem.name", "&5/shareitem", "&5/em shareitem");
      this.migrateExactString("dialog.titleCommands", "Commands", "&6Quick Actions");
      this.migrateExactString(
         "gearHover11",
         "This determines the level of the\nElite Mobs that spawns near you.\nTakes armor, weapon in hand, guild\ntier into account.\n",
         "This is your combat level and determines the\nlevel of Elite Mobs that spawn near you.\nIt averages your armor skill and your\ntwo highest weapon skills.\n"
      );
      boolean missingLegacyCommandsLore = !this.fileConfiguration.isSet("indexCommandsItem.lore");
      if (this.isExactString("indexCommandsItem.material", Material.JUKEBOX.name())
         && this.isExactColorString("indexCommandsItem.name", "&6Commands")
         && (missingLegacyCommandsLore || this.isExactColorList("indexCommandsItem.lore", List.of("Click to go!")))) {
         this.fileConfiguration.set("indexCommandsItem.name", "&6&lQuick Actions");
         this.fileConfiguration
            .set("indexCommandsItem.lore", List.of("&7Open the Hub, teleport to spawn", "&7or share your held Elite item.", "", "&aClick to view!"));
      }

      if (this.isExactString("gearThreatItem.material", Material.TARGET.name())
         && this.isExactColorString("gearThreatItem.name", "&cThreat Level: $threat")
         && this.isExactColorList(
            "gearThreatItem.lore",
            List.of("&fThis determines the level of the", "&fElite Mobs that spawns near you", "&fTakes armor, weapon in hand, guild", "&ftier into account.")
         )) {
         this.fileConfiguration
            .set(
               "gearThreatItem.lore",
               List.of(
                  "&fThis is your combat level and determines",
                  "&fthe level of Elite Mobs spawning nearby.",
                  "&fIt averages your armor skill and your",
                  "&ftwo highest weapon skills."
               )
            );
      }

      if (this.isExactString("gearDamageItem.material", Material.DIAMOND_SWORD.name())
         && this.isExactColorString("gearDamageItem.name", "&4Damage: $damage")
         && this.isExactColorList("gearDamageItem.lore", List.of("&fBase damage dealt to Elites.", "&fBased on the level of your weapon!"))) {
         this.fileConfiguration.set("gearDamageItem.name", "&c&lWeapon Power: &fLv. $weaponLevel");
         this.fileConfiguration
            .set(
               "gearDamageItem.lore",
               List.of(
                  "&7$weaponSkill skill: &fLv. $weaponSkillLevel",
                  "&7Equal-level offense: &f$offenseMatch%",
                  "",
                  "&8Equipment contribution only; active skills",
                  "&8and temporary effects apply during combat."
               )
            );
      }

      if (this.isExactString("gearArmorItem.material", Material.SHIELD.name())
         && this.isExactColorString("gearArmorItem.name", "&2Defense: $defense")
         && this.isExactColorList("gearArmorItem.lore", List.of("&fBase damage reduction from Elites.", "&fBased on the average level of your armor!"))) {
         this.fileConfiguration.set("gearArmorItem.name", "&b&lArmor Rating: &f$armorLevel");
         this.fileConfiguration
            .set(
               "gearArmorItem.lore",
               List.of(
                  "&7Armor skill: &fLv. $armorSkillLevel",
                  "&7Equal-level reduction: &f$defenseMatch%",
                  "",
                  "&8Includes equipped armor and relevant",
                  "&8protection enchantments."
               )
            );
      }

      if (this.isExactString("gearThreatItem.material", Material.TARGET.name())
         && this.isExactColorString("gearThreatItem.name", "&cThreat Level: $threat")
         && this.isExactColorList(
            "gearThreatItem.lore",
            List.of(
               "&fThis is your combat level and determines",
               "&fthe level of Elite Mobs spawning nearby.",
               "&fIt averages your armor skill and your",
               "&ftwo highest weapon skills."
            )
         )) {
         this.fileConfiguration.set("gearThreatItem.name", "&e&lCombat Level: &f$combatLevel");
         this.fileConfiguration
            .set(
               "gearThreatItem.lore",
               List.of("&7Average of your armor skill and your", "&7two strongest weapon skills.", "", "&8Nearby natural Elites scale from this level.")
            );
      }
   }

   private void migrateWipVisualDefaults() {
      this.migrateExactColorString("dialog.titlePlayerStatus", "Player Status Menu", "<g:#8B0000:#CC4400:#DAA520>EliteMobs</g> &7Menu");
      this.migrateExactColorString("dialog.titleStats", "&6Adventure Record", "<g:#B8860B:#F0C040>★ Adventure Record</g>");
      this.migrateExactColorString("dialog.titleGear", "Gear", "<g:#A63D2F:#E97932>⚔ Combat Gear</g>");
      this.migrateExactColorString("dialog.titleTeleports", "Teleports", "<g:#355CA8:#5FA9E8>↔ Teleports</g>");
      this.migrateExactColorString("dialog.titleCommands", "&6Quick Actions", "<g:#267A78:#58B8A9>⚡ Quick Actions</g>");
      this.migrateExactColorString("dialog.titleQuests", "Quests", "<g:#2E7D4F:#69C56F>✉ Quests</g>");
      this.migrateExactColorString("dialog.titleBossTracking", "Boss Tracking", "<g:#7A1F2B:#C2414A>☠ Boss Tracker</g>");
      this.migrateExactColorString("dialog.titleSkills", "Skills", "<g:#6D3AA8:#A855F7>⚗ Skills</g>");
      this.migrateExactColorString("dialog.titleParty", "&6Party", "<g:#A04468:#E07A9A>♥ Party</g>");
      this.migrateExactColorString("indexChestMenuName", "&2EliteMobs Index", "<g:#8B0000:#CC4400:#DAA520>EliteMobs</g> &7Menu");
      this.migrateExactColorString("statsChestMenuName", "&6Adventure Record", "<g:#B8860B:#F0C040>★ Adventure Record</g>");
      this.migrateExactColorString("gearChestMenuName", "&2EliteMobs Gear", "<g:#A63D2F:#E97932>⚔ Combat Gear</g>");
      this.migrateExactColorString("teleportChestMenuName", "&2EliteMobs Teleports", "<g:#355CA8:#5FA9E8>↔ Teleports</g>");
      this.migrateExactColorString("commandsChestMenuName", "&2EliteMobs Quick Actions", "<g:#267A78:#58B8A9>⚡ Quick Actions</g>");
      this.migrateExactColorString("bossTrackerChestMenuName", "&2EliteMobs Boss Tracking", "<g:#7A1F2B:#C2414A>☠ Boss Tracker</g>");
      this.migrateExactColorString("dialog.partyInviteTitle", "&6Invite a Player", "<g:#A04468:#E07A9A>✉ Invite a Player</g>");
      this.migrateExactColorString("dialog.partyCreateButton", "&aCreate a Party", "<g:#2E7D4F:#69C56F>+ Create a Party</g>");
      this.migrateExactColorString("dialog.partyInviteButton", "&aInvite a Player", "<g:#267A78:#58B8A9>✉ Invite a Player</g>");
      this.migrateExactColorString("dialog.partyLeaveButton", "&cLeave Party", "<g:#7A1F2B:#C2414A>✘ Leave Party</g>");
      this.migrateExactColorString("dialog.partyInvitePlayerButton", "&aInvite $player", "<g:#267A78:#58B8A9>✉ Invite</g> &f$player");
      this.migrateExactColorList(
         "landingOverviewLines",
         List.of("&2Coins: &a$money &8| &cThreat: &f$threat", "&dActive Quests: &f$activeQuests &8| &bScore: &f$score"),
         List.of(
            "<g:#B8860B:#F0C040>Coins</g>: &f$money &8• <g:#A63D2F:#E97932>Combat Level</g>: &f$combatLevel",
            "<g:#2E7D4F:#69C56F>Active Quests</g>: &f$activeQuests &8• <g:#355CA8:#5FA9E8>Score</g>: &f$score"
         )
      );
      this.migrateExactColorList(
         "dialog.gearSummaryLines",
         List.of(
            "&6&lCombat Profile",
            "&eCombat Level: &f$combatLevel",
            "&cWeapon Power: &fLv. $weaponLevel &8| &c$weaponSkill: &fLv. $weaponSkillLevel",
            "&cEqual-level offense: &f$offenseMatch%",
            "&bArmor Rating: &f$armorLevel &8| &bArmor Skill: &fLv. $armorSkillLevel",
            "&bEqual-level reduction: &f$defenseMatch%",
            "&8Equipment matchup only; skills and temporary effects apply in combat."
         ),
         List.of(
            "<g:#A63D2F:#E97932>⚔ Equipment Matchup</g>",
            "<g:#B8860B:#F0C040>Combat Level</g>: &f$combatLevel",
            "<g:#A63D2F:#E97932>Held Weapon</g>: &fLv. $weaponLevel &8• &7$weaponSkill: &fLv. $weaponSkillLevel",
            "<g:#A63D2F:#E97932>Weapon factor vs Lv.</g> &f$referenceLevel: &f×$weaponFactor",
            "<g:#A04468:#E07A9A>Critical chance</g>: &f$critChance% &8• &7Equipment bonus: &f+$enchantmentBonus%",
            "&7Selected weapon perks: &f$weaponPerks",
            "<g:#355CA8:#5FA9E8>Melee armor</g>: &f$armorLevel &8• &7Armor Skill: &fLv. $armorSkillLevel",
            "<g:#355CA8:#5FA9E8>Melee reduction vs Lv.</g> &f$referenceLevel: &f$defenseMatch%",
            "<g:#2E7D4F:#69C56F>Health</g>: &f$health&7/&f$maxHealth",
            "&7Selected armor perks: &f$armorPerks",
            "<g:#B8860B:#F0C040>Threat generated</g>: &f×$threatMultiplier",
            "&8Equipment matchup only; skills and temporary effects apply in combat."
         )
      );
      this.migrateExactColorString("indexTexts6", "&bp. $statsPage &8- &6Stats", "&bp. $statsPage &8- <g:#B8860B:#F0C040>★ Adventure Record</g>");
      this.migrateExactColorString("indexTexts7", "&bp. $gearPage &8- &6Gear", "&bp. $gearPage &8- <g:#A63D2F:#E97932>⚔ Combat Gear</g>");
      this.migrateExactColorString("indexTexts8", "&bp. $teleportsPage &8- &6Teleports", "&bp. $teleportsPage &8- <g:#355CA8:#5FA9E8>↔ Teleports</g>");
      this.migrateExactColorString("indexTexts9", "&bp. $commandsPage &8- &6Quick Actions", "&bp. $commandsPage &8- <g:#267A78:#58B8A9>⚡ Quick Actions</g>");
      this.migrateExactColorString("indexTexts10", "&bp. $questsPage &8- &6Quest Tracking", "&bp. $questsPage &8- <g:#2E7D4F:#69C56F>✉ Quests</g>");
      this.migrateExactColorString(
         "indexTexts11", "&bp. $bossTrackingPage &8- &6Boss Tracking", "&bp. $bossTrackingPage &8- <g:#7A1F2B:#C2414A>☠ Boss Tracker</g>"
      );
      this.migrateExactColorString("indexTexts12", "&bp. $skillsPage &8- &6Skills", "&bp. $skillsPage &8- <g:#6D3AA8:#A855F7>⚗ Skills</g>");
      this.migrateExactColorString("indexTexts13", "&6Party Controls", "<g:#A04468:#E07A9A>♥ Party</g>");
      this.migrateExactColorString("indexStatsItem.name", "&6Stats", "<g:#B8860B:#F0C040>★ Adventure Record</g>");
      this.migrateExactColorString("indexGearItem.name", "&6Gear", "<g:#A63D2F:#E97932>⚔ Combat Gear</g>");
      this.migrateExactColorString("indexTeleportsItem.name", "&6Teleports", "<g:#355CA8:#5FA9E8>↔ Teleports</g>");
      this.migrateExactColorString("indexCommandsItem.name", "&6&lQuick Actions", "<g:#267A78:#58B8A9>⚡ Quick Actions</g>");
      this.migrateExactColorString("indexQuestTrackingItem.name", "&6Quest Tracking", "<g:#2E7D4F:#69C56F>✉ Quests</g>");
      this.migrateExactColorString("indexBossTrackingItem.name", "&6Boss Tracking", "<g:#7A1F2B:#C2414A>☠ Boss Tracker</g>");
      this.migrateExactColorString("indexSkillsItem.name", "&5&lSkills", "<g:#6D3AA8:#A855F7>⚗ Skills</g>");
      this.migrateExactColorString("indexPartyItem.name", "&6&lParty", "<g:#A04468:#E07A9A>♥ Party</g>");
      this.migrateExactColorString("gearDamageItem.name", "&c&lWeapon Power: &fLv. $weaponLevel", "<g:#A63D2F:#E97932>⚔ Held Weapon</g>: &fLv. $weaponLevel");
      this.migrateExactColorString("gearArmorItem.name", "&b&lArmor Rating: &f$armorLevel", "<g:#355CA8:#5FA9E8>⛨ Armor Rating</g>: &f$armorLevel");
      this.migrateExactColorString("gearThreatItem.name", "&e&lCombat Level: &f$combatLevel", "<g:#B8860B:#F0C040>★ Combat Level</g>: &f$combatLevel");
      this.migrateExactColorList(
         "gearDamageItem.lore",
         List.of(
            "&7$weaponSkill skill: &fLv. $weaponSkillLevel",
            "&7Equal-level offense: &f$offenseMatch%",
            "",
            "&8Equipment contribution only; active skills",
            "&8and temporary effects apply during combat."
         ),
         List.of(
            "&7$weaponSkill skill: &fLv. $weaponSkillLevel",
            "&7Weapon factor vs Lv. $referenceLevel: &f×$weaponFactor",
            "&7Critical chance: &f$critChance%",
            "&7Equipment damage bonus: &f+$enchantmentBonus%",
            "",
            "&7Selected weapon perks:",
            "&f$weaponPerks"
         )
      );
      this.migrateExactColorList(
         "gearArmorItem.lore",
         List.of(
            "&7Armor skill: &fLv. $armorSkillLevel",
            "&7Equal-level reduction: &f$defenseMatch%",
            "",
            "&8Includes equipped armor and relevant",
            "&8protection enchantments."
         ),
         List.of(
            "&7Armor skill: &fLv. $armorSkillLevel",
            "&7Melee reduction vs Lv. $referenceLevel: &f$defenseMatch%",
            "&7Health: &f$health&7/&f$maxHealth",
            "",
            "&7Selected armor perks:",
            "&f$armorPerks"
         )
      );
      this.migrateExactColorList(
         "gearThreatItem.lore",
         List.of("&7Average of your armor skill and your", "&7two strongest weapon skills.", "", "&8Nearby natural Elites scale from this level."),
         List.of(
            "&7Progression baseline: &fLv. $combatLevel",
            "&7Threat generated: &f×$threatMultiplier",
            "",
            "&8Natural scaling may also use distance,",
            "&8nearby players, party size and randomness."
         )
      );
      this.migrateExactColorString("gearText10", "{dmg : $damage}    {armr: $armor}", "Combat Lv. $combatLevel | Crit $critChance%");
      this.migrateExactString(
         "gearHover10",
         "{Base damage dealt to Elite Mobs}{Damage reduction from Elite Mobs}",
         "Weapon: $weaponSkill Lv. $weaponSkillLevel\nGear Lv. $weaponLevel vs Elite Lv. $referenceLevel"
      );
      this.migrateExactColorString("gearText11", "Threat level: $threat", "Weapon ×$weaponFactor | Melee armor $defenseMatch%");
      this.migrateExactString(
         "gearHover11",
         "This is your combat level and determines the\nlevel of Elite Mobs that spawn near you.\nIt averages your armor skill and your\ntwo highest weapon skills.\n",
         "Melee armor rating: $armorLevel\nArmor skill: Lv. $armorSkillLevel\nEquipment damage bonus: +$enchantmentBonus%"
      );
      this.migrateExactColorString("gearText12", "", "Threat ×$threatMultiplier | HP $health/$maxHealth");
      this.migrateExactString("gearHover12", "", "Selected weapon perks: $weaponPerks\nSelected armor perks: $armorPerks");
      this.migrateExactColorString("statsText1", "&6&lAdventure Record", "<g:#B8860B:#F0C040>★ Adventure Record</g>");
      this.migrateExactColorString("statsText4", "&6Server Rank: &f$rank &8/ &7$ranktotal", "<g:#B8860B:#F0C040>★ Server Rank</g>: &f$rank &8/ &7$ranktotal");
      this.migrateExactColorString("statsText5", "&9Adventure Score: &f$score", "<g:#355CA8:#5FA9E8>Adventure Score</g>: &f$score");
      this.migrateExactColorString("statsText6", "&dDungeons Cleared: &f$dungeons", "<g:#6D3AA8:#A855F7>Dungeons Cleared</g>: &f$dungeons");
      this.migrateExactColorString("statsText7", "&cCombat Level: &f$combat", "<g:#A63D2F:#E97932>Combat Level</g>: &f$combat");
      this.migrateExactColorString("statsText9", "&aElite Kills: &f$kills", "<g:#2E7D4F:#69C56F>Elite Kills</g>: &f$kills");
      this.migrateExactColorString("statsText10", "&bHighest Elite Level: &f$highestkill", "<g:#267A78:#58B8A9>Highest Elite Level</g>: &f$highestkill");
      this.migrateExactColorString("statsText11", "&eQuests Completed: &f$quests", "<g:#B8860B:#F0C040>Quests Completed</g>: &f$quests");
      this.migrateExactColorString(
         "statsRankItem.name", "&6&lServer Rank &8• &f$rank &7/ $ranktotal", "<g:#B8860B:#F0C040>★ Server Rank</g> &8• &f$rank &7/ $ranktotal"
      );
      this.migrateExactColorString(
         "statsDungeonsCompletedItem.name", "&d&lDungeons Cleared &8• &f$dungeons", "<g:#6D3AA8:#A855F7>Dungeons Cleared</g> &8• &f$dungeons"
      );
      this.migrateExactColorString("statsCombatLevelItem.name", "&c&lCombat Level &8• &f$combat", "<g:#A63D2F:#E97932>Combat Level</g> &8• &f$combat");
      this.migrateExactColorString("statsEliteKillsItem.name", "&a&lElite Kills &8• &f$kills", "<g:#2E7D4F:#69C56F>Elite Kills</g> &8• &f$kills");
      this.migrateExactColorString(
         "statsMaxEliteLevelKilledItem.name", "&b&lPeak Elite Level &8• &f$maxKill", "<g:#267A78:#58B8A9>Peak Elite Level</g> &8• &f$maxKill"
      );
      this.migrateExactColorString(
         "statsQuestsCompletedItem.name", "&e&lQuests Completed &8• &f$questsCompleted", "<g:#B8860B:#F0C040>Quests Completed</g> &8• &f$questsCompleted"
      );
      this.migrateExactColorString("statsScoreItem.name", "&9&lAdventure Score &8• &f$score", "<g:#355CA8:#5FA9E8>Adventure Score</g> &8• &f$score");
      this.migrateExactColorString("skillsItemDisplayName", "&5&lSkills", "<g:#6D3AA8:#A855F7>⚗ Skills</g>");
      this.migrateExactColorString("skillsPageHeader", "&5&lYour Skills", "<g:#6D3AA8:#A855F7>⚗ Your Skills</g>");
   }

   private void migrateLegacyStatsDefaults() {
      List<String> legacyText = List.of(
         "&m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯",
         "&5&lPlayer Stats:",
         "&m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯",
         "",
         "&2Money: &a$money",
         "",
         "&6Guild Tier: &3$guildtier",
         "&4Elite Kills: &c$kills",
         "&4Max Lvl Killed: &c$highestkill",
         "&4Elite Deaths: &c$deaths",
         "&5Quests Completed: &d$quests",
         "",
         "&bScore: &3$score"
      );
      List<String> legacyHover = List.of(
         "",
         "",
         "",
         "",
         "Kill Elite Mobs to loot currency or\nsell their drops in /em shop or\ncomplete quests!",
         "",
         "Prestige Tier and Guild Rank:\nGuild Rank determines how good your loot can be, sets your bonus from the Prestige Tier, among other things. The Prestige Tier unlocks extremely powerful rewards, like increased max health, chance to dodge/crit, increased currency rewards and more! You can unlock Guild Ranks and Prestige Tiers at /ag!\n⚜ = prestige rank, ✧ = guild rank!",
         "Amount of Elite Mobs killed.",
         "Level of the highest Elite Mob killed.\nElite Mob levels are based on the tier\nof your gear! Higher tiers, higher\nElite Mob levels!\nNote: only non-exploity kills get counted!",
         "Times killed by Elite Mobs.",
         "Amount of EliteMobs quests completed.\nYou can accept quests by talking to NPCs!",
         "",
         "Your EliteMobs score. It goes up\nwhen you kill and elite mob,\nand it goes down when you die\nto an elite. Higher level\nelites give more score."
      );
      boolean exactLegacyText = true;

      for (int i = 0; i < legacyText.size(); i++) {
         if (!this.isExactColorString("statsText" + i, legacyText.get(i)) || !this.isExactString("statsHover" + i, legacyHover.get(i))) {
            exactLegacyText = false;
            break;
         }
      }

      if (exactLegacyText) {
         List<String> text = List.of(
            "&6&m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯",
            "&6&lAdventure Record",
            "&6&m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯",
            "",
            "&6Server Rank: &f$rank &8/ &7$ranktotal",
            "&9Adventure Score: &f$score",
            "&dDungeons Cleared: &f$dungeons",
            "&cCombat Level: &f$combat",
            "",
            "&aElite Kills: &f$kills",
            "&bHighest Elite Level: &f$highestkill",
            "&eQuests Completed: &f$quests",
            ""
         );
         List<String> hover = List.of(
            "",
            "",
            "",
            "",
            "Your server-wide position based on Adventure Score.",
            "Higher-level Elite kills award more score. Elite deaths reduce it.",
            "Successful dungeon clears. Entering or abandoning a run does not count.",
            "Your overall level from armor and your two strongest weapon skills.",
            "",
            "Confirmed non-exploit Elite Mob kills.",
            "The highest-level Elite Mob you have defeated legitimately.",
            "EliteMobs quests you have fully completed.",
            ""
         );

         for (int ix = 0; ix < text.size(); ix++) {
            this.fileConfiguration.set("statsText" + ix, text.get(ix));
            this.fileConfiguration.set("statsHover" + ix, hover.get(ix));
         }
      }

      this.migrateExactString("statsChestMenuName", "&2EliteMobs Stats", "&6Adventure Record");
      this.migrateExactString("dialog.titleStats", "Stats", "&6Adventure Record");
      this.migrateLegacyStatsSlots();
      this.migrateExactItem(
         "statsEliteKillsItem",
         Material.DIAMOND_SWORD,
         "&4Elite Kills: &c$kills",
         List.of("&fAmount of EliteMobs killed."),
         Material.DIAMOND_SWORD,
         "&a&lElite Kills &8• &f$kills",
         List.of("&7Confirmed non-exploit Elite Mob kills.")
      );
      this.migrateExactItem(
         "statsMaxEliteLevelKilledItem",
         Material.GOLDEN_SWORD,
         "&4Max Lvl Killed: &c$maxKill",
         List.of(
            "&fElite Mob levels are based on the tier",
            "&fof your gear! Higher tiers, higher",
            "&fElite Mob levels!\n",
            "&eNote: only non-exploity kills get counted!"
         ),
         Material.BEACON,
         "&b&lPeak Elite Level &8• &f$maxKill",
         List.of("&7Your highest legitimate Elite Mob kill.")
      );
      this.migrateExactItem(
         "statsQuestsCompletedItem",
         Material.LECTERN,
         "&5Quests Completed: &d$questsCompleted",
         List.of("&fAmount of EliteMobs quests completed.", "&fYou can accept quests by talking to NPCs!"),
         Material.WRITABLE_BOOK,
         "&e&lQuests Completed &8• &f$questsCompleted",
         List.of("&7EliteMobs quests fully completed.")
      );
      this.migrateExactItem(
         "statsScoreItem",
         Material.ITEM_FRAME,
         "&3Score: &b$score",
         List.of(
            "&fYour EliteMobs score. It goes up",
            "&fwhen you kill and elite mob,",
            "&fand it goes down when you die",
            "&fto an elite. Higher level",
            "&felites give more score."
         ),
         Material.EXPERIENCE_BOTTLE,
         "&9&lAdventure Score &8• &f$score",
         List.of("&7Higher-level Elite kills award more score.", "&7Elite deaths reduce it.")
      );
   }

   private void migrateLegacyStatsSlots() {
      Map<String, Integer> legacy = Map.of("statsEliteKillsSlot", 12, "statsMaxEliteLevelKilledSlot", 13, "statsQuestsCompletedSlot", 15, "statsScoreSlot", 16);

      for (Entry<String, Integer> entry : legacy.entrySet()) {
         if (!this.fileConfiguration.isInt(entry.getKey()) || this.fileConfiguration.getInt(entry.getKey()) != entry.getValue()) {
            return;
         }
      }

      Map<String, Integer> proposed = Map.of(
         "statsEliteKillsSlot", 14, "statsMaxEliteLevelKilledSlot", 15, "statsQuestsCompletedSlot", 16, "statsScoreSlot", 11
      );
      HashSet<Integer> occupied = new HashSet<>(proposed.values());
      Map<String, Integer> unchanged = Map.of("statsRankSlot", 10, "statsDungeonsCompletedSlot", 12, "statsCombatLevelSlot", 13);

      for (Entry<String, Integer> entryx : unchanged.entrySet()) {
         if (this.fileConfiguration.isSet(entryx.getKey()) && !this.fileConfiguration.isInt(entryx.getKey())) {
            return;
         }

         int slot = this.fileConfiguration.getInt(entryx.getKey(), entryx.getValue());
         if (slot < 0 || slot >= 27 || !occupied.add(slot)) {
            return;
         }
      }

      proposed.forEach(this.fileConfiguration::set);
   }

   private void migrateExactItem(
      String path,
      Material legacyMaterial,
      String legacyName,
      List<String> legacyLore,
      Material replacementMaterial,
      String replacementName,
      List<String> replacementLore
   ) {
      if (this.isExactString(path + ".material", legacyMaterial.name())
         && this.isExactColorString(path + ".name", legacyName)
         && this.isExactColorList(path + ".lore", legacyLore)) {
         this.fileConfiguration.set(path + ".material", replacementMaterial.name());
         this.fileConfiguration.set(path + ".name", replacementName);
         this.fileConfiguration.set(path + ".lore", replacementLore);
      }
   }

   private void migrateLegacySpawnAction() {
      if (this.isExactString("commandsText5", "") && this.isExactString("commandsHover5", "") && this.isExactString("commandsCommand5", "")) {
         this.fileConfiguration.set("commandsText5", "&5/em spawntp &7- Spawn");
         this.fileConfiguration.set("commandsHover5", "CLICK TO USE\nTeleport to the server spawn!");
         this.fileConfiguration.set("commandsCommand5", "/em spawntp");
      }
   }

   private void migrateExactString(String path, String legacyValue, String replacement) {
      if (this.isExactString(path, legacyValue)) {
         this.fileConfiguration.set(path, replacement);
      }
   }

   private void migrateExactColorString(String path, String legacyValue, String replacement) {
      if (this.isExactColorString(path, legacyValue)) {
         this.fileConfiguration.set(path, replacement);
      }
   }

   private boolean isExactString(String path, String value) {
      return this.fileConfiguration.isSet(path) && value.equals(this.fileConfiguration.getString(path));
   }

   private boolean isExactColorString(String path, String value) {
      if (!this.fileConfiguration.isSet(path)) {
         return false;
      } else {
         String configuredValue = this.fileConfiguration.getString(path);
         return configuredValue != null && this.normalizeSectionColorCodes(configuredValue).equals(this.normalizeSectionColorCodes(value));
      }
   }

   private boolean isExactColorList(String path, List<String> values) {
      if (!this.fileConfiguration.isSet(path)) {
         return false;
      } else {
         List<String> configuredValues = this.fileConfiguration.getStringList(path);
         if (configuredValues.size() != values.size()) {
            return false;
         } else {
            for (int i = 0; i < values.size(); i++) {
               if (!this.normalizeSectionColorCodes(configuredValues.get(i)).equals(this.normalizeSectionColorCodes(values.get(i)))) {
                  return false;
               }
            }

            return true;
         }
      }
   }

   private void migrateExactColorList(String path, List<String> legacyValues, List<String> replacementValues) {
      if (this.isExactColorList(path, legacyValues)) {
         this.fileConfiguration.set(path, replacementValues);
      }
   }

   private String normalizeSectionColorCodes(String value) {
      return value.replace('§', '&');
   }

   @Override
   public void processAdditionalFields() {
      this.migrateLegacyQuickActionDefaults();
      this.migrateWipVisualDefaults();
      doStatsPage = ConfigurationEngine.setBoolean(this.fileConfiguration, "doStatsPage", true);
      doGearPage = ConfigurationEngine.setBoolean(this.fileConfiguration, "doGearPage", true);
      doTeleportsPage = ConfigurationEngine.setBoolean(this.fileConfiguration, "doTeleportsPage", true);
      doCommandsPage = ConfigurationEngine.setBoolean(this.fileConfiguration, "doCommandsPage", true);
      doQuestTrackingPage = ConfigurationEngine.setBoolean(this.fileConfiguration, "doQuestTrackingPage", true);
      doBossTrackingPage = ConfigurationEngine.setBoolean(this.fileConfiguration, "doBossTrackingPage", true);
      landingOverviewLines = ConfigurationEngine.setList(
         List.of(
            "Compact player overview shown on the /em dialog and inventory header.",
            "Available placeholders: $money, $combatLevel, $activeQuests and $score.",
            "$threat remains available as a legacy alias for $combatLevel."
         ),
         this.file,
         this.fileConfiguration,
         "landingOverviewLines",
         List.of(
            "<g:#B8860B:#F0C040>Coins</g>: &f$money &8• <g:#A63D2F:#E97932>Combat Level</g>: &f$combatLevel",
            "<g:#2E7D4F:#69C56F>Active Quests</g>: &f$activeQuests &8• <g:#355CA8:#5FA9E8>Score</g>: &f$score"
         ),
         true
      );
      dialogGearSummaryLines = ConfigurationEngine.setList(
         List.of(
            "Modern combat summary shown in the Gear dialog.",
            "Placeholders: $combatLevel, $referenceLevel, $weaponLevel, $weaponSkill,",
            "$weaponSkillLevel, $weaponFactor, $critChance, $enchantmentBonus, $weaponPerks,",
            "$armorLevel, $armorSkillLevel, $defenseMatch, $health, $maxHealth, $armorPerks",
            "and $threatMultiplier."
         ),
         this.file,
         this.fileConfiguration,
         "dialog.gearSummaryLines",
         List.of(
            "<g:#A63D2F:#E97932>⚔ Equipment Matchup</g>",
            "<g:#B8860B:#F0C040>Combat Level</g>: &f$combatLevel",
            "<g:#A63D2F:#E97932>Held Weapon</g>: &fLv. $weaponLevel &8• &7$weaponSkill: &fLv. $weaponSkillLevel",
            "<g:#A63D2F:#E97932>Weapon factor vs Lv.</g> &f$referenceLevel: &f×$weaponFactor",
            "<g:#A04468:#E07A9A>Critical chance</g>: &f$critChance% &8• &7Equipment bonus: &f+$enchantmentBonus%",
            "&7Selected weapon perks: &f$weaponPerks",
            "<g:#355CA8:#5FA9E8>Melee armor</g>: &f$armorLevel &8• &7Armor Skill: &fLv. $armorSkillLevel",
            "<g:#355CA8:#5FA9E8>Melee reduction vs Lv.</g> &f$referenceLevel: &f$defenseMatch%",
            "<g:#2E7D4F:#69C56F>Health</g>: &f$health&7/&f$maxHealth",
            "&7Selected armor perks: &f$armorPerks",
            "<g:#B8860B:#F0C040>Threat generated</g>: &f×$threatMultiplier",
            "&8Equipment matchup only; skills and temporary effects apply in combat."
         ),
         true
      );
      gearUnarmedLabel = ConfigurationEngine.setString(
         List.of("Weapon skill label shown when no recognized weapon is held."), this.file, this.fileConfiguration, "dialog.gearUnarmedLabel", "Unarmed", true
      );
      gearNoSelectedPerksLabel = ConfigurationEngine.setString(
         List.of("Text shown in the Combat Gear profile when no applicable perks are selected."),
         this.file,
         this.fileConfiguration,
         "dialog.gearNoSelectedPerksLabel",
         "None selected",
         true
      );
      dialogGearHelmetLabel = ConfigurationEngine.setString(this.file, this.fileConfiguration, "dialog.gearHelmetLabel", "Helmet", true);
      dialogGearChestplateLabel = ConfigurationEngine.setString(this.file, this.fileConfiguration, "dialog.gearChestplateLabel", "Chestplate", true);
      dialogGearLeggingsLabel = ConfigurationEngine.setString(this.file, this.fileConfiguration, "dialog.gearLeggingsLabel", "Leggings", true);
      dialogGearBootsLabel = ConfigurationEngine.setString(this.file, this.fileConfiguration, "dialog.gearBootsLabel", "Boots", true);
      dialogGearMainHandLabel = ConfigurationEngine.setString(this.file, this.fileConfiguration, "dialog.gearMainHandLabel", "Main Hand", true);
      dialogGearOffHandLabel = ConfigurationEngine.setString(this.file, this.fileConfiguration, "dialog.gearOffHandLabel", "Off Hand", true);
      dialogQuestFallbackFormat = ConfigurationEngine.setString(
         List.of("Fallback quest label used when a quest has no configured display name.", "Available placeholder: $id."),
         this.file,
         this.fileConfiguration,
         "dialog.questFallbackFormat",
         "Quest $id",
         true
      );
      indexLineCreator(0, "&0&m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯", "", "", this.fileConfiguration, this.file);
      indexLineCreator(
         1,
         "&5&l/ag &7- &6EliteMobs Hub",
         "CLICK TO USE\nThe place where you can find\nNPCs that give quests, buy and\nsell items, give advice and more!",
         "/ag",
         this.fileConfiguration,
         this.file
      );
      indexLineCreator(2, "&0&m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯", "", "", this.fileConfiguration, this.file);
      indexLineCreator(3, "", "", "", this.fileConfiguration, this.file);
      indexLineCreator(4, "&6&lIndex", "", "", this.fileConfiguration, this.file);
      indexLineCreator(5, "", "", "", this.fileConfiguration, this.file);
      indexLineCreator(6, "&bp. $statsPage &8- <g:#B8860B:#F0C040>★ Adventure Record</g>", "Click to go!", "$statsPage", this.fileConfiguration, this.file);
      indexLineCreator(7, "&bp. $gearPage &8- <g:#A63D2F:#E97932>⚔ Combat Gear</g>", "Click to go!", "$gearPage", this.fileConfiguration, this.file);
      indexLineCreator(8, "&bp. $teleportsPage &8- <g:#355CA8:#5FA9E8>↔ Teleports</g>", "Click to go!", "$teleportsPage", this.fileConfiguration, this.file);
      indexLineCreator(
         9,
         "&bp. $commandsPage &8- <g:#267A78:#58B8A9>⚡ Quick Actions</g>",
         "Open useful EliteMobs actions",
         "$commandsPage",
         this.fileConfiguration,
         this.file
      );
      indexLineCreator(10, "&bp. $questsPage &8- <g:#2E7D4F:#69C56F>✉ Quests</g>", "Click to go!", "$questsPage", this.fileConfiguration, this.file);
      indexLineCreator(
         11, "&bp. $bossTrackingPage &8- <g:#7A1F2B:#C2414A>☠ Boss Tracker</g>", "Click to go!", "$bossTrackingPage", this.fileConfiguration, this.file
      );
      indexLineCreator(12, "&bp. $skillsPage &8- <g:#6D3AA8:#A855F7>⚗ Skills</g>", "Click to go!", "$skillsPage", this.fileConfiguration, this.file);
      indexLineCreator(13, "<g:#A04468:#E07A9A>♥ Party</g>", "Create, invite, or leave a party", "/em party menu", this.fileConfiguration, this.file);
      statsLineCreator(0, "&6&m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯", "", "", this.fileConfiguration, this.file);
      statsLineCreator(1, "<g:#B8860B:#F0C040>★ Adventure Record</g>", "", "", this.fileConfiguration, this.file);
      statsLineCreator(2, "&6&m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯", "", "", this.fileConfiguration, this.file);
      statsLineCreator(3, "", "", "", this.fileConfiguration, this.file);
      statsLineCreator(
         4,
         "<g:#B8860B:#F0C040>★ Server Rank</g>: &f$rank &8/ &7$ranktotal",
         "Your server-wide position based on Adventure Score.",
         "",
         this.fileConfiguration,
         this.file
      );
      statsLineCreator(
         5,
         "<g:#355CA8:#5FA9E8>Adventure Score</g>: &f$score",
         "Higher-level Elite kills award more score. Elite deaths reduce it.",
         "",
         this.fileConfiguration,
         this.file
      );
      statsLineCreator(
         6,
         "<g:#6D3AA8:#A855F7>Dungeons Cleared</g>: &f$dungeons",
         "Successful dungeon clears. Entering or abandoning a run does not count.",
         "",
         this.fileConfiguration,
         this.file
      );
      statsLineCreator(
         7,
         "<g:#A63D2F:#E97932>Combat Level</g>: &f$combat",
         "Your overall level from armor and your two strongest weapon skills.",
         "",
         this.fileConfiguration,
         this.file
      );
      statsLineCreator(8, "", "", "", this.fileConfiguration, this.file);
      statsLineCreator(9, "<g:#2E7D4F:#69C56F>Elite Kills</g>: &f$kills", "Confirmed non-exploit Elite Mob kills.", "", this.fileConfiguration, this.file);
      statsLineCreator(
         10,
         "<g:#267A78:#58B8A9>Highest Elite Level</g>: &f$highestkill",
         "The highest-level Elite Mob you have defeated legitimately.",
         "",
         this.fileConfiguration,
         this.file
      );
      statsLineCreator(
         11, "<g:#B8860B:#F0C040>Quests Completed</g>: &f$quests", "EliteMobs quests you have fully completed.", "", this.fileConfiguration, this.file
      );
      statsLineCreator(12, "", "", "", this.fileConfiguration, this.file);
      gearLineCreator(0, "&m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯", "", "", this.fileConfiguration, this.file);
      gearLineCreator(1, "&7&lArmor & Weapons:", "", "", this.fileConfiguration, this.file);
      gearLineCreator(2, "&m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯", "", "", this.fileConfiguration, this.file);
      gearLineCreator(3, "&8&lGear Tiers:", "", "", this.fileConfiguration, this.file);
      gearLineCreator(4, "          ☠ - $helmettier", "$helmet", "", this.fileConfiguration, this.file);
      gearLineCreator(5, "          ▼ - $chestplatetier", "$chestplate", "", this.fileConfiguration, this.file);
      gearLineCreator(6, "          Π - $leggingstier", "$leggings", "", this.fileConfiguration, this.file);
      gearLineCreator(7, "          ╯╰ - $bootstier", "$boots", "", this.fileConfiguration, this.file);
      gearLineCreator(8, "{⚔ - $mainhandtier}    {⛨ - $offhandtier}", "{$mainhand}{$offhand}", "", this.fileConfiguration, this.file);
      gearLineCreator(9, "", "", "", this.fileConfiguration, this.file);
      gearLineCreator(
         10,
         "Combat Lv. $combatLevel | Crit $critChance%",
         "Weapon: $weaponSkill Lv. $weaponSkillLevel\nGear Lv. $weaponLevel vs Elite Lv. $referenceLevel",
         "",
         this.fileConfiguration,
         this.file
      );
      gearLineCreator(
         11,
         "Weapon ×$weaponFactor | Melee armor $defenseMatch%",
         "Melee armor rating: $armorLevel\nArmor skill: Lv. $armorSkillLevel\nEquipment damage bonus: +$enchantmentBonus%",
         "",
         this.fileConfiguration,
         this.file
      );
      gearLineCreator(
         12,
         "Threat ×$threatMultiplier | HP $health/$maxHealth",
         "Selected weapon perks: $weaponPerks\nSelected armor perks: $armorPerks",
         "",
         this.fileConfiguration,
         this.file
      );
      teleportLineCreator(0, "&m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯", "", "", this.fileConfiguration, this.file);
      teleportLineCreator(1, "&2&lTeleports", "", "", this.fileConfiguration, this.file);
      teleportLineCreator(2, "&m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯", "", "", this.fileConfiguration, this.file);
      teleportLineCreator(3, "&r&0Spawn", "Teleport to spawn!", "/em spawntp", this.fileConfiguration, this.file);
      teleportLineCreator(
         4,
         "&r" + PlayerStatusScreen.convertLightColorsToBlack(AdventurersGuildConfig.getAdventurersGuildMenuName()),
         "Teleport to the Adventurer's Guild Hub!",
         "/ag",
         this.fileConfiguration,
         this.file
      );
      onTeleportHover = ConfigurationEngine.setString(this.file, this.fileConfiguration, "onTeleportsHover", "Click to teleport!", true);
      commandsLineCreator(0, "&m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯", "", "", this.fileConfiguration, this.file);
      commandsLineCreator(1, "&3&lQuick Actions:", "", "", this.fileConfiguration, this.file);
      commandsLineCreator(2, "&m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯", "", "", this.fileConfiguration, this.file);
      commandsLineCreator(3, "", "", "", this.fileConfiguration, this.file);
      commandsLineCreator(
         4,
         "&5/ag &7- EliteMobs Hub",
         "CLICK TO USE\nThe place where you can find\nNPCs that give quests, buy and\nsell items, give advice and more!",
         "/ag",
         this.fileConfiguration,
         this.file
      );
      commandsLineCreator(5, "&5/em spawntp &7- Spawn", "CLICK TO USE\nTeleport to the server spawn!", "/em spawntp", this.fileConfiguration, this.file);
      commandsLineCreator(
         6, "&5/em shareitem &7- Share Item", "CLICK TO USE\nShares the item you're holding\non chat!", "/em shareitem", this.fileConfiguration, this.file
      );
      commandsLineCreator(7, "", "", "", this.fileConfiguration, this.file);
      commandsLineCreator(8, "", "", "", this.fileConfiguration, this.file);
      commandsLineCreator(9, "", "", "", this.fileConfiguration, this.file);
      commandsLineCreator(10, "", "", "", this.fileConfiguration, this.file);
      commandsLineCreator(11, "", "", "", this.fileConfiguration, this.file);
      commandsLineCreator(12, "", "", "", this.fileConfiguration, this.file);
      bossTrackerLineCreator(0, "&m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯", "", "", this.fileConfiguration, this.file);
      bossTrackerLineCreator(1, "&4&lBoss Tracker:", "Big bosses get displayed here!", "", this.fileConfiguration, this.file);
      bossTrackerLineCreator(2, "&m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯", "", "", this.fileConfiguration, this.file);
      onBossTrackHover = ConfigurationEngine.setString(this.file, this.fileConfiguration, "onBossTrackHover", "Click to track/untrack!", true);
      teleportChestMenuName = ConfigurationEngine.setString(
         this.file, this.fileConfiguration, "teleportChestMenuName", "<g:#355CA8:#5FA9E8>↔ Teleports</g>", true
      );
      teleportSpawnItem = ConfigurationEngine.setItemStack(
         this.file,
         this.fileConfiguration,
         "teleportSpawnItem",
         ItemStackGenerator.generateItemStack(Material.COMPASS, "&6&lSpawn", List.of("&7Teleport to the server spawn.", "", "&aClick to teleport!")),
         true
      );
      teleportSpawnSlot = ConfigurationEngine.setInt(this.fileConfiguration, "teleportSpawnSlot", 45);
      teleportGuildItem = ConfigurationEngine.setItemStack(
         this.file,
         this.fileConfiguration,
         "teleportGuildItem",
         ItemStackGenerator.generateItemStack(
            Material.END_PORTAL_FRAME, "&6&lEliteMobs Hub", List.of("&7Visit the Adventurer's Guild.", "", "&aClick to teleport!")
         ),
         true
      );
      teleportGuildSlot = ConfigurationEngine.setInt(this.fileConfiguration, "teleportGuildSlot", 49);
      bossTrackerChestMenuName = ConfigurationEngine.setString(
         this.file, this.fileConfiguration, "bossTrackerChestMenuName", "<g:#7A1F2B:#C2414A>☠ Boss Tracker</g>", true
      );
      indexChestMenuName = ConfigurationEngine.setString(
         this.file, this.fileConfiguration, "indexChestMenuName", "<g:#8B0000:#CC4400:#DAA520>EliteMobs</g> &7Menu", true
      );
      indexHeaderItem = ConfigurationEngine.setItemStack(
         this.file,
         this.fileConfiguration,
         "indexHeaderItem",
         ItemStackGenerator.generateItemStack(
            Material.PAPER,
            "&5&l/ag &7- &6EliteMobs Hub",
            new ArrayList<>(List.of("CLICK TO USE", "The place where you can find", "NPCs that give quests, buy and", "sell items, give advice and more!"))
         ),
         true
      );
      indexHeaderSlot = ConfigurationEngine.setInt(this.fileConfiguration, "indexHeaderSlot", 4);
      indexStatsItem = ConfigurationEngine.setItemStack(
         this.file,
         this.fileConfiguration,
         "indexStatsItem",
         ItemStackGenerator.generateItemStack(Material.MAP, "<g:#B8860B:#F0C040>★ Adventure Record</g>", List.of("Click to go!")),
         true
      );
      indexStatsSlot = ConfigurationEngine.setInt(this.fileConfiguration, "indexStatsSlot", 10);
      indexGearItem = ConfigurationEngine.setItemStack(
         this.file,
         this.fileConfiguration,
         "indexGearItem",
         ItemStackGenerator.generateItemStack(Material.DIAMOND_SWORD, "<g:#A63D2F:#E97932>⚔ Combat Gear</g>", List.of("Click to go!")),
         true
      );
      indexGearSlot = ConfigurationEngine.setInt(this.fileConfiguration, "indexGearSlot", 12);
      indexTeleportsItem = ConfigurationEngine.setItemStack(
         this.file,
         this.fileConfiguration,
         "indexTeleportsItem",
         ItemStackGenerator.generateItemStack(Material.END_PORTAL_FRAME, "<g:#355CA8:#5FA9E8>↔ Teleports</g>", List.of("Click to go!")),
         true
      );
      indexTeleportsSlot = ConfigurationEngine.setInt(this.fileConfiguration, "indexTeleportsSlot", 14);
      indexCommandsItem = ConfigurationEngine.setItemStack(
         this.file,
         this.fileConfiguration,
         "indexCommandsItem",
         ItemStackGenerator.generateItemStack(
            Material.JUKEBOX,
            "<g:#267A78:#58B8A9>⚡ Quick Actions</g>",
            List.of("&7Open the Hub, teleport to spawn", "&7or share your held Elite item.", "", "&aClick to view!")
         ),
         true
      );
      indexCommandsSlot = ConfigurationEngine.setInt(this.fileConfiguration, "indexCommandsSlot", 16);
      indexQuestTrackingItem = ConfigurationEngine.setItemStack(
         this.file,
         this.fileConfiguration,
         "indexQuestTrackingItem",
         ItemStackGenerator.generateItemStack(Material.WRITABLE_BOOK, "<g:#2E7D4F:#69C56F>✉ Quests</g>", List.of("Click to go!")),
         true
      );
      indexQuestTrackingSlot = ConfigurationEngine.setInt(this.fileConfiguration, "indexQuestTrackingSlot", 20);
      indexBossTrackingItem = ConfigurationEngine.setItemStack(
         this.file,
         this.fileConfiguration,
         "indexBossTrackingItem",
         ItemStackGenerator.generateItemStack(Material.TARGET, "<g:#7A1F2B:#C2414A>☠ Boss Tracker</g>", List.of("Click to go!")),
         true
      );
      indexBossTrackingSlot = ConfigurationEngine.setInt(this.fileConfiguration, "indexBossTrackingSlot", 24);
      indexSkillsItem = ConfigurationEngine.setItemStack(
         this.file,
         this.fileConfiguration,
         "indexSkillsItem",
         ItemStackGenerator.generateItemStack(
            Material.EXPERIENCE_BOTTLE, "<g:#6D3AA8:#A855F7>⚗ Skills</g>", List.of("&7View your skill levels", "&7and XP progress.", "", "&aClick to view!")
         ),
         true
      );
      indexSkillsSlot = ConfigurationEngine.setInt(this.fileConfiguration, "indexSkillsSlot", 22);
      indexPartyItem = ConfigurationEngine.setItemStack(
         this.file,
         this.fileConfiguration,
         "indexPartyItem",
         ItemStackGenerator.generateItemStack(
            Material.PLAYER_HEAD,
            "<g:#A04468:#E07A9A>♥ Party</g>",
            List.of("&7Create or manage a party", "&7and enter dungeons together.", "", "&aClick for party controls!")
         ),
         true
      );
      indexPartySlot = ConfigurationEngine.setInt(this.fileConfiguration, "indexPartySlot", 26);
      backItem = ConfigurationEngine.setItemStack(
         this.file, this.fileConfiguration, "backItem", ItemStackGenerator.generateItemStack(Material.BARRIER, "&cBack"), true
      );
      gearChestMenuName = ConfigurationEngine.setString(this.file, this.fileConfiguration, "gearChestMenuName", "<g:#A63D2F:#E97932>⚔ Combat Gear</g>", true);
      gearDamageItem = ConfigurationEngine.setItemStack(
         this.file,
         this.fileConfiguration,
         "gearDamageItem",
         ItemStackGenerator.generateItemStack(
            Material.DIAMOND_SWORD,
            "<g:#A63D2F:#E97932>⚔ Held Weapon</g>: &fLv. $weaponLevel",
            new ArrayList<>(
               List.of(
                  "&7$weaponSkill skill: &fLv. $weaponSkillLevel",
                  "&7Weapon factor vs Lv. $referenceLevel: &f×$weaponFactor",
                  "&7Critical chance: &f$critChance%",
                  "&7Equipment damage bonus: &f+$enchantmentBonus%",
                  "",
                  "&7Selected weapon perks:",
                  "&f$weaponPerks"
               )
            )
         ),
         true
      );
      gearDamageSlot = ConfigurationEngine.setInt(this.fileConfiguration, "gearDamageSlot", 23);
      gearArmorItem = ConfigurationEngine.setItemStack(
         this.file,
         this.fileConfiguration,
         "gearArmorItem",
         ItemStackGenerator.generateItemStack(
            Material.SHIELD,
            "<g:#355CA8:#5FA9E8>⛨ Armor Rating</g>: &f$armorLevel",
            new ArrayList<>(
               List.of(
                  "&7Armor skill: &fLv. $armorSkillLevel",
                  "&7Melee reduction vs Lv. $referenceLevel: &f$defenseMatch%",
                  "&7Health: &f$health&7/&f$maxHealth",
                  "",
                  "&7Selected armor perks:",
                  "&f$armorPerks"
               )
            )
         ),
         true
      );
      gearArmorSlot = ConfigurationEngine.setInt(this.fileConfiguration, "gearArmorSlot", 24);
      gearThreatItem = ConfigurationEngine.setItemStack(
         this.file,
         this.fileConfiguration,
         "gearThreatItem",
         ItemStackGenerator.generateItemStack(
            Material.TARGET,
            "<g:#B8860B:#F0C040>★ Combat Level</g>: &f$combatLevel",
            new ArrayList<>(
               List.of(
                  "&7Progression baseline: &fLv. $combatLevel",
                  "&7Threat generated: &f×$threatMultiplier",
                  "",
                  "&8Natural scaling may also use distance,",
                  "&8nearby players, party size and randomness."
               )
            )
         ),
         true
      );
      gearThreatSlot = ConfigurationEngine.setInt(this.fileConfiguration, "gearThreatSlot", 25);
      statsChestMenuName = ConfigurationEngine.setString(
         this.file, this.fileConfiguration, "statsChestMenuName", "<g:#B8860B:#F0C040>★ Adventure Record</g>", true
      );
      statsRankUnavailableText = ConfigurationEngine.setString(
         List.of("Text used for $rank while the persisted score ranking cache is still loading."),
         this.file,
         this.fileConfiguration,
         "statsRankUnavailableText",
         "Calculating...",
         true
      );
      statsRankTotalUnavailableText = ConfigurationEngine.setString(
         List.of("Text used for $ranktotal while the persisted score ranking cache is still loading."),
         this.file,
         this.fileConfiguration,
         "statsRankTotalUnavailableText",
         "?",
         true
      );
      statsRankItem = ConfigurationEngine.setItemStack(
         this.file,
         this.fileConfiguration,
         "statsRankItem",
         ItemStackGenerator.generateItemStack(
            Material.NETHER_STAR,
            "<g:#B8860B:#F0C040>★ Server Rank</g> &8• &f$rank &7/ $ranktotal",
            List.of("&7Competition rank by Adventure Score.", "&7Equal scores share the same rank.")
         ),
         true
      );
      statsRankSlot = ConfigurationEngine.setInt(this.fileConfiguration, "statsRankSlot", 10);
      statsDungeonsCompletedItem = ConfigurationEngine.setItemStack(
         this.file,
         this.fileConfiguration,
         "statsDungeonsCompletedItem",
         ItemStackGenerator.generateItemStack(
            Material.ENDER_EYE,
            "<g:#6D3AA8:#A855F7>Dungeons Cleared</g> &8• &f$dungeons",
            List.of("&7Counts successful clears only.", "&7Entering or abandoning does not count.")
         ),
         true
      );
      statsDungeonsCompletedSlot = ConfigurationEngine.setInt(this.fileConfiguration, "statsDungeonsCompletedSlot", 12);
      statsCombatLevelItem = ConfigurationEngine.setItemStack(
         this.file,
         this.fileConfiguration,
         "statsCombatLevelItem",
         ItemStackGenerator.generateItemStack(
            Material.TARGET, "<g:#A63D2F:#E97932>Combat Level</g> &8• &f$combat", List.of("&7Calculated from armor and your two", "&7strongest weapon skills.")
         ),
         true
      );
      statsCombatLevelSlot = ConfigurationEngine.setInt(this.fileConfiguration, "statsCombatLevelSlot", 13);
      statsEliteKillsItem = ConfigurationEngine.setItemStack(
         this.file,
         this.fileConfiguration,
         "statsEliteKillsItem",
         ItemStackGenerator.generateItemStack(
            Material.DIAMOND_SWORD, "<g:#2E7D4F:#69C56F>Elite Kills</g> &8• &f$kills", List.of("&7Confirmed non-exploit Elite Mob kills.")
         ),
         true
      );
      statsEliteKillsSlot = ConfigurationEngine.setInt(this.fileConfiguration, "statsEliteKillsSlot", 14);
      statsMaxEliteLevelKilledItem = ConfigurationEngine.setItemStack(
         this.file,
         this.fileConfiguration,
         "statsMaxEliteLevelKilledItem",
         ItemStackGenerator.generateItemStack(
            Material.BEACON, "<g:#267A78:#58B8A9>Peak Elite Level</g> &8• &f$maxKill", List.of("&7Your highest legitimate Elite Mob kill.")
         ),
         true
      );
      statsMaxEliteLevelKilledSlot = ConfigurationEngine.setInt(this.fileConfiguration, "statsMaxEliteLevelKilledSlot", 15);
      statsQuestsCompletedItem = ConfigurationEngine.setItemStack(
         this.file,
         this.fileConfiguration,
         "statsQuestsCompletedItem",
         ItemStackGenerator.generateItemStack(
            Material.WRITABLE_BOOK, "<g:#B8860B:#F0C040>Quests Completed</g> &8• &f$questsCompleted", List.of("&7EliteMobs quests fully completed.")
         ),
         true
      );
      statsQuestsCompletedSlot = ConfigurationEngine.setInt(this.fileConfiguration, "statsQuestsCompletedSlot", 16);
      statsScoreItem = ConfigurationEngine.setItemStack(
         this.file,
         this.fileConfiguration,
         "statsScoreItem",
         ItemStackGenerator.generateItemStack(
            Material.EXPERIENCE_BOTTLE,
            "<g:#355CA8:#5FA9E8>Adventure Score</g> &8• &f$score",
            List.of("&7Higher-level Elite kills award more score.", "&7Elite deaths reduce it.")
         ),
         true
      );
      statsScoreSlot = ConfigurationEngine.setInt(this.fileConfiguration, "statsScoreSlot", 11);
      commandsChestMenuName = ConfigurationEngine.setString(
         this.file, this.fileConfiguration, "commandsChestMenuName", "<g:#267A78:#58B8A9>⚡ Quick Actions</g>", true
      );
      commandsAGItem = ConfigurationEngine.setItemStack(
         this.file,
         this.fileConfiguration,
         "commandsAGItem",
         ItemStackGenerator.generateItemStack(
            Material.END_PORTAL_FRAME,
            "&5/ag",
            new ArrayList<>(
               List.of("&fClick to use!", "&fThe place where you can find", "&fNPCs that give quests, buy and", "&fsell items, give advice and more!")
            )
         ),
         true
      );
      commandsAGSlot = ConfigurationEngine.setInt(this.fileConfiguration, "commandsAGSlot", 11);
      commandsSpawnItem = ConfigurationEngine.setItemStack(
         this.file,
         this.fileConfiguration,
         "commandsSpawnItem",
         ItemStackGenerator.generateItemStack(
            Material.COMPASS, "&5/em spawntp", new ArrayList<>(List.of("&fClick to use!", "&fTeleports you to the server spawn."))
         ),
         true
      );
      commandsSpawnSlot = ConfigurationEngine.setInt(this.fileConfiguration, "commandsSpawnSlot", 13);
      commandsShareItemItem = ConfigurationEngine.setItemStack(
         this.file,
         this.fileConfiguration,
         "commandsShareItemItem",
         ItemStackGenerator.generateItemStack(
            Material.PAPER, "&5/em shareitem", new ArrayList<>(List.of("&fClick to use!", "&fShares the Elite Item you're holding", "&fon chat!"))
         ),
         true
      );
      commandsShareItemSlot = ConfigurationEngine.setInt(this.fileConfiguration, "commandsShareItemSlot", 15);
      skillsItemDisplayName = ConfigurationEngine.setString(this.file, this.fileConfiguration, "skillsItemDisplayName", "<g:#6D3AA8:#A855F7>⚗ Skills</g>", true);
      skillsItemLore1 = ConfigurationEngine.setString(this.file, this.fileConfiguration, "skillsItemLore1", "&7View your skill levels", true);
      skillsItemLore2 = ConfigurationEngine.setString(this.file, this.fileConfiguration, "skillsItemLore2", "&7and XP progress.", true);
      skillsItemClickLore = ConfigurationEngine.setString(this.file, this.fileConfiguration, "skillsItemClickLore", "&eClick to view!", true);
      skillsPageHeader = ConfigurationEngine.setString(this.file, this.fileConfiguration, "skillsPageHeader", "<g:#6D3AA8:#A855F7>⚗ Your Skills</g>", true);
      skillsPageLevelFormat = ConfigurationEngine.setString(this.file, this.fileConfiguration, "skillsPageLevelFormat", "&6$skillName &7Lv.&e$level", true);
      skillsPageXpFormat = ConfigurationEngine.setString(this.file, this.fileConfiguration, "skillsPageXpFormat", "&8$progressBar &7$currentXp/$nextXp", true);
      skillItemDisplayNameFormat = ConfigurationEngine.setString(
         this.file, this.fileConfiguration, "skillItemDisplayNameFormat", "&6&lLevel $level $skillName", true
      );
      skillItemSelectLore1 = ConfigurationEngine.setString(this.file, this.fileConfiguration, "skillItemSelectLore1", "&eClick to select passive skills", true);
      skillItemSelectLore2 = ConfigurationEngine.setString(this.file, this.fileConfiguration, "skillItemSelectLore2", "&eand see more details!", true);
      dialogTitlePlayerStatus = ConfigurationEngine.setString(
         List.of("Title for the main player status dialog menu."),
         this.file,
         this.fileConfiguration,
         "dialog.titlePlayerStatus",
         "<g:#8B0000:#CC4400:#DAA520>EliteMobs</g> &7Menu",
         true
      );
      dialogTitleStats = ConfigurationEngine.setString(
         List.of("Title for the Stats dialog section."),
         this.file,
         this.fileConfiguration,
         "dialog.titleStats",
         "<g:#B8860B:#F0C040>★ Adventure Record</g>",
         true
      );
      dialogTitleGear = ConfigurationEngine.setString(
         List.of("Title for the Gear dialog section."), this.file, this.fileConfiguration, "dialog.titleGear", "<g:#A63D2F:#E97932>⚔ Combat Gear</g>", true
      );
      dialogTitleTeleports = ConfigurationEngine.setString(
         List.of("Title for the Teleports dialog section."),
         this.file,
         this.fileConfiguration,
         "dialog.titleTeleports",
         "<g:#355CA8:#5FA9E8>↔ Teleports</g>",
         true
      );
      dialogTitleCommands = ConfigurationEngine.setString(
         List.of("Title for the Quick Actions dialog section."),
         this.file,
         this.fileConfiguration,
         "dialog.titleCommands",
         "<g:#267A78:#58B8A9>⚡ Quick Actions</g>",
         true
      );
      dialogTitleQuests = ConfigurationEngine.setString(
         List.of("Title for the Quests dialog section."), this.file, this.fileConfiguration, "dialog.titleQuests", "<g:#2E7D4F:#69C56F>✉ Quests</g>", true
      );
      dialogTitleBossTracking = ConfigurationEngine.setString(
         List.of("Title for the Boss Tracking dialog section."),
         this.file,
         this.fileConfiguration,
         "dialog.titleBossTracking",
         "<g:#7A1F2B:#C2414A>☠ Boss Tracker</g>",
         true
      );
      dialogTitleSkills = ConfigurationEngine.setString(
         List.of("Title for the Skills dialog section."), this.file, this.fileConfiguration, "dialog.titleSkills", "<g:#6D3AA8:#A855F7>⚗ Skills</g>", true
      );
      dialogTitleParty = ConfigurationEngine.setString(
         List.of("Title for the Party dialog section."), this.file, this.fileConfiguration, "dialog.titleParty", "<g:#A04468:#E07A9A>♥ Party</g>", true
      );
      dialogPartyDescription = ConfigurationEngine.setString(
         List.of("Description shown on the Party dialog section."),
         this.file,
         this.fileConfiguration,
         "dialog.partyDescription",
         "&7Team up with as many as four other players. Parties share nearby quest progress, vote on loot, and enter dungeons together.",
         true
      );
      dialogPartyCreateButton = ConfigurationEngine.setString(
         List.of("Button used to create a party."),
         this.file,
         this.fileConfiguration,
         "dialog.partyCreateButton",
         "<g:#2E7D4F:#69C56F>+ Create a Party</g>",
         true
      );
      dialogPartyInviteButton = ConfigurationEngine.setString(
         List.of("Button used to prepare the party invite command."),
         this.file,
         this.fileConfiguration,
         "dialog.partyInviteButton",
         "<g:#267A78:#58B8A9>✉ Invite a Player</g>",
         true
      );
      dialogPartyLeaveButton = ConfigurationEngine.setString(
         List.of("Button used to leave a party."), this.file, this.fileConfiguration, "dialog.partyLeaveButton", "<g:#7A1F2B:#C2414A>✘ Leave Party</g>", true
      );
      dialogPartyInviteTitle = ConfigurationEngine.setString(
         List.of("Title for the dialog that lists players available for a party invitation."),
         this.file,
         this.fileConfiguration,
         "dialog.partyInviteTitle",
         "<g:#A04468:#E07A9A>✉ Invite a Player</g>",
         true
      );
      dialogPartyInvitePlayerButton = ConfigurationEngine.setString(
         List.of("Button for an available party invite target. Use $player for their name."),
         this.file,
         this.fileConfiguration,
         "dialog.partyInvitePlayerButton",
         "<g:#267A78:#58B8A9>✉ Invite</g> &f$player",
         true
      );
      dialogPartyInvitePlayerTooltip = ConfigurationEngine.setString(
         List.of("Tooltip for a party invite target. Use $player for their name."),
         this.file,
         this.fileConfiguration,
         "dialog.partyInvitePlayerTooltip",
         "&7Invite $player to your party.",
         true
      );
      dialogPartyInviteNoPlayers = ConfigurationEngine.setString(
         List.of("Message shown when nobody online can currently be invited."),
         this.file,
         this.fileConfiguration,
         "dialog.partyInviteNoPlayers",
         "&7No available players are online right now.",
         true
      );
      dialogNoActiveQuests = ConfigurationEngine.setString(
         List.of("Message shown when the player has no active quests."), this.file, this.fileConfiguration, "dialog.noActiveQuests", "No active quests", true
      );
      dialogNoTrackableBosses = ConfigurationEngine.setString(
         List.of("Message shown when there are no bosses available to track."),
         this.file,
         this.fileConfiguration,
         "dialog.noTrackableBosses",
         "&7No trackable bosses are currently active.",
         true
      );
      dialogActiveQuestsFormat = ConfigurationEngine.setString(
         List.of("Message showing the number of active quests. Use $amount for the quest count."),
         this.file,
         this.fileConfiguration,
         "dialog.activeQuestsFormat",
         "You have $amount active quest(s).",
         true
      );
      dialogBackButton = ConfigurationEngine.setString(
         List.of("Text for the back button in dialog sections."), this.file, this.fileConfiguration, "dialog.backButton", "← Back to Menu", true
      );
   }

   @Generated
   public static String[] getIndexTextLines() {
      return indexTextLines;
   }

   @Generated
   public static String[] getIndexHoverLines() {
      return indexHoverLines;
   }

   @Generated
   public static String[] getIndexCommandLines() {
      return indexCommandLines;
   }

   @Generated
   public static String[] getStatsTextLines() {
      return statsTextLines;
   }

   @Generated
   public static String[] getStatsHoverLines() {
      return statsHoverLines;
   }

   @Generated
   public static String[] getStatsCommandLines() {
      return statsCommandLines;
   }

   @Generated
   public static String[] getGearTextLines() {
      return gearTextLines;
   }

   @Generated
   public static String[] getGearHoverLines() {
      return gearHoverLines;
   }

   @Generated
   public static String[] getTeleportTextLines() {
      return teleportTextLines;
   }

   @Generated
   public static String[] getTeleportHoverLines() {
      return teleportHoverLines;
   }

   @Generated
   public static String[] getTeleportCommandLines() {
      return teleportCommandLines;
   }

   @Generated
   public static String[] getCommandsTextLines() {
      return commandsTextLines;
   }

   @Generated
   public static String[] getCommandsHoverLines() {
      return commandsHoverLines;
   }

   @Generated
   public static String[] getCommandsCommandLines() {
      return commandsCommandLines;
   }

   @Generated
   public static String[] getBossTrackerTextLines() {
      return bossTrackerTextLines;
   }

   @Generated
   public static String[] getBossTrackerHoverLines() {
      return bossTrackerHoverLines;
   }

   @Generated
   public static String[] getBossTrackerCommandLines() {
      return bossTrackerCommandLines;
   }

   @Generated
   public static boolean isDoStatsPage() {
      return doStatsPage;
   }

   @Generated
   public static boolean isDoGearPage() {
      return doGearPage;
   }

   @Generated
   public static boolean isDoTeleportsPage() {
      return doTeleportsPage;
   }

   @Generated
   public static boolean isDoCommandsPage() {
      return doCommandsPage;
   }

   @Generated
   public static boolean isDoQuestTrackingPage() {
      return doQuestTrackingPage;
   }

   @Generated
   public static boolean isDoBossTrackingPage() {
      return doBossTrackingPage;
   }

   @Generated
   public static List<String> getLandingOverviewLines() {
      return landingOverviewLines;
   }

   @Generated
   public static List<String> getDialogGearSummaryLines() {
      return dialogGearSummaryLines;
   }

   @Generated
   public static String getGearUnarmedLabel() {
      return gearUnarmedLabel;
   }

   @Generated
   public static String getGearNoSelectedPerksLabel() {
      return gearNoSelectedPerksLabel;
   }

   @Generated
   public static String getDialogGearHelmetLabel() {
      return dialogGearHelmetLabel;
   }

   @Generated
   public static String getDialogGearChestplateLabel() {
      return dialogGearChestplateLabel;
   }

   @Generated
   public static String getDialogGearLeggingsLabel() {
      return dialogGearLeggingsLabel;
   }

   @Generated
   public static String getDialogGearBootsLabel() {
      return dialogGearBootsLabel;
   }

   @Generated
   public static String getDialogGearMainHandLabel() {
      return dialogGearMainHandLabel;
   }

   @Generated
   public static String getDialogGearOffHandLabel() {
      return dialogGearOffHandLabel;
   }

   @Generated
   public static String getDialogQuestFallbackFormat() {
      return dialogQuestFallbackFormat;
   }

   @Generated
   public static String getTeleportChestMenuName() {
      return teleportChestMenuName;
   }

   @Generated
   public static ItemStack getTeleportSpawnItem() {
      return teleportSpawnItem;
   }

   @Generated
   public static int getTeleportSpawnSlot() {
      return teleportSpawnSlot;
   }

   @Generated
   public static ItemStack getTeleportGuildItem() {
      return teleportGuildItem;
   }

   @Generated
   public static int getTeleportGuildSlot() {
      return teleportGuildSlot;
   }

   @Generated
   public static String getBossTrackerChestMenuName() {
      return bossTrackerChestMenuName;
   }

   @Generated
   public static String getOnBossTrackHover() {
      return onBossTrackHover;
   }

   @Generated
   public static String getOnTeleportHover() {
      return onTeleportHover;
   }

   @Generated
   public static String getIndexChestMenuName() {
      return indexChestMenuName;
   }

   @Generated
   public static ItemStack getBackItem() {
      return backItem;
   }

   @Generated
   public static ItemStack getIndexHeaderItem() {
      return indexHeaderItem;
   }

   @Generated
   public static int getIndexHeaderSlot() {
      return indexHeaderSlot;
   }

   @Generated
   public static ItemStack getIndexStatsItem() {
      return indexStatsItem;
   }

   @Generated
   public static int getIndexStatsSlot() {
      return indexStatsSlot;
   }

   @Generated
   public static ItemStack getIndexGearItem() {
      return indexGearItem;
   }

   @Generated
   public static int getIndexGearSlot() {
      return indexGearSlot;
   }

   @Generated
   public static ItemStack getIndexTeleportsItem() {
      return indexTeleportsItem;
   }

   @Generated
   public static int getIndexTeleportsSlot() {
      return indexTeleportsSlot;
   }

   @Generated
   public static ItemStack getIndexCommandsItem() {
      return indexCommandsItem;
   }

   @Generated
   public static int getIndexCommandsSlot() {
      return indexCommandsSlot;
   }

   @Generated
   public static ItemStack getIndexQuestTrackingItem() {
      return indexQuestTrackingItem;
   }

   @Generated
   public static int getIndexQuestTrackingSlot() {
      return indexQuestTrackingSlot;
   }

   @Generated
   public static ItemStack getIndexBossTrackingItem() {
      return indexBossTrackingItem;
   }

   @Generated
   public static int getIndexBossTrackingSlot() {
      return indexBossTrackingSlot;
   }

   @Generated
   public static ItemStack getIndexSkillsItem() {
      return indexSkillsItem;
   }

   @Generated
   public static int getIndexSkillsSlot() {
      return indexSkillsSlot;
   }

   @Generated
   public static ItemStack getIndexPartyItem() {
      return indexPartyItem;
   }

   @Generated
   public static int getIndexPartySlot() {
      return indexPartySlot;
   }

   @Generated
   public static String getGearChestMenuName() {
      return gearChestMenuName;
   }

   @Generated
   public static ItemStack getGearDamageItem() {
      return gearDamageItem;
   }

   @Generated
   public static int getGearDamageSlot() {
      return gearDamageSlot;
   }

   @Generated
   public static ItemStack getGearArmorItem() {
      return gearArmorItem;
   }

   @Generated
   public static int getGearArmorSlot() {
      return gearArmorSlot;
   }

   @Generated
   public static ItemStack getGearThreatItem() {
      return gearThreatItem;
   }

   @Generated
   public static int getGearThreatSlot() {
      return gearThreatSlot;
   }

   @Generated
   public static String getStatsChestMenuName() {
      return statsChestMenuName;
   }

   @Generated
   public static String getStatsRankUnavailableText() {
      return statsRankUnavailableText;
   }

   @Generated
   public static String getStatsRankTotalUnavailableText() {
      return statsRankTotalUnavailableText;
   }

   @Generated
   public static ItemStack getStatsRankItem() {
      return statsRankItem;
   }

   @Generated
   public static int getStatsRankSlot() {
      return statsRankSlot;
   }

   @Generated
   public static ItemStack getStatsDungeonsCompletedItem() {
      return statsDungeonsCompletedItem;
   }

   @Generated
   public static int getStatsDungeonsCompletedSlot() {
      return statsDungeonsCompletedSlot;
   }

   @Generated
   public static ItemStack getStatsCombatLevelItem() {
      return statsCombatLevelItem;
   }

   @Generated
   public static int getStatsCombatLevelSlot() {
      return statsCombatLevelSlot;
   }

   @Generated
   public static ItemStack getStatsEliteKillsItem() {
      return statsEliteKillsItem;
   }

   @Generated
   public static int getStatsEliteKillsSlot() {
      return statsEliteKillsSlot;
   }

   @Generated
   public static ItemStack getStatsMaxEliteLevelKilledItem() {
      return statsMaxEliteLevelKilledItem;
   }

   @Generated
   public static int getStatsMaxEliteLevelKilledSlot() {
      return statsMaxEliteLevelKilledSlot;
   }

   @Generated
   public static ItemStack getStatsQuestsCompletedItem() {
      return statsQuestsCompletedItem;
   }

   @Generated
   public static int getStatsQuestsCompletedSlot() {
      return statsQuestsCompletedSlot;
   }

   @Generated
   public static ItemStack getStatsScoreItem() {
      return statsScoreItem;
   }

   @Generated
   public static int getStatsScoreSlot() {
      return statsScoreSlot;
   }

   @Generated
   public static String getCommandsChestMenuName() {
      return commandsChestMenuName;
   }

   @Generated
   public static ItemStack getCommandsAGItem() {
      return commandsAGItem;
   }

   @Generated
   public static int getCommandsAGSlot() {
      return commandsAGSlot;
   }

   @Generated
   public static ItemStack getCommandsSpawnItem() {
      return commandsSpawnItem;
   }

   @Generated
   public static int getCommandsSpawnSlot() {
      return commandsSpawnSlot;
   }

   @Generated
   public static ItemStack getCommandsShareItemItem() {
      return commandsShareItemItem;
   }

   @Generated
   public static int getCommandsShareItemSlot() {
      return commandsShareItemSlot;
   }

   @Generated
   public static String getSkillsItemDisplayName() {
      return skillsItemDisplayName;
   }

   @Generated
   public static String getSkillsItemLore1() {
      return skillsItemLore1;
   }

   @Generated
   public static String getSkillsItemLore2() {
      return skillsItemLore2;
   }

   @Generated
   public static String getSkillsItemClickLore() {
      return skillsItemClickLore;
   }

   @Generated
   public static String getSkillsPageHeader() {
      return skillsPageHeader;
   }

   @Generated
   public static String getSkillsPageLevelFormat() {
      return skillsPageLevelFormat;
   }

   @Generated
   public static String getSkillsPageXpFormat() {
      return skillsPageXpFormat;
   }

   @Generated
   public static String getSkillItemDisplayNameFormat() {
      return skillItemDisplayNameFormat;
   }

   @Generated
   public static String getSkillItemSelectLore1() {
      return skillItemSelectLore1;
   }

   @Generated
   public static String getSkillItemSelectLore2() {
      return skillItemSelectLore2;
   }

   @Generated
   public static String getDialogTitlePlayerStatus() {
      return dialogTitlePlayerStatus;
   }

   @Generated
   public static String getDialogTitleStats() {
      return dialogTitleStats;
   }

   @Generated
   public static String getDialogTitleGear() {
      return dialogTitleGear;
   }

   @Generated
   public static String getDialogTitleTeleports() {
      return dialogTitleTeleports;
   }

   @Generated
   public static String getDialogTitleCommands() {
      return dialogTitleCommands;
   }

   @Generated
   public static String getDialogTitleQuests() {
      return dialogTitleQuests;
   }

   @Generated
   public static String getDialogTitleBossTracking() {
      return dialogTitleBossTracking;
   }

   @Generated
   public static String getDialogTitleSkills() {
      return dialogTitleSkills;
   }

   @Generated
   public static String getDialogTitleParty() {
      return dialogTitleParty;
   }

   @Generated
   public static String getDialogPartyDescription() {
      return dialogPartyDescription;
   }

   @Generated
   public static String getDialogPartyCreateButton() {
      return dialogPartyCreateButton;
   }

   @Generated
   public static String getDialogPartyInviteButton() {
      return dialogPartyInviteButton;
   }

   @Generated
   public static String getDialogPartyLeaveButton() {
      return dialogPartyLeaveButton;
   }

   @Generated
   public static String getDialogPartyInviteTitle() {
      return dialogPartyInviteTitle;
   }

   @Generated
   public static String getDialogPartyInvitePlayerButton() {
      return dialogPartyInvitePlayerButton;
   }

   @Generated
   public static String getDialogPartyInvitePlayerTooltip() {
      return dialogPartyInvitePlayerTooltip;
   }

   @Generated
   public static String getDialogPartyInviteNoPlayers() {
      return dialogPartyInviteNoPlayers;
   }

   @Generated
   public static String getDialogNoActiveQuests() {
      return dialogNoActiveQuests;
   }

   @Generated
   public static String getDialogNoTrackableBosses() {
      return dialogNoTrackableBosses;
   }

   @Generated
   public static String getDialogActiveQuestsFormat() {
      return dialogActiveQuestsFormat;
   }

   @Generated
   public static String getDialogBackButton() {
      return dialogBackButton;
   }
}
