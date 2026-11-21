package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.magmacore.config.ConfigurationFile;
import com.magmaguy.elitemobs.magmacore.nightbreak.NightbreakChatStyle;
import java.util.List;
import lombok.Generated;

public class DungeonsConfig extends ConfigurationFile {
   private static String instancedDungeonTitle;
   private static List<String> instancedDungeonDescription;
   private static String dungeonJoinAsPlayerText;
   private static String dungeonJoinAsSpectatorText;
   private static boolean allowSpectatorsInInstancedContent;
   private static String instancedDungeonCompleteMessage;
   private static String instancedDungeonClosingInstanceMessage;
   private static boolean friendlyFireInDungeons;
   private static double fireDamageMultiplier;
   private static double poisonDamageMultiplier;
   private static double witherDamageMultiplier;
   private static boolean enableDungeonFoodRegeneration;
   private static int dungeonFoodRegenerationCombatTimeoutSeconds;
   private static int dungeonFoodRegenerationIntervalSeconds;
   private static String dungeonFoodRegenerationCombatStartMessage;
   private static String dungeonFoodRegenerationCombatEndMessage;
   private static String dynamicDungeonLevelSelectionMenuTitle;
   private static String dynamicDungeonLevelSelectionItemTitle;
   private static String dynamicDungeonLevelSelectionClickToSelect;
   private static String dynamicDungeonLevelSelectionMobsWillBeLevel;
   private static String dynamicDungeonLevelSelectionRecommended;
   private static String dynamicDungeonLevelSelectionEasier;
   private static String dynamicDungeonLevelSelectionHarder;
   private static String dynamicDungeonDifficultySelectionMenuTitle;
   private static String dynamicDungeonDifficultySelectionSelectedLevel;
   private static String dungeonLockoutTitle;
   private static String dungeonLockoutSubtitle;
   private static String dungeonLockoutChatMessage;
   private static String invalidInstancedDungeonMessage;
   private static String invalidDynamicDungeonMessage;
   private static String matchAlreadyEndedMessage;
   private static String dungeonBrowserPlayersLabel;
   private static String dungeonBrowserPlayerEntryFormat;
   private static String dungeonBrowserLevelLabel;
   private static String dungeonPreparingQueueMessage;
   private static String dungeonPreparingMessage;
   private static String dungeonPrepareFailedMessage;
   private static String dungeonLoadFailedMessage;
   private static String dungeonCancelledShutdownMessage;
   private static String dungeonDataFailedMessage;
   private static String dungeonNoPermissionMessage;
   private static String dungeonCancelledEventMessage;
   private static String dungeonCopyFailedMessage;
   private static String dungeonWorldLoadFailedMessage;
   private static String dungeonDifficultyMessage;
   private static String dynamicDungeonDataFailedMessage;
   private static String dynamicDungeonNoPermissionMessage;
   private static String dynamicDungeonWorldLoadFailedMessage;
   private static String dynamicDungeonLevelSetMessage;
   private static String enchantNoChallengeMessage;
   private static String enchantChallengeCompleteMessage;
   private static String enchantChallengeSuccessMessage;
   private static String enchantCriticalFailureMessage;
   private static String enchantChallengeFailedMessage;
   private static String contentInstalledMessage;
   private static String contentUninstallFailedMessage;
   private static String contentUninstalledMessage;
   private static String dynamicDungeonAccessMessage1;
   private static String dynamicDungeonAccessMessage2;
   private static String dynamicDungeonAccessMessage3;
   private static String contentInstalledLine1;
   private static String contentInstalledLine2;
   private static String contentNotInstalledLine1;
   private static String contentNotInstalledLine2;
   private static String contentPartialLine1;
   private static String contentPartialLine2;
   private static String contentPartialLine3;
   private static String contentPartialLine4;
   private static String bundledContentPartialLine1;
   private static String bundledContentPartialLine2;
   private static String bundledContentPartialLine3;
   private static String contentToggleInProgressMessage;
   private static String contentNotDownloadedLine1;
   private static String contentNotDownloadedLine2;
   private static String contentNeedAccessMessage;
   private static String contentAccessClickMessage;
   private static String contentAvailablePatreon;
   private static String contentAvailableItch;
   private static String contentUpdateAvailable;
   private static String contentUpdateClickMessage;
   private static String contentUpdateNeedAccess;
   private static String contentUpdateAccessClick;
   private static String contentDownloadLegacyMessage;
   private static String contentDownloadSeparator;
   private static String contentNightbreakPromptLine1;
   private static String contentNightbreakPromptLine2;
   private static String contentNightbreakPromptLine3;
   private static String contentNightbreakPromptLine4;
   private static String contentCheckingAccessMessage;
   private static String contentAccessFailedMessage;
   private static String contentAccessFailedLink;
   private static String contentInvalidTokenMessage;
   private static String contentInvalidTokenInstructions;
   private static String contentDownloadedReloadMessage;
   private static String instancedDungeonInstalledMessage;
   private static String instancedDungeonAccessMessage;
   private static String instancedDungeonInstallNote;
   private static String enchantmentDungeonInstalledMessage;
   private static String instancePunchToRez;
   private static String instanceLivesLeft;
   private static String skillMigrationSeparator;
   private static String skillMigrationTitle;
   private static String skillMigrationLine1;
   private static String skillMigrationLine2;
   private static String skillMigrationLine3;
   private static String skillMigrationLine4;
   private static String skillMigrationLine5;
   private static String skillMigrationLine6;
   private static String skillMigrationLine6Suffix;
   private static String dynamicDungeonInstalledMessage;
   private static String vorpalStrikeReadyMessage;
   private static String soulSiphonDecayMessage;
   private static String spectralScytheCooldownMessage;
   private static String skillLevelUpTitle;
   private static String skillLevelUpSubtitle;
   private static String skillLevelUpBroadcast;
   private static boolean skillLevelUpBroadcastEnabled;
   private static String skillXpNoGainMessage;
   private static String skillXpCappedMessage;
   private static String skillActivationFormat;
   private static String skillStackFormat;
   private static String tazeShockedTitle;
   private static String tazeShockedSubtitle;
   private static String legionsDisciplineBrokenMessage;
   private static String avatarFadesMessage;
   private static String modelsAutoInstallMessage;
   private static String modelsCannotUninstallMessage;
   private static String itemsInstallingMessage;
   private static String itemsUninstallingMessage;
   private static String itemsSavingMessage;
   private static String itemsReloadingMessage;
   private static String eventsInstallingMessage;
   private static String eventsUninstallingMessage;
   private static String eventsSavingMessage;
   private static String eventsReloadingMessage;
   private static String contentConfigurationSaveFailedMessage;
   private static String contentNoAccessMessage;
   private static String contentGetAccessMessage;
   private static String contentNightbreakLink;
   private static String contentPatreonLink;
   private static String contentItchLink;
   private static String contentLinkAccountMessage;

   public DungeonsConfig() {
      super("dungeons.yml");
   }

   @Override
   public void initializeValues() {
      NightbreakChatStyle.migrateSeparators(this.fileConfiguration, "contentDownloadSeparator", "skillMigrationSeparator");
      dungeonJoinAsPlayerText = ConfigurationEngine.setString(
         List.of("Sets the text for joining a dungeon as a player! Placeholders:", "$dungeonName - the name of the dungeon"),
         this.file,
         this.fileConfiguration,
         "joinDungeonAsPlayerText",
         "&fJoin $dungeonName as a player!",
         true
      );
      dungeonJoinAsSpectatorText = ConfigurationEngine.setString(
         List.of("Sets the text for joining a dungeon as a spectator! Placeholders:", "$dungeonName - the name of the dungeon"),
         this.file,
         this.fileConfiguration,
         "joinDungeonAsSpectatorText",
         "&fJoin $dungeonName as a spectator!",
         true
      );
      instancedDungeonTitle = ConfigurationEngine.setString(
         List.of(
            "Sets the title that will show up in the item description of instanced dungeon menus",
            "$difficulty is the placeholder for the difficulty name in the configuration file of the dungeon"
         ),
         this.file,
         this.fileConfiguration,
         "instancedDungeonTitle",
         "Start $difficulty difficulty dungeon!",
         true
      );
      instancedDungeonDescription = ConfigurationEngine.setList(
         List.of(
            "Sets the description that will show up in the item description of instanced dungeon menus",
            "$dungeonName is the placeholder for the dungeon name in the configuration file of the dungeon"
         ),
         this.file,
         this.fileConfiguration,
         "instancedDungeonDescription",
         List.of("&fCreate a new instance of the dungeon", "$dungeonName &ffor yourself and maybe", "&fsome friends!"),
         true
      );
      allowSpectatorsInInstancedContent = ConfigurationEngine.setBoolean(
         List.of(
            "Whether vanilla spectator mode is used inside instanced content (arenas & dungeons).",
            "When true (default): players may join as spectators, and dead players become",
            "spectators at a death banner so teammates can revive them (multiple lives).",
            "When false: the 'Spectate' option is hidden AND players no longer enter spectator",
            "mode on death - they are removed from the instance instead, giving every participant",
            "a single life. Set to false to fully prevent spectator mode (and the vanilla",
            "spectator-teleport exploit) inside instanced content."
         ),
         this.fileConfiguration,
         "allowSpectatorsInInstancedContent",
         true
      );
      instancedDungeonCompleteMessage = ConfigurationEngine.setString(
         List.of("Sets the message that appears when an instanced dungeon is completed"),
         this.file,
         this.fileConfiguration,
         "instancedDungeonCompleteMessage",
         "[EliteMobs] Dungeon completed! It will self-destruct in 2 minutes!",
         true
      );
      instancedDungeonClosingInstanceMessage = ConfigurationEngine.setString(
         List.of("Sets the message that appears when an instanced dungeon closing"),
         this.file,
         this.fileConfiguration,
         "instancedDungeonClosingInstanceMessage",
         "[EliteMobs] Closing instance!",
         true
      );
      friendlyFireInDungeons = ConfigurationEngine.setBoolean(
         List.of("Sets if PvP will be allowed in dungeons"), this.fileConfiguration, "friendlyFireInDungeons", false
      );
      fireDamageMultiplier = ConfigurationEngine.setDouble(
         List.of(
            "Sets the damage multiplier for fire damage in dungeons",
            "This is important for balance as by default the damage is a bit too high for the dungeons as we design them"
         ),
         this.fileConfiguration,
         "fireDamageMultiplier",
         0.5
      );
      witherDamageMultiplier = ConfigurationEngine.setDouble(
         List.of(
            "Sets the damage multiplier for wither damage in dungeons",
            "This is important for balance as by default the damage is a bit too high for the dungeons as we design them"
         ),
         this.fileConfiguration,
         "witherDamageMultiplier",
         0.5
      );
      poisonDamageMultiplier = ConfigurationEngine.setDouble(
         List.of(
            "Sets the damage multiplier for fire damage in dungeons",
            "This is important for balance as by default the damage is a bit too high for the dungeons as we design them"
         ),
         this.fileConfiguration,
         "poisonDamageMultiplier",
         0.5
      );
      enableDungeonFoodRegeneration = ConfigurationEngine.setBoolean(
         List.of("Whether players regenerate food while out of combat in EliteMobs dungeons."), this.fileConfiguration, "enableDungeonFoodRegeneration", true
      );
      dungeonFoodRegenerationCombatTimeoutSeconds = Math.max(
         1,
         ConfigurationEngine.setInt(
            List.of("Seconds without dealing or receiving enemy damage before a player leaves combat."),
            this.fileConfiguration,
            "dungeonFoodRegenerationCombatTimeoutSeconds",
            20
         )
      );
      dungeonFoodRegenerationIntervalSeconds = Math.max(
         1,
         ConfigurationEngine.setInt(
            List.of("Seconds between level-I saturation pulses while an eligible player is out of combat."),
            this.fileConfiguration,
            "dungeonFoodRegenerationIntervalSeconds",
            5
         )
      );
      dungeonFoodRegenerationCombatStartMessage = ConfigurationEngine.setString(
         List.of("Action bar message shown when dungeon combat starts."),
         this.file,
         this.fileConfiguration,
         "dungeonFoodRegenerationCombatStartMessage",
         "&cCombat start",
         true
      );
      dungeonFoodRegenerationCombatEndMessage = ConfigurationEngine.setString(
         List.of("Action bar message shown when dungeon combat ends."),
         this.file,
         this.fileConfiguration,
         "dungeonFoodRegenerationCombatEndMessage",
         "&aCombat end",
         true
      );
      dynamicDungeonLevelSelectionMenuTitle = ConfigurationEngine.setString(
         List.of("Sets the title for the dynamic dungeon level selection menu"),
         this.file,
         this.fileConfiguration,
         "dynamicDungeonLevelSelectionMenuTitle",
         "&8Select Dungeon Level",
         true
      );
      dynamicDungeonLevelSelectionItemTitle = ConfigurationEngine.setString(
         List.of("Sets the title for level selection items in the dynamic dungeon menu", "$level is the placeholder for the level number"),
         this.file,
         this.fileConfiguration,
         "dynamicDungeonLevelSelectionItemTitle",
         "&fLevel $level",
         true
      );
      dynamicDungeonLevelSelectionClickToSelect = ConfigurationEngine.setString(
         List.of("Sets the 'click to select' text in the dynamic dungeon level selection menu", "$level is the placeholder for the level number"),
         this.file,
         this.fileConfiguration,
         "dynamicDungeonLevelSelectionClickToSelect",
         "&7Click to select level &e$level",
         true
      );
      dynamicDungeonLevelSelectionMobsWillBeLevel = ConfigurationEngine.setString(
         List.of("Sets the 'mobs will be level' text in the dynamic dungeon level selection menu", "$level is the placeholder for the level number"),
         this.file,
         this.fileConfiguration,
         "dynamicDungeonLevelSelectionMobsWillBeLevel",
         "&7All mobs will be level &e$level",
         true
      );
      dynamicDungeonLevelSelectionRecommended = ConfigurationEngine.setString(
         List.of("Sets the 'recommended level' text in the dynamic dungeon level selection menu"),
         this.file,
         this.fileConfiguration,
         "dynamicDungeonLevelSelectionRecommended",
         "&a&lRecommended level!",
         true
      );
      dynamicDungeonLevelSelectionEasier = ConfigurationEngine.setString(
         List.of("Sets the 'easier than recommended' text in the dynamic dungeon level selection menu"),
         this.file,
         this.fileConfiguration,
         "dynamicDungeonLevelSelectionEasier",
         "&eEasier than recommended",
         true
      );
      dynamicDungeonLevelSelectionHarder = ConfigurationEngine.setString(
         List.of("Sets the 'harder than recommended' text in the dynamic dungeon level selection menu"),
         this.file,
         this.fileConfiguration,
         "dynamicDungeonLevelSelectionHarder",
         "&6Harder than recommended",
         true
      );
      dynamicDungeonDifficultySelectionMenuTitle = ConfigurationEngine.setString(
         List.of("Sets the title for the dynamic dungeon difficulty selection menu"),
         this.file,
         this.fileConfiguration,
         "dynamicDungeonDifficultySelectionMenuTitle",
         "&8Select Difficulty",
         true
      );
      dynamicDungeonDifficultySelectionSelectedLevel = ConfigurationEngine.setString(
         List.of("Sets the 'selected level' text in the dynamic dungeon difficulty selection menu", "$level is the placeholder for the selected level number"),
         this.file,
         this.fileConfiguration,
         "dynamicDungeonDifficultySelectionSelectedLevel",
         "&7Selected Level: &e$level",
         true
      );
      dungeonLockoutTitle = ConfigurationEngine.setString(
         List.of("Title shown on screen during dungeon boss lockout.", "Leave empty to show only the HUD notification and chat message."),
         this.file,
         this.fileConfiguration,
         "dungeonLockoutTitle",
         "",
         true
      );
      dungeonLockoutSubtitle = ConfigurationEngine.setString(
         List.of(
            "Sets the HUD notification when a player kills a boss they are locked out from.",
            "The legacy key name is retained; this text is not shown as an on-screen subtitle."
         ),
         this.file,
         this.fileConfiguration,
         "dungeonLockoutSubtitle",
         "&cLockout!",
         true
      );
      dungeonLockoutChatMessage = ConfigurationEngine.setString(
         List.of(
            "Sets the chat message shown when a player kills a boss they are locked out from",
            "$bossName is the placeholder for the boss name",
            "$remainingTime is the placeholder for the remaining lockout time"
         ),
         this.file,
         this.fileConfiguration,
         "dungeonLockoutChatMessage",
         "&c[EliteMobs] &7You killed &c$bossName &7in the last lockout period and must wait another &e$remainingTime &7before it will drop loot for you again.",
         true
      );
      invalidInstancedDungeonMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when a player tries to open a browser for an invalid instanced dungeon"),
         this.file,
         this.fileConfiguration,
         "invalidInstancedDungeonMessage",
         "[EliteMobs] Not a valid instanced dungeon!",
         true
      );
      invalidDynamicDungeonMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when a player tries to open a browser for an invalid dynamic dungeon"),
         this.file,
         this.fileConfiguration,
         "invalidDynamicDungeonMessage",
         "[EliteMobs] Not a valid dynamic dungeon!",
         true
      );
      matchAlreadyEndedMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when a player tries to join a match that has already ended"),
         this.file,
         this.fileConfiguration,
         "matchAlreadyEndedMessage",
         "[EliteMobs] This match already ended! Can't join it!",
         true
      );
      dungeonBrowserPlayersLabel = ConfigurationEngine.setString(
         List.of("Sets the players label shown in dungeon browser lore"), this.file, this.fileConfiguration, "dungeonBrowserPlayersLabel", "&2Players:", true
      );
      dungeonBrowserPlayerEntryFormat = ConfigurationEngine.setString(
         List.of("Sets the format for each player entry in dungeon browser lore", "$playerName is the placeholder for the player's display name"),
         this.file,
         this.fileConfiguration,
         "dungeonBrowserPlayerEntryFormat",
         "&f- $playerName",
         true
      );
      dungeonBrowserLevelLabel = ConfigurationEngine.setString(
         List.of("Sets the level label shown in dungeon browser lore", "$level is the placeholder for the dungeon level"),
         this.file,
         this.fileConfiguration,
         "dungeonBrowserLevelLabel",
         "&7Level: &e$level",
         true
      );
      dungeonPreparingQueueMessage = ConfigurationEngine.setString(
         List.of("Sets the message shown when a player's dungeon preparation is queued", "$position is the placeholder for the queue position"),
         this.file,
         this.fileConfiguration,
         "dungeonPreparingQueueMessage",
         "[EliteMobs] Preparing your dungeon... (Queue position: $position)",
         true
      );
      dungeonPreparingMessage = ConfigurationEngine.setString(
         List.of("Sets the message shown when a player's dungeon is being prepared"),
         this.file,
         this.fileConfiguration,
         "dungeonPreparingMessage",
         "[EliteMobs] Preparing your dungeon...",
         true
      );
      dungeonPrepareFailedMessage = ConfigurationEngine.setString(
         List.of("Sets the message shown when dungeon preparation fails during the async phase"),
         this.file,
         this.fileConfiguration,
         "dungeonPrepareFailedMessage",
         "[EliteMobs] Failed to prepare dungeon. Please try again.",
         true
      );
      dungeonLoadFailedMessage = ConfigurationEngine.setString(
         List.of("Sets the message shown when dungeon loading fails during the sync phase"),
         this.file,
         this.fileConfiguration,
         "dungeonLoadFailedMessage",
         "[EliteMobs] Failed to load dungeon. Please try again.",
         true
      );
      dungeonCancelledShutdownMessage = ConfigurationEngine.setString(
         List.of("Sets the message shown when dungeon preparation is cancelled due to server shutdown"),
         this.file,
         this.fileConfiguration,
         "dungeonCancelledShutdownMessage",
         "[EliteMobs] Dungeon preparation cancelled due to server shutdown.",
         true
      );
      dungeonDataFailedMessage = ConfigurationEngine.setString(
         List.of("Sets the message shown when dungeon data cannot be found", "$dungeon is the placeholder for the dungeon config name"),
         this.file,
         this.fileConfiguration,
         "dungeonDataFailedMessage",
         "[EliteMobs] Failed to get data for dungeon $dungeon! The dungeon will not start.",
         true
      );
      dungeonNoPermissionMessage = ConfigurationEngine.setString(
         List.of("Sets the message shown when a player doesn't have permission for a dungeon"),
         this.file,
         this.fileConfiguration,
         "dungeonNoPermissionMessage",
         "[EliteMobs] You don't have the permission to go to this dungeon!",
         true
      );
      dungeonCancelledEventMessage = ConfigurationEngine.setString(
         List.of("Sets the message shown when the instancing event is cancelled"),
         this.file,
         this.fileConfiguration,
         "dungeonCancelledEventMessage",
         "[EliteMobs] Something cancelled the instancing event! The dungeon will not start.",
         true
      );
      dungeonCopyFailedMessage = ConfigurationEngine.setString(
         List.of("Sets the message shown when world copy fails"),
         this.file,
         this.fileConfiguration,
         "dungeonCopyFailedMessage",
         "[EliteMobs] Failed to copy the world! Report this to the dev. The dungeon will not start.",
         true
      );
      dungeonWorldLoadFailedMessage = ConfigurationEngine.setString(
         List.of("Sets the message shown when world loading fails"),
         this.file,
         this.fileConfiguration,
         "dungeonWorldLoadFailedMessage",
         "[EliteMobs] Failed to load the world! Report this to the dev. The dungeon will not start.",
         true
      );
      dungeonDifficultyMessage = ConfigurationEngine.setString(
         List.of(
            "Sets the message shown when a player joins a dungeon with level sync",
            "$difficulty is the placeholder for the difficulty name",
            "$levelSync is the placeholder for the level sync cap"
         ),
         this.file,
         this.fileConfiguration,
         "dungeonDifficultyMessage",
         "[EliteMobs] Dungeon difficulty is set to $difficulty! Level sync caps your item level to $levelSync.",
         true
      );
      dynamicDungeonDataFailedMessage = ConfigurationEngine.setString(
         List.of("Sets the message shown when dynamic dungeon data cannot be found", "$dungeon is the placeholder for the dungeon config name"),
         this.file,
         this.fileConfiguration,
         "dynamicDungeonDataFailedMessage",
         "[EliteMobs] Failed to get data for dynamic dungeon $dungeon! The dungeon will not start.",
         true
      );
      dynamicDungeonNoPermissionMessage = ConfigurationEngine.setString(
         List.of("Sets the message shown when a player doesn't have permission for a dynamic dungeon"),
         this.file,
         this.fileConfiguration,
         "dynamicDungeonNoPermissionMessage",
         "[EliteMobs] You don't have the permission to go to this dungeon!",
         true
      );
      dynamicDungeonWorldLoadFailedMessage = ConfigurationEngine.setString(
         List.of("Sets the message shown when world loading fails for a dynamic dungeon"),
         this.file,
         this.fileConfiguration,
         "dynamicDungeonWorldLoadFailedMessage",
         "[EliteMobs] Failed to load the world! Report this to the dev. The dungeon will not start.",
         true
      );
      dynamicDungeonLevelSetMessage = ConfigurationEngine.setString(
         List.of("Sets the message shown when a player joins a dynamic dungeon", "$level is the placeholder for the selected level"),
         this.file,
         this.fileConfiguration,
         "dynamicDungeonLevelSetMessage",
         "[EliteMobs] Dynamic dungeon level set to $level!",
         true
      );
      enchantNoChallengeMessage = ConfigurationEngine.setString(
         List.of("Sets the message shown when no challenge dungeons are installed"),
         this.file,
         this.fileConfiguration,
         "enchantNoChallengeMessage",
         "&8[EliteMobs] &cYou rolled challenge but your server has not installed any challenge dungeons! &2This will count as an automatic enchantment success.",
         true
      );
      enchantChallengeCompleteMessage = ConfigurationEngine.setString(
         List.of("Sets the message shown when an enchantment challenge is completed"),
         this.file,
         this.fileConfiguration,
         "enchantChallengeCompleteMessage",
         "&8[EliteMobs] &2Challenge complete! Instance will close in 10 seconds. &6You can leave earlier by doing &9/em quit&6!",
         true
      );
      enchantChallengeSuccessMessage = ConfigurationEngine.setString(
         List.of("Sets the message shown when an enchantment challenge succeeds"),
         this.file,
         this.fileConfiguration,
         "enchantChallengeSuccessMessage",
         "&8[EliteMobs] &2You succeeded your item enchantment challenge! Your item has been successfully enchanted.",
         true
      );
      enchantCriticalFailureMessage = ConfigurationEngine.setString(
         List.of("Sets the message shown on critical enchantment failure", "$item is the placeholder for the item display name"),
         this.file,
         this.fileConfiguration,
         "enchantCriticalFailureMessage",
         "&8[EliteMobs] &4Critical enchantment failure! You have lost $item!",
         true
      );
      enchantChallengeFailedMessage = ConfigurationEngine.setString(
         List.of("Sets the message shown when an enchantment challenge fails", "$item is the placeholder for the item display name"),
         this.file,
         this.fileConfiguration,
         "enchantChallengeFailedMessage",
         "&8[EliteMobs] &cYou have failed the enchantment challenge! Your item $item was not enchanted.",
         true
      );
      contentInstalledMessage = ConfigurationEngine.setString(
         List.of("Sets the message shown when content is successfully installed", "$name is the placeholder for the content name"),
         this.file,
         this.fileConfiguration,
         "contentInstalledMessage",
         "[EliteMobs] Successfully installed $name! To uninstall, do /em setup again and click on this content again.",
         true
      );
      contentUninstallFailedMessage = ConfigurationEngine.setString(
         List.of("Sets the message shown when content uninstall fails", "$name is the placeholder for the content name"),
         this.file,
         this.fileConfiguration,
         "contentUninstallFailedMessage",
         "&c[EliteMobs] Failed to uninstall $name! There may still be players in the world.",
         true
      );
      contentUninstalledMessage = ConfigurationEngine.setString(
         List.of("Sets the message shown when content is successfully uninstalled", "$name is the placeholder for the content name"),
         this.file,
         this.fileConfiguration,
         "contentUninstalledMessage",
         "&a[EliteMobs] Successfully uninstalled $name!",
         true
      );
      dynamicDungeonAccessMessage1 = ConfigurationEngine.setString(
         List.of("Sets the first access message shown when a dynamic dungeon is installed"),
         this.file,
         this.fileConfiguration,
         "dynamicDungeonAccessMessage1",
         "&6Dynamic dungeons must be accessed either through the &a/em &6menu or an NPC! NPCs for premade EliteMobs content can be found at the Adventurer's Guild Hub map.",
         true
      );
      dynamicDungeonAccessMessage2 = ConfigurationEngine.setString(
         List.of("Sets the second access message shown when a dynamic dungeon is installed"),
         this.file,
         this.fileConfiguration,
         "dynamicDungeonAccessMessage2",
         "Dynamic dungeons allow you to select the level before entering based on your guild rank!",
         true
      );
      dynamicDungeonAccessMessage3 = ConfigurationEngine.setString(
         List.of("Sets the third access message shown when a dynamic dungeon is installed"),
         this.file,
         this.fileConfiguration,
         "dynamicDungeonAccessMessage3",
         "Remember that instanced dungeons create a world when you join them and remove that world when you are done playing in them!",
         true
      );
      contentInstalledLine1 = ConfigurationEngine.setString(
         List.of("Sets the first line of the tooltip for installed content in the setup menu."),
         this.file,
         this.fileConfiguration,
         "contentInstalledLine1",
         "&aContent is installed and up to date.",
         true
      );
      contentInstalledLine2 = ConfigurationEngine.setString(
         List.of("Sets the second line of the tooltip for installed content in the setup menu."),
         this.file,
         this.fileConfiguration,
         "contentInstalledLine2",
         "&7Click to uninstall.",
         true
      );
      contentNotInstalledLine1 = ConfigurationEngine.setString(
         List.of("Sets the first line of the tooltip for content that is downloaded but not installed."),
         this.file,
         this.fileConfiguration,
         "contentNotInstalledLine1",
         "Content is not installed!",
         true
      );
      contentNotInstalledLine2 = ConfigurationEngine.setString(
         List.of("Sets the second line of the tooltip for content that is downloaded but not installed."),
         this.file,
         this.fileConfiguration,
         "contentNotInstalledLine2",
         "Click to install!",
         true
      );
      contentPartialLine1 = ConfigurationEngine.setString(
         List.of("Sets the first line of the tooltip for partially installed content."),
         this.file,
         this.fileConfiguration,
         "contentPartialLine1",
         "Content partially installed!",
         true
      );
      contentPartialLine2 = ConfigurationEngine.setString(
         List.of("Sets the second line of the tooltip for partially installed content."),
         this.file,
         this.fileConfiguration,
         "contentPartialLine2",
         "This is either because you haven't downloaded all of it,",
         true
      );
      contentPartialLine3 = ConfigurationEngine.setString(
         List.of("Sets the third line of the tooltip for partially installed content."),
         this.file,
         this.fileConfiguration,
         "contentPartialLine3",
         "or because some elements have been manually disabled.",
         true
      );
      contentPartialLine4 = ConfigurationEngine.setString(
         List.of("Sets the fourth line of the tooltip for partially installed content."),
         this.file,
         this.fileConfiguration,
         "contentPartialLine4",
         "Click to download!",
         true
      );
      bundledContentPartialLine1 = ConfigurationEngine.setString(
         List.of("Sets the first line of the tooltip for partially installed content that ships inside the plugin jar (default items, events)."),
         this.file,
         this.fileConfiguration,
         "bundledContentPartialLine1",
         "Content partially installed!",
         true
      );
      bundledContentPartialLine2 = ConfigurationEngine.setString(
         List.of("Sets the second line of the tooltip for partially installed content that ships inside the plugin jar."),
         this.file,
         this.fileConfiguration,
         "bundledContentPartialLine2",
         "Some of this plugin-bundled content is disabled.",
         true
      );
      bundledContentPartialLine3 = ConfigurationEngine.setString(
         List.of("Sets the third line of the tooltip for partially installed content that ships inside the plugin jar."),
         this.file,
         this.fileConfiguration,
         "bundledContentPartialLine3",
         "Click to enable all of it!",
         true
      );
      contentToggleInProgressMessage = ConfigurationEngine.setString(
         List.of("Message shown when a content install/uninstall is clicked while a previous one is still saving."),
         this.file,
         this.fileConfiguration,
         "contentToggleInProgressMessage",
         "&cStill applying a previous content change! Wait for the reload to finish, then try again.",
         true
      );
      contentNotDownloadedLine1 = ConfigurationEngine.setString(
         List.of("Sets the first line of the tooltip for content that has not been downloaded."),
         this.file,
         this.fileConfiguration,
         "contentNotDownloadedLine1",
         "Content is not downloaded!",
         true
      );
      contentNotDownloadedLine2 = ConfigurationEngine.setString(
         List.of("Sets the second line of the tooltip for content that has not been downloaded."),
         this.file,
         this.fileConfiguration,
         "contentNotDownloadedLine2",
         "Click to download!",
         true
      );
      contentNeedAccessMessage = ConfigurationEngine.setString(
         List.of("Sets the tooltip message shown when access is required to download content."),
         this.file,
         this.fileConfiguration,
         "contentNeedAccessMessage",
         "&cYou need access to download this content!",
         true
      );
      contentAccessClickMessage = ConfigurationEngine.setString(
         List.of("Sets the tooltip message prompting the user to click for access info."),
         this.file,
         this.fileConfiguration,
         "contentAccessClickMessage",
         "&eClick to see how to get access.",
         true
      );
      contentAvailablePatreon = ConfigurationEngine.setString(
         List.of("Sets the tooltip message indicating content is available via Patreon."),
         this.file,
         this.fileConfiguration,
         "contentAvailablePatreon",
         "&7Available via: &6Patreon",
         true
      );
      contentAvailableItch = ConfigurationEngine.setString(
         List.of("Sets the tooltip message indicating content is available via itch.io."),
         this.file,
         this.fileConfiguration,
         "contentAvailableItch",
         "&7Available via: &ditch.io",
         true
      );
      contentUpdateAvailable = ConfigurationEngine.setString(
         List.of("Sets the tooltip message indicating an update is available."),
         this.file,
         this.fileConfiguration,
         "contentUpdateAvailable",
         "&eUpdate available!",
         true
      );
      contentUpdateClickMessage = ConfigurationEngine.setString(
         List.of("Sets the tooltip message prompting the user to click to update."),
         this.file,
         this.fileConfiguration,
         "contentUpdateClickMessage",
         "&aClick to update automatically!",
         true
      );
      contentUpdateNeedAccess = ConfigurationEngine.setString(
         List.of("Sets the tooltip message indicating access is required for the update."),
         this.file,
         this.fileConfiguration,
         "contentUpdateNeedAccess",
         "&cYou need access to download this update.",
         true
      );
      contentUpdateAccessClick = ConfigurationEngine.setString(
         List.of("Sets the tooltip message prompting the user to click for access info for updates."),
         this.file,
         this.fileConfiguration,
         "contentUpdateAccessClick",
         "&7Click to see how to get access.",
         true
      );
      contentDownloadLegacyMessage = ConfigurationEngine.setString(
         List.of("Sets the legacy download message shown when no Nightbreak slug is configured.", "$link is the placeholder for the download link."),
         this.file,
         this.fileConfiguration,
         "contentDownloadLegacyMessage",
         "&4Download this at &9$link &4!",
         true
      );
      contentDownloadSeparator = ConfigurationEngine.setString(
         List.of("Sets the separator line used in download messages."),
         this.file,
         this.fileConfiguration,
         "contentDownloadSeparator",
         NightbreakChatStyle.separator(),
         true
      );
      contentNightbreakPromptLine1 = ConfigurationEngine.setString(
         List.of("Sets the first line of the account token prompt."),
         this.file,
         this.fileConfiguration,
         "contentNightbreakPromptLine1",
         "&eThis content can be downloaded automatically after connecting this server.",
         true
      );
      contentNightbreakPromptLine2 = ConfigurationEngine.setString(
         List.of("Sets the second line of the account token prompt.", "Followed by a clickable nightbreak.io/account link."),
         this.file,
         this.fileConfiguration,
         "contentNightbreakPromptLine2",
         "<g:#B8860B:#DAA520>Step 1:</g> &7Get your token at: ",
         true
      );
      contentNightbreakPromptLine3 = ConfigurationEngine.setString(
         List.of("Sets the third line of the account token prompt."),
         this.file,
         this.fileConfiguration,
         "contentNightbreakPromptLine3",
         "<g:#B8860B:#DAA520>Step 2:</g> &7Run: &e/nightbreaklogin <your-token>",
         true
      );
      contentNightbreakPromptLine4 = ConfigurationEngine.setString(
         List.of("Sets the fourth line of the account token prompt.", "Followed by a clickable content link."),
         this.file,
         this.fileConfiguration,
         "contentNightbreakPromptLine4",
         "&8Or download manually at: ",
         true
      );
      contentCheckingAccessMessage = ConfigurationEngine.setString(
         List.of("Sets the message shown when checking access for content.", "$name is the placeholder for the content name."),
         this.file,
         this.fileConfiguration,
         "contentCheckingAccessMessage",
         "&7[EliteMobs] Checking access for $name...",
         true
      );
      contentAccessFailedMessage = ConfigurationEngine.setString(
         List.of("Sets the message shown when access checking fails."),
         this.file,
         this.fileConfiguration,
         "contentAccessFailedMessage",
         "&c[EliteMobs] Failed to check access. Try again later or download manually:",
         true
      );
      contentAccessFailedLink = ConfigurationEngine.setString(
         List.of("Sets the link shown when access checking fails."),
         this.file,
         this.fileConfiguration,
         "contentAccessFailedLink",
         "&9https://nightbreak.io/plugin/elitemobs",
         true
      );
      contentInvalidTokenMessage = ConfigurationEngine.setString(
         List.of("Sets the message shown when Nightbreak rejects the saved token."),
         this.file,
         this.fileConfiguration,
         "contentInvalidTokenMessage",
         "&e[EliteMobs] The saved account token needs to be updated.",
         true
      );
      contentInvalidTokenInstructions = ConfigurationEngine.setString(
         List.of("Sets the follow-up message shown after an invalid account token.", "Followed by a clickable nightbreak.io/account link."),
         this.file,
         this.fileConfiguration,
         "contentInvalidTokenInstructions",
         "&7Get a new token here, then run &e/nightbreaklogin <token>&7: ",
         true
      );
      contentDownloadedReloadMessage = ConfigurationEngine.setString(
         List.of("Sets the message shown when content has been downloaded and a reload is triggered."),
         this.file,
         this.fileConfiguration,
         "contentDownloadedReloadMessage",
         "&a[EliteMobs] Content downloaded! Reloading EliteMobs...",
         true
      );
      contentNoAccessMessage = ConfigurationEngine.setString(
         List.of("Sets the message shown when the player does not have access to content.", "$name is the placeholder for the content name."),
         this.file,
         this.fileConfiguration,
         "contentNoAccessMessage",
         "&cYou don't have access to: &f$name",
         true
      );
      contentGetAccessMessage = ConfigurationEngine.setString(
         List.of("Sets the message prompting the player on how to get access."),
         this.file,
         this.fileConfiguration,
         "contentGetAccessMessage",
         "&eYou can get access through:",
         true
      );
      contentNightbreakLink = ConfigurationEngine.setString(
         List.of("Sets the plugin page link prefix in access info.", "Followed by a clickable nightbreak.io/elitemobs link."),
         this.file,
         this.fileConfiguration,
         "contentNightbreakLink",
         "&a• Plugin page: ",
         true
      );
      contentPatreonLink = ConfigurationEngine.setString(
         List.of("Sets the Patreon link prefix in access info.", "Followed by a clickable Patreon link."),
         this.file,
         this.fileConfiguration,
         "contentPatreonLink",
         "&6• Patreon: ",
         true
      );
      contentItchLink = ConfigurationEngine.setString(
         List.of("Sets the itch.io link prefix in access info.", "Followed by a clickable itch.io link."),
         this.file,
         this.fileConfiguration,
         "contentItchLink",
         "&d• itch.io: ",
         true
      );
      contentLinkAccountMessage = ConfigurationEngine.setString(
         List.of("Sets the message telling the player how to link their account after purchasing."),
         this.file,
         this.fileConfiguration,
         "contentLinkAccountMessage",
         "&7After purchasing, use &e/nightbreaklogin <token> &7to link your account.",
         true
      );
      instancedDungeonInstalledMessage = ConfigurationEngine.setString(
         List.of("Sets the message shown when an instanced dungeon is installed.", "$name is the placeholder for the dungeon filename."),
         this.file,
         this.fileConfiguration,
         "instancedDungeonInstalledMessage",
         "&2Dungeon $name installed!",
         true
      );
      instancedDungeonAccessMessage = ConfigurationEngine.setString(
         List.of("Sets the access message shown when an instanced dungeon is installed."),
         this.file,
         this.fileConfiguration,
         "instancedDungeonAccessMessage",
         "&6Instanced dungeons must be accessed either through the &a/em &6menu or an NPC! NPCs for premade EliteMobs content can be found at the Adventurer's Guild Hub map.",
         true
      );
      instancedDungeonInstallNote = ConfigurationEngine.setString(
         List.of("Sets the note reminding players that instanced dungeons create and remove worlds."),
         this.file,
         this.fileConfiguration,
         "instancedDungeonInstallNote",
         "Remember that instanced dungeons create a world when you join them and remove that world when you are done playing in them!",
         true
      );
      enchantmentDungeonInstalledMessage = ConfigurationEngine.setString(
         List.of("Sets the message shown when an enchantment instanced dungeon is installed."),
         this.file,
         this.fileConfiguration,
         "enchantmentDungeonInstalledMessage",
         "&8[EliteMobs] &2Enchantment instanced dungeon installed! This dungeon can only be accessed when attempting to enchant items and getting a challenge as a result!",
         true
      );
      instancePunchToRez = ConfigurationEngine.setString(
         List.of("Sets the text shown above death banners instructing players to punch to resurrect."),
         this.file,
         this.fileConfiguration,
         "instancePunchToRez",
         "&2Punch to rez!",
         true
      );
      instanceLivesLeft = ConfigurationEngine.setString(
         List.of("Sets the text shown below death banners indicating remaining lives.", "$amount is the placeholder for the number of lives remaining."),
         this.file,
         this.fileConfiguration,
         "instanceLivesLeft",
         "$amount lives left!",
         true
      );
      skillMigrationSeparator = ConfigurationEngine.setString(
         List.of("Sets the separator line used in the skill migration notification."),
         this.file,
         this.fileConfiguration,
         "skillMigrationSeparator",
         NightbreakChatStyle.separator(),
         true
      );
      skillMigrationTitle = ConfigurationEngine.setString(
         List.of("Sets the title of the skill migration notification."),
         this.file,
         this.fileConfiguration,
         "skillMigrationTitle",
         "<g:#7B2FBE:#A855F7>&lSKILL SYSTEM ACTIVATED</g>",
         true
      );
      skillMigrationLine1 = ConfigurationEngine.setString(
         List.of("Sets the first line of the skill migration notification."),
         this.file,
         this.fileConfiguration,
         "skillMigrationLine1",
         "&7EliteMobs now uses a new skill leveling system!",
         true
      );
      skillMigrationLine2 = ConfigurationEngine.setString(
         List.of("Sets the second line of the skill migration notification."),
         this.file,
         this.fileConfiguration,
         "skillMigrationLine2",
         "&eYou now level individual skills by killing mobs:",
         true
      );
      skillMigrationLine3 = ConfigurationEngine.setString(
         List.of("Sets the third line of the skill migration notification."),
         this.file,
         this.fileConfiguration,
         "skillMigrationLine3",
         "&7- Swords, Axes, Bows, Crossbows, Tridents, Hoes",
         true
      );
      skillMigrationLine4 = ConfigurationEngine.setString(
         List.of("Sets the fourth line of the skill migration notification."),
         this.file,
         this.fileConfiguration,
         "skillMigrationLine4",
         "&7- Armor levels on every kill at 1/3 XP rate",
         true
      );
      skillMigrationLine5 = ConfigurationEngine.setString(
         List.of("Sets the fifth line of the skill migration notification."),
         this.file,
         this.fileConfiguration,
         "skillMigrationLine5",
         "&cAll players start fresh with Level 1 in all skills.",
         true
      );
      skillMigrationLine6 = ConfigurationEngine.setString(
         List.of("Sets the sixth line of the skill migration notification.", "Followed by a clickable /em command, then the suffix."),
         this.file,
         this.fileConfiguration,
         "skillMigrationLine6",
         "&7Type ",
         true
      );
      skillMigrationLine6Suffix = ConfigurationEngine.setString(
         List.of("Sets the text after the clickable /em command in the skill migration notification."),
         this.file,
         this.fileConfiguration,
         "skillMigrationLine6Suffix",
         " &7and click &5Skills &7to view your progress!",
         true
      );
      dynamicDungeonInstalledMessage = ConfigurationEngine.setString(
         List.of("Sets the message shown when a dynamic dungeon is installed.", "$name is the placeholder for the dungeon filename."),
         this.file,
         this.fileConfiguration,
         "dynamicDungeonInstalledMessage",
         "&2Dynamic dungeon $name installed!",
         true
      );
      vorpalStrikeReadyMessage = ConfigurationEngine.setString(
         List.of("Sets the message shown when Vorpal Strike comes off cooldown."),
         this.file,
         this.fileConfiguration,
         "vorpalStrikeReadyMessage",
         "§6Vorpal Strike §aready!",
         true
      );
      soulSiphonDecayMessage = ConfigurationEngine.setString(
         List.of("Sets the action bar message shown when Soul Siphon stacks decay."),
         this.file,
         this.fileConfiguration,
         "soulSiphonDecayMessage",
         "§7Soul Siphon stacks decayed",
         true
      );
      spectralScytheCooldownMessage = ConfigurationEngine.setString(
         List.of("Sets the action bar message shown when Spectral Scythe is on cooldown.", "$time is the placeholder for the remaining cooldown in seconds."),
         this.file,
         this.fileConfiguration,
         "spectralScytheCooldownMessage",
         "§cSpectral Scythe on cooldown: $times",
         true
      );
      skillLevelUpTitle = ConfigurationEngine.setString(
         List.of("Sets the title shown when a player levels up a skill."), this.file, this.fileConfiguration, "skillLevelUpTitle", "&6&lLEVEL UP!", true
      );
      skillLevelUpSubtitle = ConfigurationEngine.setString(
         List.of(
            "Sets the subtitle shown when a player levels up a skill.",
            "$skill is the placeholder for the skill display name.",
            "$level is the placeholder for the new level."
         ),
         this.file,
         this.fileConfiguration,
         "skillLevelUpSubtitle",
         "&e$skill &7→ &aLevel $level",
         true
      );
      skillLevelUpBroadcast = ConfigurationEngine.setString(
         List.of(
            "Sets the server-wide broadcast message when a player levels up a skill.",
            "$player is the placeholder for the player name.",
            "$skill is the placeholder for the skill display name.",
            "$level is the placeholder for the new level."
         ),
         this.file,
         this.fileConfiguration,
         "skillLevelUpBroadcast",
         "&6$player &7reached &e$skill &7level &a$level&7!",
         true
      );
      skillLevelUpBroadcastEnabled = ConfigurationEngine.setBoolean(
         List.of(
            "Sets whether the server-wide skill level-up broadcast (every 5 levels) is sent.",
            "When false, the personal level-up title/sound/particles still play, but no chat broadcast goes out."
         ),
         this.fileConfiguration,
         "skillLevelUpBroadcastEnabled",
         true
      );
      skillXpNoGainMessage = ConfigurationEngine.setString(
         List.of(
            "Sets the message shown when a player gains no XP because the mob is too low level.",
            "$mobLevel is the placeholder for the mob's level.",
            "$skill is the placeholder for the skill name.",
            "$playerLevel is the placeholder for the player's combat level."
         ),
         this.file,
         this.fileConfiguration,
         "skillXpNoGainMessage",
         "&7[&eEliteMobs&7] &cNo XP gained &7- this mob (level &f$mobLevel&7) is too far below your &e$skill &7level (&f$playerLevel&7). Fight stronger mobs!",
         true
      );
      skillXpCappedMessage = ConfigurationEngine.setString(
         List.of(
            "Sets the message shown when XP is capped because the mob is too high level.",
            "$mobLevel is the placeholder for the mob's level.",
            "$skill is the placeholder for the skill name.",
            "$playerLevel is the placeholder for the player's combat level.",
            "$xp is the placeholder for the capped level."
         ),
         this.file,
         this.fileConfiguration,
         "skillXpCappedMessage",
         "&7[&eEliteMobs&7] &6XP capped &7- this mob (level &f$mobLevel&7) is below your &e$skill &7level (&f$playerLevel&7). XP reduced to &f$xp&7.",
         true
      );
      skillActivationFormat = ConfigurationEngine.setString(
         List.of("Sets the action bar format shown when a skill activates.", "$skillName is the placeholder for the skill name."),
         this.file,
         this.fileConfiguration,
         "skillActivationFormat",
         "&6&l$skillName!",
         true
      );
      skillStackFormat = ConfigurationEngine.setString(
         List.of(
            "Sets the action bar format shown when a stacking skill gains stacks.",
            "$skillName is the placeholder for the skill name.",
            "$current is the placeholder for the current stack count.",
            "$max is the placeholder for the maximum stack count."
         ),
         this.file,
         this.fileConfiguration,
         "skillStackFormat",
         "&6&l$skillName &7($current/$max stacks)",
         true
      );
      tazeShockedTitle = ConfigurationEngine.setString(
         List.of("Title shown on screen when a player is hit by the Taze power.", "Leave empty for subtitle only."),
         this.file,
         this.fileConfiguration,
         "tazeShockedTitle",
         "",
         true
      );
      tazeShockedSubtitle = ConfigurationEngine.setString(
         List.of("Sets the subtitle shown when a player is tazed by an elite mob."), this.file, this.fileConfiguration, "tazeShockedSubtitle", "Shocked!", true
      );
      legionsDisciplineBrokenMessage = ConfigurationEngine.setString(
         List.of("Sets the action bar message shown when discipline stacks are broken."),
         this.file,
         this.fileConfiguration,
         "legionsDisciplineBrokenMessage",
         "&7Combat rhythm lost...",
         true
      );
      avatarFadesMessage = ConfigurationEngine.setString(
         List.of("Sets the action bar message shown when the Avatar of Judgment buff expires."),
         this.file,
         this.fileConfiguration,
         "avatarFadesMessage",
         "&7Avatar fades...",
         true
      );
      modelsAutoInstallMessage = ConfigurationEngine.setString(
         List.of("Sets the message shown when a player tries to manually install models."),
         this.file,
         this.fileConfiguration,
         "modelsAutoInstallMessage",
         "Models are installed automatically, you can't do this manually!",
         true
      );
      modelsCannotUninstallMessage = ConfigurationEngine.setString(
         List.of("Sets the message shown when a player tries to uninstall models via the menu."),
         this.file,
         this.fileConfiguration,
         "modelsCannotUninstallMessage",
         "Models can't currently be uninstalled via this menu! Delete the Models folder in EliteMobs if you want to remove them.",
         true
      );
      itemsInstallingMessage = ConfigurationEngine.setString(
         List.of("Sets the message shown when items are being installed.", "$count is the placeholder for the number of items."),
         this.file,
         this.fileConfiguration,
         "itemsInstallingMessage",
         "&2Installing $count items...",
         true
      );
      itemsUninstallingMessage = ConfigurationEngine.setString(
         List.of("Sets the message shown when items are being uninstalled.", "$count is the placeholder for the number of items."),
         this.file,
         this.fileConfiguration,
         "itemsUninstallingMessage",
         "&2Uninstalling $count items...",
         true
      );
      itemsSavingMessage = ConfigurationEngine.setString(
         List.of("Sets the message shown when item files are being saved.", "$count is the placeholder for the number of items."),
         this.file,
         this.fileConfiguration,
         "itemsSavingMessage",
         "&2Saving $count item files...",
         true
      );
      itemsReloadingMessage = ConfigurationEngine.setString(
         List.of("Sets the message shown when EliteMobs is reloading to apply item changes."),
         this.file,
         this.fileConfiguration,
         "itemsReloadingMessage",
         "Reloading EliteMobs to apply item changes!",
         true
      );
      eventsInstallingMessage = ConfigurationEngine.setString(
         List.of("Sets the message shown when events are being installed.", "$count is the placeholder for the number of events."),
         this.file,
         this.fileConfiguration,
         "eventsInstallingMessage",
         "&2Installing $count events...",
         true
      );
      eventsUninstallingMessage = ConfigurationEngine.setString(
         List.of("Sets the message shown when events are being uninstalled.", "$count is the placeholder for the number of events."),
         this.file,
         this.fileConfiguration,
         "eventsUninstallingMessage",
         "&2Uninstalling $count events...",
         true
      );
      eventsSavingMessage = ConfigurationEngine.setString(
         List.of("Sets the message shown when event files are being saved.", "$count is the placeholder for the number of events."),
         this.file,
         this.fileConfiguration,
         "eventsSavingMessage",
         "&2Saving $count event files...",
         true
      );
      eventsReloadingMessage = ConfigurationEngine.setString(
         List.of("Sets the message shown when EliteMobs is reloading to apply event changes."),
         this.file,
         this.fileConfiguration,
         "eventsReloadingMessage",
         "Reloading EliteMobs to apply event changes!",
         true
      );
      contentConfigurationSaveFailedMessage = ConfigurationEngine.setString(
         List.of("Sets the message shown when a content package configuration could not be saved."),
         this.file,
         this.fileConfiguration,
         "contentConfigurationSaveFailedMessage",
         "&cCould not save all content configuration files. Check the console; EliteMobs was not reloaded.",
         true
      );
   }

   @Generated
   public static String getInstancedDungeonTitle() {
      return instancedDungeonTitle;
   }

   @Generated
   public static List<String> getInstancedDungeonDescription() {
      return instancedDungeonDescription;
   }

   @Generated
   public static String getDungeonJoinAsPlayerText() {
      return dungeonJoinAsPlayerText;
   }

   @Generated
   public static String getDungeonJoinAsSpectatorText() {
      return dungeonJoinAsSpectatorText;
   }

   @Generated
   public static boolean isAllowSpectatorsInInstancedContent() {
      return allowSpectatorsInInstancedContent;
   }

   @Generated
   public static String getInstancedDungeonCompleteMessage() {
      return instancedDungeonCompleteMessage;
   }

   @Generated
   public static String getInstancedDungeonClosingInstanceMessage() {
      return instancedDungeonClosingInstanceMessage;
   }

   @Generated
   public static boolean isFriendlyFireInDungeons() {
      return friendlyFireInDungeons;
   }

   @Generated
   public static double getFireDamageMultiplier() {
      return fireDamageMultiplier;
   }

   @Generated
   public static double getPoisonDamageMultiplier() {
      return poisonDamageMultiplier;
   }

   @Generated
   public static double getWitherDamageMultiplier() {
      return witherDamageMultiplier;
   }

   @Generated
   public static boolean isEnableDungeonFoodRegeneration() {
      return enableDungeonFoodRegeneration;
   }

   @Generated
   public static int getDungeonFoodRegenerationCombatTimeoutSeconds() {
      return dungeonFoodRegenerationCombatTimeoutSeconds;
   }

   @Generated
   public static int getDungeonFoodRegenerationIntervalSeconds() {
      return dungeonFoodRegenerationIntervalSeconds;
   }

   @Generated
   public static String getDungeonFoodRegenerationCombatStartMessage() {
      return dungeonFoodRegenerationCombatStartMessage;
   }

   @Generated
   public static String getDungeonFoodRegenerationCombatEndMessage() {
      return dungeonFoodRegenerationCombatEndMessage;
   }

   @Generated
   public static String getDynamicDungeonLevelSelectionMenuTitle() {
      return dynamicDungeonLevelSelectionMenuTitle;
   }

   @Generated
   public static String getDynamicDungeonLevelSelectionItemTitle() {
      return dynamicDungeonLevelSelectionItemTitle;
   }

   @Generated
   public static String getDynamicDungeonLevelSelectionClickToSelect() {
      return dynamicDungeonLevelSelectionClickToSelect;
   }

   @Generated
   public static String getDynamicDungeonLevelSelectionMobsWillBeLevel() {
      return dynamicDungeonLevelSelectionMobsWillBeLevel;
   }

   @Generated
   public static String getDynamicDungeonLevelSelectionRecommended() {
      return dynamicDungeonLevelSelectionRecommended;
   }

   @Generated
   public static String getDynamicDungeonLevelSelectionEasier() {
      return dynamicDungeonLevelSelectionEasier;
   }

   @Generated
   public static String getDynamicDungeonLevelSelectionHarder() {
      return dynamicDungeonLevelSelectionHarder;
   }

   @Generated
   public static String getDynamicDungeonDifficultySelectionMenuTitle() {
      return dynamicDungeonDifficultySelectionMenuTitle;
   }

   @Generated
   public static String getDynamicDungeonDifficultySelectionSelectedLevel() {
      return dynamicDungeonDifficultySelectionSelectedLevel;
   }

   @Generated
   public static String getDungeonLockoutTitle() {
      return dungeonLockoutTitle;
   }

   @Generated
   public static String getDungeonLockoutSubtitle() {
      return dungeonLockoutSubtitle;
   }

   @Generated
   public static String getDungeonLockoutChatMessage() {
      return dungeonLockoutChatMessage;
   }

   @Generated
   public static String getInvalidInstancedDungeonMessage() {
      return invalidInstancedDungeonMessage;
   }

   @Generated
   public static String getInvalidDynamicDungeonMessage() {
      return invalidDynamicDungeonMessage;
   }

   @Generated
   public static String getMatchAlreadyEndedMessage() {
      return matchAlreadyEndedMessage;
   }

   @Generated
   public static String getDungeonBrowserPlayersLabel() {
      return dungeonBrowserPlayersLabel;
   }

   @Generated
   public static String getDungeonBrowserPlayerEntryFormat() {
      return dungeonBrowserPlayerEntryFormat;
   }

   @Generated
   public static String getDungeonBrowserLevelLabel() {
      return dungeonBrowserLevelLabel;
   }

   @Generated
   public static String getDungeonPreparingQueueMessage() {
      return dungeonPreparingQueueMessage;
   }

   @Generated
   public static String getDungeonPreparingMessage() {
      return dungeonPreparingMessage;
   }

   @Generated
   public static String getDungeonPrepareFailedMessage() {
      return dungeonPrepareFailedMessage;
   }

   @Generated
   public static String getDungeonLoadFailedMessage() {
      return dungeonLoadFailedMessage;
   }

   @Generated
   public static String getDungeonCancelledShutdownMessage() {
      return dungeonCancelledShutdownMessage;
   }

   @Generated
   public static String getDungeonDataFailedMessage() {
      return dungeonDataFailedMessage;
   }

   @Generated
   public static String getDungeonNoPermissionMessage() {
      return dungeonNoPermissionMessage;
   }

   @Generated
   public static String getDungeonCancelledEventMessage() {
      return dungeonCancelledEventMessage;
   }

   @Generated
   public static String getDungeonCopyFailedMessage() {
      return dungeonCopyFailedMessage;
   }

   @Generated
   public static String getDungeonWorldLoadFailedMessage() {
      return dungeonWorldLoadFailedMessage;
   }

   @Generated
   public static String getDungeonDifficultyMessage() {
      return dungeonDifficultyMessage;
   }

   @Generated
   public static String getDynamicDungeonDataFailedMessage() {
      return dynamicDungeonDataFailedMessage;
   }

   @Generated
   public static String getDynamicDungeonNoPermissionMessage() {
      return dynamicDungeonNoPermissionMessage;
   }

   @Generated
   public static String getDynamicDungeonWorldLoadFailedMessage() {
      return dynamicDungeonWorldLoadFailedMessage;
   }

   @Generated
   public static String getDynamicDungeonLevelSetMessage() {
      return dynamicDungeonLevelSetMessage;
   }

   @Generated
   public static String getEnchantNoChallengeMessage() {
      return enchantNoChallengeMessage;
   }

   @Generated
   public static String getEnchantChallengeCompleteMessage() {
      return enchantChallengeCompleteMessage;
   }

   @Generated
   public static String getEnchantChallengeSuccessMessage() {
      return enchantChallengeSuccessMessage;
   }

   @Generated
   public static String getEnchantCriticalFailureMessage() {
      return enchantCriticalFailureMessage;
   }

   @Generated
   public static String getEnchantChallengeFailedMessage() {
      return enchantChallengeFailedMessage;
   }

   @Generated
   public static String getContentInstalledMessage() {
      return contentInstalledMessage;
   }

   @Generated
   public static String getContentUninstallFailedMessage() {
      return contentUninstallFailedMessage;
   }

   @Generated
   public static String getContentUninstalledMessage() {
      return contentUninstalledMessage;
   }

   @Generated
   public static String getDynamicDungeonAccessMessage1() {
      return dynamicDungeonAccessMessage1;
   }

   @Generated
   public static String getDynamicDungeonAccessMessage2() {
      return dynamicDungeonAccessMessage2;
   }

   @Generated
   public static String getDynamicDungeonAccessMessage3() {
      return dynamicDungeonAccessMessage3;
   }

   @Generated
   public static String getContentInstalledLine1() {
      return contentInstalledLine1;
   }

   @Generated
   public static String getContentInstalledLine2() {
      return contentInstalledLine2;
   }

   @Generated
   public static String getContentNotInstalledLine1() {
      return contentNotInstalledLine1;
   }

   @Generated
   public static String getContentNotInstalledLine2() {
      return contentNotInstalledLine2;
   }

   @Generated
   public static String getContentPartialLine1() {
      return contentPartialLine1;
   }

   @Generated
   public static String getContentPartialLine2() {
      return contentPartialLine2;
   }

   @Generated
   public static String getContentPartialLine3() {
      return contentPartialLine3;
   }

   @Generated
   public static String getContentPartialLine4() {
      return contentPartialLine4;
   }

   @Generated
   public static String getBundledContentPartialLine1() {
      return bundledContentPartialLine1;
   }

   @Generated
   public static String getBundledContentPartialLine2() {
      return bundledContentPartialLine2;
   }

   @Generated
   public static String getBundledContentPartialLine3() {
      return bundledContentPartialLine3;
   }

   @Generated
   public static String getContentToggleInProgressMessage() {
      return contentToggleInProgressMessage;
   }

   @Generated
   public static String getContentNotDownloadedLine1() {
      return contentNotDownloadedLine1;
   }

   @Generated
   public static String getContentNotDownloadedLine2() {
      return contentNotDownloadedLine2;
   }

   @Generated
   public static String getContentNeedAccessMessage() {
      return contentNeedAccessMessage;
   }

   @Generated
   public static String getContentAccessClickMessage() {
      return contentAccessClickMessage;
   }

   @Generated
   public static String getContentAvailablePatreon() {
      return contentAvailablePatreon;
   }

   @Generated
   public static String getContentAvailableItch() {
      return contentAvailableItch;
   }

   @Generated
   public static String getContentUpdateAvailable() {
      return contentUpdateAvailable;
   }

   @Generated
   public static String getContentUpdateClickMessage() {
      return contentUpdateClickMessage;
   }

   @Generated
   public static String getContentUpdateNeedAccess() {
      return contentUpdateNeedAccess;
   }

   @Generated
   public static String getContentUpdateAccessClick() {
      return contentUpdateAccessClick;
   }

   @Generated
   public static String getContentDownloadLegacyMessage() {
      return contentDownloadLegacyMessage;
   }

   @Generated
   public static String getContentDownloadSeparator() {
      return contentDownloadSeparator;
   }

   @Generated
   public static String getContentNightbreakPromptLine1() {
      return contentNightbreakPromptLine1;
   }

   @Generated
   public static String getContentNightbreakPromptLine2() {
      return contentNightbreakPromptLine2;
   }

   @Generated
   public static String getContentNightbreakPromptLine3() {
      return contentNightbreakPromptLine3;
   }

   @Generated
   public static String getContentNightbreakPromptLine4() {
      return contentNightbreakPromptLine4;
   }

   @Generated
   public static String getContentCheckingAccessMessage() {
      return contentCheckingAccessMessage;
   }

   @Generated
   public static String getContentAccessFailedMessage() {
      return contentAccessFailedMessage;
   }

   @Generated
   public static String getContentAccessFailedLink() {
      return contentAccessFailedLink;
   }

   @Generated
   public static String getContentInvalidTokenMessage() {
      return contentInvalidTokenMessage;
   }

   @Generated
   public static String getContentInvalidTokenInstructions() {
      return contentInvalidTokenInstructions;
   }

   @Generated
   public static String getContentDownloadedReloadMessage() {
      return contentDownloadedReloadMessage;
   }

   @Generated
   public static String getInstancedDungeonInstalledMessage() {
      return instancedDungeonInstalledMessage;
   }

   @Generated
   public static String getInstancedDungeonAccessMessage() {
      return instancedDungeonAccessMessage;
   }

   @Generated
   public static String getInstancedDungeonInstallNote() {
      return instancedDungeonInstallNote;
   }

   @Generated
   public static String getEnchantmentDungeonInstalledMessage() {
      return enchantmentDungeonInstalledMessage;
   }

   @Generated
   public static String getInstancePunchToRez() {
      return instancePunchToRez;
   }

   @Generated
   public static String getInstanceLivesLeft() {
      return instanceLivesLeft;
   }

   @Generated
   public static String getSkillMigrationSeparator() {
      return skillMigrationSeparator;
   }

   @Generated
   public static String getSkillMigrationTitle() {
      return skillMigrationTitle;
   }

   @Generated
   public static String getSkillMigrationLine1() {
      return skillMigrationLine1;
   }

   @Generated
   public static String getSkillMigrationLine2() {
      return skillMigrationLine2;
   }

   @Generated
   public static String getSkillMigrationLine3() {
      return skillMigrationLine3;
   }

   @Generated
   public static String getSkillMigrationLine4() {
      return skillMigrationLine4;
   }

   @Generated
   public static String getSkillMigrationLine5() {
      return skillMigrationLine5;
   }

   @Generated
   public static String getSkillMigrationLine6() {
      return skillMigrationLine6;
   }

   @Generated
   public static String getSkillMigrationLine6Suffix() {
      return skillMigrationLine6Suffix;
   }

   @Generated
   public static String getDynamicDungeonInstalledMessage() {
      return dynamicDungeonInstalledMessage;
   }

   @Generated
   public static String getVorpalStrikeReadyMessage() {
      return vorpalStrikeReadyMessage;
   }

   @Generated
   public static String getSoulSiphonDecayMessage() {
      return soulSiphonDecayMessage;
   }

   @Generated
   public static String getSpectralScytheCooldownMessage() {
      return spectralScytheCooldownMessage;
   }

   @Generated
   public static String getSkillLevelUpTitle() {
      return skillLevelUpTitle;
   }

   @Generated
   public static String getSkillLevelUpSubtitle() {
      return skillLevelUpSubtitle;
   }

   @Generated
   public static String getSkillLevelUpBroadcast() {
      return skillLevelUpBroadcast;
   }

   @Generated
   public static boolean isSkillLevelUpBroadcastEnabled() {
      return skillLevelUpBroadcastEnabled;
   }

   @Generated
   public static String getSkillXpNoGainMessage() {
      return skillXpNoGainMessage;
   }

   @Generated
   public static String getSkillXpCappedMessage() {
      return skillXpCappedMessage;
   }

   @Generated
   public static String getSkillActivationFormat() {
      return skillActivationFormat;
   }

   @Generated
   public static String getSkillStackFormat() {
      return skillStackFormat;
   }

   @Generated
   public static String getTazeShockedTitle() {
      return tazeShockedTitle;
   }

   @Generated
   public static String getTazeShockedSubtitle() {
      return tazeShockedSubtitle;
   }

   @Generated
   public static String getLegionsDisciplineBrokenMessage() {
      return legionsDisciplineBrokenMessage;
   }

   @Generated
   public static String getAvatarFadesMessage() {
      return avatarFadesMessage;
   }

   @Generated
   public static String getModelsAutoInstallMessage() {
      return modelsAutoInstallMessage;
   }

   @Generated
   public static String getModelsCannotUninstallMessage() {
      return modelsCannotUninstallMessage;
   }

   @Generated
   public static String getItemsInstallingMessage() {
      return itemsInstallingMessage;
   }

   @Generated
   public static String getItemsUninstallingMessage() {
      return itemsUninstallingMessage;
   }

   @Generated
   public static String getItemsSavingMessage() {
      return itemsSavingMessage;
   }

   @Generated
   public static String getItemsReloadingMessage() {
      return itemsReloadingMessage;
   }

   @Generated
   public static String getEventsInstallingMessage() {
      return eventsInstallingMessage;
   }

   @Generated
   public static String getEventsUninstallingMessage() {
      return eventsUninstallingMessage;
   }

   @Generated
   public static String getEventsSavingMessage() {
      return eventsSavingMessage;
   }

   @Generated
   public static String getEventsReloadingMessage() {
      return eventsReloadingMessage;
   }

   @Generated
   public static String getContentConfigurationSaveFailedMessage() {
      return contentConfigurationSaveFailedMessage;
   }

   @Generated
   public static String getContentNoAccessMessage() {
      return contentNoAccessMessage;
   }

   @Generated
   public static String getContentGetAccessMessage() {
      return contentGetAccessMessage;
   }

   @Generated
   public static String getContentNightbreakLink() {
      return contentNightbreakLink;
   }

   @Generated
   public static String getContentPatreonLink() {
      return contentPatreonLink;
   }

   @Generated
   public static String getContentItchLink() {
      return contentItchLink;
   }

   @Generated
   public static String getContentLinkAccountMessage() {
      return contentLinkAccountMessage;
   }
}
