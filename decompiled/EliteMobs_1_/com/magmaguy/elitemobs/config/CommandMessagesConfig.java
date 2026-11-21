package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.magmacore.config.ConfigurationFile;
import com.magmaguy.elitemobs.magmacore.nightbreak.NightbreakChatStyle;
import java.util.List;
import lombok.Generated;

public class CommandMessagesConfig extends ConfigurationFile {
   private static String payNiceTryMessage;
   private static String payPlayerNotOnlineMessage;
   private static String currencyPlayerNotValidMessage;
   private static String currencyDataNotReadyMessage;
   private static String currencyAddedMessage;
   private static String currencyNowHasMessage;
   private static String currencyAddedAllMessage;
   private static String currencySubtractedMessage;
   private static String currencySetMessage;
   private static String currencyCheckMessage;
   private static String dungeonNotValidMessage;
   private static String dungeonNotInstalledMessage;
   private static String alreadyInInstanceMessage;
   private static String dungeonTeleportNotSetMessage;
   private static String dungeonLockoutPlayerNotValidMessage;
   private static String dungeonLockoutResetSuccessMessage;
   private static String unbindForceSuccessMessage;
   private static String unbindForceNotSoulboundMessage;
   private static String unbindForceAllMessage;
   private static String questPlayerNotValidMessage;
   private static String questResetSuccessMessage;
   private static String questNameNotValidMessage;
   private static String questCompletedSuccessMessage;
   private static String noActiveQuestsMessage;
   private static String questNotFoundMessage;
   private static String noSafeSpawnMessage;
   private static String bossNotValidRegionalMessage;
   private static String leashRadiusFailedMessage;
   private static String invalidEntityTypeMessage;
   private static String filenameNotValidMessage;
   private static String worldNotValidMessage;
   private static String invalidPowerMessage;
   private static String killedElitesMessage;
   private static String killedEliteTypeMessage;
   private static String invalidEliteTypeMessage;
   private static String removeElitesOffMessage;
   private static String removeElitesOnMessage;
   private static String removedNotEliteMobsMessage;
   private static String removedHijackedMessage;
   private static String removedTreasureChestMessage;
   private static String removedSpawnLocationMessage;
   private static String invalidNpcFilenameMessage;
   private static String gaveItemMessage;
   private static String invalidItemFilenameMessage;
   private static String invalidPlayerForItemMessage;
   private static String eventQueuedMessage;
   private static String invalidEventMessage;
   private static String eventCancelledMessage;
   private static String eventNoSpawnTypeMessage;
   private static String eventInvalidSpawnTypeMessage;
   private static String eventNoValidBossesMessage;
   private static String invalidMinidungeonMessage;
   private static String minidungeonNotInstalledMessage;
   private static String invalidAnchorPointMessage;
   private static String packageDoneMessage;
   private static String packageDontForgetMessage;
   private static String packageZipFailedMessage;
   private static String packageZippedMessage;
   private static String packageNeedsDungeonNameMessage;
   private static String packageNeedsNumberMessage;
   private static String packageNoSubdirectoryMessage;
   private static String packageFailedDirectoryMessage;
   private static String missingPermissionMessage;
   private static String regionalBossStatsMessage;
   private static String languageNotFoundMessage;
   private static String languageListPrefix;
   private static String languageSetEnglishMessage;
   private static String languageGeneratingCustomMessage;
   private static String languageGenerateFailedMessage;
   private static String languageGenerateSuccessMessage;
   private static String languageSetCustomMessage;
   private static String languageDownloadingMessage;
   private static String languageDownloadFailedMessage;
   private static String languageDownloadSuccessMessage;
   private static String languageSetMessage;
   private static String skillPlayerNotFoundMessage;
   private static String skillCheckHeaderMessage;
   private static String skillCheckEntryFormat;
   private static String skillInvalidTypeMessage;
   private static String skillValidTypesMessage;
   private static String skillLevelMinMessage;
   private static String skillLevelWarningMessage;
   private static String skillSetSuccessMessage;
   private static String skillSetAllSuccessMessage;
   private static String skillTestOnlyPlayerMessage;
   private static String skillTestAlreadyRunningMessage;
   private static String skillTestStartMessage;
   private static String skillTestInfoMessage1;
   private static String skillTestInfoMessage2;
   private static String skillTestCancelNoTestMessage;
   private static String skillTestCancelSuccessMessage;
   private static String debugEnabledMessage;
   private static String debugDisabledMessage;
   private static String discordMessage;
   private static String discordMessageSentMessage;
   private static String eliteScrollConfigMessage;
   private static String scrollsNotEnabledMessage;
   private static String scrollInvalidNumberMessage;
   private static String scrollLevelZeroMessage;
   private static String scrollAmountZeroMessage;
   private static String scrollGaveMessage;
   private static String helpHeaderMessage;
   private static String wormholeInvalidOptionMessage;
   private static String wormholeSetFailedMessage;
   private static String protectionBypassMessage;
   private static String setupDoneDisableMessage;
   private static String setupDoneEnableMessage;
   private static String setupNotValidPackageMessage;
   private static String setupInstalledMessage;
   private static String setupUninstalledMessage;
   private static String shopPlayerNotFoundMessage;
   private static String notQueuedForInstanceMessage;
   private static String reloadStartMessage;
   private static String reloadSuccessMessage;
   private static String getTierGaveGearMessage;
   private static String getTierIronSwordMessage;
   private static String getTierCheatSwordMessage;
   private static String transitiveBlockStartMessage;
   private static String transitiveBlockInvalidBossMessage;
   private static String transitiveBlockNotRegionalMessage;
   private static String transitiveBlockIgnoreWarningMessage;
   private static String transitiveBlockInvalidTypeMessage;
   private static String transitiveBlockCorner1Message;
   private static String transitiveBlockCorner2Message;
   private static String transitiveBlockMissingCornersMessage;
   private static String transitiveBlockWorldMismatchMessage;
   private static String transitiveBlockSelectionCountMessage;
   private static String transitiveBlockRegisteredMessage;
   private static String transitiveBlockRegisteredAirMessage;
   private static String transitiveBlockUnregisteredMessage;
   private static String transitiveBlockTooManyMessage;
   private static String transitiveBlockRequiresRegionalMessage;
   private static String transitiveBlockCancellingMessage;
   private static String transitiveBlockMultipleBossesMessage;
   private static String transitiveBlockMultipleBossesHintMessage;
   private static String transitiveBlockNowRegisteringMessage;
   private static String transitiveBlockRunSameCommandMessage;
   private static String transitiveBlockCancelCommandMessage;
   private static String transitiveBlockCancelledMessage;
   private static String transitiveBlockNowSavingMessage;
   private static String transitiveBlockRegisteredCornerMessage;
   private static String transitiveBlockLocationsRegisteredMessage;
   private static String transitiveBlockLocationsFailedMessage;
   private static String lootStatsHeader;
   private static String lootStatsAttackSpeed;
   private static String lootStatsDamage;
   private static String lootStatsEdps;
   private static String lootStatsLevel;
   private static String lootStatsBonusEdps;
   private static String simLootPlayerNotFound;
   private static String simLootFinishedMultiple;
   private static String simLootFinishedSingle;
   private static String simLootCouldNotSend;
   private static String trackedBossCountMessage;
   private static String trackedNpcCountMessage;
   private static String statsSeparator;
   private static String statsVersionHeader;
   private static String statsEntityCountMessage;
   private static String statsBreakdownPrefix;
   private static String statsHighestThreatMessage;
   private static String statsAverageThreatMessage;
   private static String npcFeatureComingSoonMessage;
   private static String npcCannotRenameMessage;
   private static String npcFeatureReplacedMessage;
   private static String npcRemoveInstructions;
   private static String lootVoteSeparator;
   private static String lootVoteMessage;
   private static String lootVoteMessageSuffix;
   private static String versionCheckConnectionWarning;
   private static String versionOutdatedMessage;
   private static String resourcePackUpdatedMessage;
   private static String contentUpdatesAvailable;
   private static String contentUpdateEntry;
   private static String dungeonsOutdatedMessage;
   private static String versionUseSetupMessage;
   private static String versionClickDownloadHover;
   private static String versionNoDownloadLink;
   private static String versionDownloadAtMessage;
   private static String versionUpdateEasyMessage;
   private static String versionClickHereLabel;
   private static String versionInstallInfoMessage;
   private static String versionHereLabel;
   private static String versionSupportRoomMessage;
   private static String versionAutoUpdateMessage;
   private static String versionAutoUpdateHover;
   private static String versionOutdatedEntryPrefix;
   private static String versionNightbreakHover;
   private static String versionWikiHover;
   private static String versionDiscordHover;
   private static String wormholeOpDownloadMessage;
   private static String wormholeOpSetupMessage;
   private static String wormholeAgSpawnRecommendation;
   private static String farmingWarningMessage;
   private static String farmingResetMessage;
   private static String farmingCapMessage;
   private static String lootNeedMessage;
   private static String lootGreedMessage;
   private static String lootWinnerMessage;
   private static String questInvalidIdMessage;
   private static String questBypassOnMessage;
   private static String questBypassOffMessage;
   private static String invalidTreasureChestFilename;
   private static String relativePositionMessage;
   private static String cheatSwordDisplayName;
   private static String downloadAllNoTokenMessage;
   private static String downloadAllNothingMessage;
   private static String downloadAllNoAccessMessage;
   private static String downloadAllFoundMessage;
   private static String downloadAllProgressMessage;
   private static String downloadAllDownloadedMoreMessage;
   private static String downloadAllDownloadedMessage;
   private static String downloadAllFailedMessage;
   private static String downloadAllCompleteMessage;
   private static String downloadAllReloadingMessage;
   private static String downloadAllAlreadyRunningMessage;
   private static String downloadAllFailedListMessage;
   private static String downloadAllSetupClickMessage;
   private static String downloadAllSetupClickHover;
   private static String updateNoTokenMessage;
   private static String updateAllUpToDateMessage;
   private static String updateFoundOutdatedMessage;
   private static String updateCompleteMessage;
   private static String updateReloadingMessage;
   private static String updateProgressMessage;
   private static String updateDownloadedMoreMessage;
   private static String updateDownloadedMessage;
   private static String updateFailedMessage;

   public CommandMessagesConfig() {
      super("command_messages.yml");
   }

   @Override
   public void initializeValues() {
      NightbreakChatStyle.migrateSeparators(this.fileConfiguration, "statsSeparator", "lootVoteSeparator");
      payNiceTryMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when a player tries to pay a negative amount"),
         this.file,
         this.fileConfiguration,
         "payNiceTryMessage",
         "&4[EliteMobs]Nice try.",
         true
      );
      payPlayerNotOnlineMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when a player tries to pay an offline player", "$player - the target player name"),
         this.file,
         this.fileConfiguration,
         "payPlayerNotOnlineMessage",
         "&8[EliteMobs] &4Player $player is not online and can therefore not get a payment.",
         true
      );
      currencyDataNotReadyMessage = ConfigurationEngine.setString(
         List.of("Shown when an online player's economy data is still loading", "$player - the target player name"),
         this.file,
         this.fileConfiguration,
         "currencyDataNotReadyMessage",
         "&8[EliteMobs] &c$player's economy data is still loading. Try again shortly.",
         true
      );
      currencyPlayerNotValidMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when a player name is not valid for currency commands", "$player - the target player name"),
         this.file,
         this.fileConfiguration,
         "currencyPlayerNotValidMessage",
         "&8[EliteMobs] &4Player $player is not valid!",
         true
      );
      currencyAddedMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when currency is added to a player", "$amount - the amount added", "$player - the target player name"),
         this.file,
         this.fileConfiguration,
         "currencyAddedMessage",
         "&8[EliteMobs] &2You have added $amount to $player",
         true
      );
      currencyNowHasMessage = ConfigurationEngine.setString(
         List.of("Sets the message showing a player's new balance after currency change", "$amount - the new balance"),
         this.file,
         this.fileConfiguration,
         "currencyNowHasMessage",
         "&8[EliteMobs] &2They now have $amount",
         true
      );
      currencyAddedAllMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when currency is added to all online players", "$amount - the amount added"),
         this.file,
         this.fileConfiguration,
         "currencyAddedAllMessage",
         "&8[EliteMobs] &2You have added $amount to all online players.",
         true
      );
      currencySubtractedMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when currency is subtracted from a player", "$amount - the amount subtracted", "$player - the target player name"),
         this.file,
         this.fileConfiguration,
         "currencySubtractedMessage",
         "&8[EliteMobs] &2You have subtracted $amount from $player",
         true
      );
      currencySetMessage = ConfigurationEngine.setString(
         List.of(
            "Sets the message sent when a player's currency is set",
            "$player - the target player name",
            "$currencyName - the currency name",
            "$amount - the amount set"
         ),
         this.file,
         this.fileConfiguration,
         "currencySetMessage",
         "You set $player's $currencyName to $amount",
         true
      );
      currencyCheckMessage = ConfigurationEngine.setString(
         List.of(
            "Sets the message sent when checking a player's currency",
            "$player - the target player name",
            "$amount - the balance",
            "$currencyName - the currency name"
         ),
         this.file,
         this.fileConfiguration,
         "currencyCheckMessage",
         "&8[EliteMobs]&f $player &2has $amount $currencyName",
         true
      );
      dungeonNotValidMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when a dungeon name is not valid"),
         this.file,
         this.fileConfiguration,
         "dungeonNotValidMessage",
         "[EliteMobs] That dungeon isn't valid!",
         true
      );
      dungeonNotInstalledMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when a dungeon is not installed"),
         this.file,
         this.fileConfiguration,
         "dungeonNotInstalledMessage",
         "[EliteMobs] That dungeon isn't installed, ask an admin to install it!",
         true
      );
      alreadyInInstanceMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when a player is already in an instance"),
         this.file,
         this.fileConfiguration,
         "alreadyInInstanceMessage",
         "[EliteMobs] You're already in an instance! You will not be able to switch to another instance before you do /em quit",
         true
      );
      dungeonTeleportNotSetMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when a dungeon teleport location is not set"),
         this.file,
         this.fileConfiguration,
         "dungeonTeleportNotSetMessage",
         "[EliteMobs] Can't teleport you to the dungeon because the teleport location isn't set! If you are an admin, check the DungeonPackager config for this content and manually set the teleport location.",
         true
      );
      dungeonLockoutPlayerNotValidMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when a player name is not valid for the dungeon lockout reset command"),
         this.file,
         this.fileConfiguration,
         "dungeonLockoutPlayerNotValidMessage",
         "[EliteMobs] Error - player name not valid! The player must be online.",
         true
      );
      dungeonLockoutResetSuccessMessage = ConfigurationEngine.setString(
         List.of(
            "Sets the message sent when a player's dungeon boss lockouts are reset",
            "$player - the target player name",
            "$amount - the number of lockouts cleared"
         ),
         this.file,
         this.fileConfiguration,
         "dungeonLockoutResetSuccessMessage",
         "[EliteMobs] Cleared $amount dungeon boss lockout(s) for player $player",
         true
      );
      unbindForceSuccessMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when /em unbind force unbinds the held item"),
         this.file,
         this.fileConfiguration,
         "unbindForceSuccessMessage",
         "Unbound the held item.",
         true
      );
      unbindForceNotSoulboundMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when /em unbind force finds the held item is not soulbound"),
         this.file,
         this.fileConfiguration,
         "unbindForceNotSoulboundMessage",
         "The held item is not soulbound.",
         true
      );
      unbindForceAllMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when /em unbind force all sweeps the inventory", "$amount - the number of items unbound"),
         this.file,
         this.fileConfiguration,
         "unbindForceAllMessage",
         "Unbound $amount soulbound item(s) in your inventory.",
         true
      );
      questPlayerNotValidMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when a player name is not valid for quest commands"),
         this.file,
         this.fileConfiguration,
         "questPlayerNotValidMessage",
         "[EliteMobs] Error - player name not valid!",
         true
      );
      questResetSuccessMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when quests are successfully reset", "$player - the target player name"),
         this.file,
         this.fileConfiguration,
         "questResetSuccessMessage",
         "[EliteMobs] Successfully reset quests for player $player",
         true
      );
      questNameNotValidMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when a quest filename is not valid", "$quest - the quest filename"),
         this.file,
         this.fileConfiguration,
         "questNameNotValidMessage",
         "[EliteMobs] Error - quest filename $quest is not valid!",
         true
      );
      questCompletedSuccessMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when a quest is successfully completed for a player", "$quest - the quest name", "$player - the target player name"),
         this.file,
         this.fileConfiguration,
         "questCompletedSuccessMessage",
         "[EliteMobs] Successfully quest $quest for player $player",
         true
      );
      noActiveQuestsMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when a player has no active quests"),
         this.file,
         this.fileConfiguration,
         "noActiveQuestsMessage",
         "You have no active quests.",
         true
      );
      questNotFoundMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when a quest is not found"), this.file, this.fileConfiguration, "questNotFoundMessage", "Quest not found.", true
      );
      noSafeSpawnMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when no safe spawn location is found"),
         this.file,
         this.fileConfiguration,
         "noSafeSpawnMessage",
         "[EliteMobs] No safe spawn location found! Make sure the area is passable!",
         true
      );
      bossNotValidRegionalMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when a custom boss is not a valid regional boss", "$boss - the boss filename"),
         this.file,
         this.fileConfiguration,
         "bossNotValidRegionalMessage",
         "&8[EliteMobs] &4Failed to add spawn location! Custom Boss $boss is not valid regional boss!",
         true
      );
      leashRadiusFailedMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when setting a leash radius fails"),
         this.file,
         this.fileConfiguration,
         "leashRadiusFailedMessage",
         "&8[EliteMobs] &4Failed set the leash radius! Was the boss a valid regional boss?",
         true
      );
      invalidEntityTypeMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when an entity type cannot be an Elite", "$type - the entity type"),
         this.file,
         this.fileConfiguration,
         "invalidEntityTypeMessage",
         "&8[EliteMobs] &4Entity type $type can't be an Elite!",
         true
      );
      filenameNotValidMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when a boss filename is not valid", "$filename - the filename"),
         this.file,
         this.fileConfiguration,
         "filenameNotValidMessage",
         "Filename $filename is not valid! Make sure you are writing the name of a configuration file in the custombosses folder!",
         true
      );
      worldNotValidMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when a world argument is not valid"),
         this.file,
         this.fileConfiguration,
         "worldNotValidMessage",
         "[EliteMobs] World argument was not valid!",
         true
      );
      invalidPowerMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when a power name is not valid", "$power - the power name"),
         this.file,
         this.fileConfiguration,
         "invalidPowerMessage",
         "[EliteMobs] Power $power is not a valid power! Valid powers:",
         true
      );
      killedElitesMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when elites are killed", "$count - the number killed"),
         this.file,
         this.fileConfiguration,
         "killedElitesMessage",
         "&8[EliteMobs] &4Killed $count Elite Mobs.",
         true
      );
      killedEliteTypeMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when elites of a specific type are killed", "$count - the number killed", "$type - the entity type"),
         this.file,
         this.fileConfiguration,
         "killedEliteTypeMessage",
         "&8[EliteMobs] &4Killed $count Elite $type.",
         true
      );
      invalidEliteTypeMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when an entity type is not valid for EliteMobs"),
         this.file,
         this.fileConfiguration,
         "invalidEliteTypeMessage",
         "&8[EliteMobs] &cNot a valid entity type for EliteMobs!",
         true
      );
      removeElitesOffMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when a player stops removing elites"),
         this.file,
         this.fileConfiguration,
         "removeElitesOffMessage",
         "&8[EliteMobs] &aYou are no longer removing elites!",
         true
      );
      removeElitesOnMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when a player starts removing elites"),
         this.file,
         this.fileConfiguration,
         "removeElitesOnMessage",
         "&8[EliteMobs] &cYou are now removing elites when you punch them! Run &a/em remove &cagain to stop removing elites!",
         true
      );
      removedNotEliteMobsMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when a removed entity was not an EliteMobs entity"),
         this.file,
         this.fileConfiguration,
         "removedNotEliteMobsMessage",
         "&8[EliteMobs] The entity you just removed was not an EliteMobs entity. EliteMobs will still attempt to remove it though.",
         true
      );
      removedHijackedMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when a removed entity was likely hijacked by another plugin"),
         this.file,
         this.fileConfiguration,
         "removedHijackedMessage",
         "&8[EliteMobs] If the entity is supposed to be an EliteMobs entity, it is highly likely some other plugin hijacked the entity and changed it in a way that made EliteMobs unable to recognize it anymore.",
         true
      );
      removedTreasureChestMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when a treasure chest is removed"),
         this.file,
         this.fileConfiguration,
         "removedTreasureChestMessage",
         "[EliteMobs] Removed treasure chest!",
         true
      );
      removedSpawnLocationMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when a regional boss spawn location is removed", "$boss - the boss filename"),
         this.file,
         this.fileConfiguration,
         "removedSpawnLocationMessage",
         "&8[EliteMobs] &cRemoved a spawn location for boss $boss",
         true
      );
      invalidNpcFilenameMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when an NPC filename is invalid"),
         this.file,
         this.fileConfiguration,
         "invalidNpcFilenameMessage",
         "[EliteMobs] Invalid NPC filename.",
         true
      );
      gaveItemMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when an item is successfully given to a player", "$player - the target player name", "$item - the item name"),
         this.file,
         this.fileConfiguration,
         "gaveItemMessage",
         "[EliteMobs] Successfully gave $player item $item",
         true
      );
      invalidItemFilenameMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when an item filename is not valid", "$filename - the filename"),
         this.file,
         this.fileConfiguration,
         "invalidItemFilenameMessage",
         "&8[EliteMobs] &cFile name $filename is not a valid custom item file name!",
         true
      );
      invalidPlayerForItemMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when trying to give an item to an invalid player"),
         this.file,
         this.fileConfiguration,
         "invalidPlayerForItemMessage",
         "&8[EliteMobs] &cTried to give item to invalid player!",
         true
      );
      eventQueuedMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when an event is queued", "$event - the event name"),
         this.file,
         this.fileConfiguration,
         "eventQueuedMessage",
         "&8[EliteMobs] Queued event $event for the next valid time it can spawn. &cThis might take a while depending on the start conditions of the event.",
         true
      );
      invalidEventMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when an event name is not valid", "$event - the event name"),
         this.file,
         this.fileConfiguration,
         "invalidEventMessage",
         "&8[EliteMobs] &4Event $event is not valid! Make sure the event file exists, is enabled and has eventType set to TIMED.",
         true
      );
      eventCancelledMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when an event is cancelled by another plugin", "$event - the event name"),
         this.file,
         this.fileConfiguration,
         "eventCancelledMessage",
         "&8[EliteMobs] &4Event $event was cancelled by another plugin!",
         true
      );
      eventNoSpawnTypeMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when an event has no spawnType set", "$event - the event name"),
         this.file,
         this.fileConfiguration,
         "eventNoSpawnTypeMessage",
         "&8[EliteMobs] &4Event $event has no spawnType set! Add a valid custom spawn filename to the event config.",
         true
      );
      eventInvalidSpawnTypeMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when an event's spawnType is not valid", "$event - the event name", "$spawnType - the invalid spawn type"),
         this.file,
         this.fileConfiguration,
         "eventInvalidSpawnTypeMessage",
         "&8[EliteMobs] &4Event $event failed to start because its spawnType '$spawnType' is not a valid custom spawn file! Make sure the file exists in the customspawns folder and is enabled.",
         true
      );
      eventNoValidBossesMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when none of an event's boss filenames are valid", "$event - the event name"),
         this.file,
         this.fileConfiguration,
         "eventNoValidBossesMessage",
         "&8[EliteMobs] &4Event $event failed to start because none of its boss filenames are valid! Check that the bossFilenames in the event config point to existing custom boss files.",
         true
      );
      invalidMinidungeonMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when a minidungeon name is not valid", "$name - the minidungeon name"),
         this.file,
         this.fileConfiguration,
         "invalidMinidungeonMessage",
         "&8[EliteMobs] &4Minidungeons name $name isn't valid!",
         true
      );
      minidungeonNotInstalledMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when a minidungeon is not installed"),
         this.file,
         this.fileConfiguration,
         "minidungeonNotInstalledMessage",
         "&8[EliteMobs] &4Minidungeon isn't installed! Can't get the relative location for uninstalled Minidungeons!",
         true
      );
      invalidAnchorPointMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when an anchor point is not valid"),
         this.file,
         this.fileConfiguration,
         "invalidAnchorPointMessage",
         "&8[EliteMobs] &4Something went wrong and made the anchor point not valid!",
         true
      );
      packageDoneMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when packaging is done"),
         this.file,
         this.fileConfiguration,
         "packageDoneMessage",
         "&8[EliteMobs] &2Done! You can find your package in &9~/plugins/EliteMobs/exports &2. &6If you are making a dungeon, make sure to create your own dungeonpackages file!",
         true
      );
      packageDontForgetMessage = ConfigurationEngine.setString(
         List.of("Sets the reminder message about adding world and schematic files"),
         this.file,
         this.fileConfiguration,
         "packageDontForgetMessage",
         "&8[EliteMobs] &6Don't forget to add your world and schematic files to the package if needed!",
         true
      );
      packageZipFailedMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when zipping a package fails"),
         this.file,
         this.fileConfiguration,
         "packageZipFailedMessage",
         "&4[EliteMobs] Failed to zip package!",
         true
      );
      packageZippedMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when a package is zipped successfully"),
         this.file,
         this.fileConfiguration,
         "packageZippedMessage",
         "&8[EliteMobs] &6Zipped files for your convenience. Don't forget any additional files like the dungeonpackager or world/schematics files before distributing!",
         true
      );
      packageNeedsDungeonNameMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when a dungeon name is needed"),
         this.file,
         this.fileConfiguration,
         "packageNeedsDungeonNameMessage",
         "[EliteMobs] This commands needs a valid dungeon name!",
         true
      );
      packageNeedsNumberMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when a valid number is needed"),
         this.file,
         this.fileConfiguration,
         "packageNeedsNumberMessage",
         "This command needs a valid natural number! (1, 2, 3, 4...)",
         true
      );
      packageNoSubdirectoryMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when a subdirectory is not found", "$subdirectory - the subdirectory name"),
         this.file,
         this.fileConfiguration,
         "packageNoSubdirectoryMessage",
         "[EliteMobs] Could not find any $subdirectory for this dungeon. This might be normal depending on your setup.",
         true
      );
      packageFailedDirectoryMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when a directory fails to be created", "$path - the directory path"),
         this.file,
         this.fileConfiguration,
         "packageFailedDirectoryMessage",
         "[EliteMobs] Failed to create directory $path",
         true
      );
      missingPermissionMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when a player is missing a permission"),
         this.file,
         this.fileConfiguration,
         "missingPermissionMessage",
         "Missing permission: elitemobs.adventurersguild.command / elitemobs.adventurersguild.teleport",
         true
      );
      regionalBossStatsMessage = ConfigurationEngine.setString(
         List.of("Sets the message showing regional boss stats", "$total - total regional bosses", "$loaded - currently loaded bosses"),
         this.file,
         this.fileConfiguration,
         "regionalBossStatsMessage",
         "&7[EM] &2There are &c$total &2Regional Bosses installed, of which &c$loaded &2are currently loaded.",
         true
      );
      languageNotFoundMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when a language is not found"),
         this.file,
         this.fileConfiguration,
         "languageNotFoundMessage",
         "&cLanguage not found. Valid options:",
         true
      );
      languageListPrefix = ConfigurationEngine.setString(
         List.of("Sets the prefix for each language listed"), this.file, this.fileConfiguration, "languageListPrefix", "  - ", true
      );
      languageSetEnglishMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when language is set to English"),
         this.file,
         this.fileConfiguration,
         "languageSetEnglishMessage",
         "&2Language set to English! Using plugin defaults.",
         true
      );
      languageGeneratingCustomMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when generating custom.csv template"),
         this.file,
         this.fileConfiguration,
         "languageGeneratingCustomMessage",
         "&eGenerating custom.csv template...",
         true
      );
      languageGenerateFailedMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when custom.csv generation fails"),
         this.file,
         this.fileConfiguration,
         "languageGenerateFailedMessage",
         "&cFailed to generate custom.csv. Language not changed.",
         true
      );
      languageGenerateSuccessMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when custom.csv is generated successfully"),
         this.file,
         this.fileConfiguration,
         "languageGenerateSuccessMessage",
         "&2Generated custom.csv! Edit it to customize text or add translations.",
         true
      );
      languageSetCustomMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when language is set to custom"),
         this.file,
         this.fileConfiguration,
         "languageSetCustomMessage",
         "&2Language set to custom! &eEdit plugins/EliteMobs/translations/custom.csv to customize text. New keys will be added automatically as the plugin runs.",
         true
      );
      languageDownloadingMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when downloading a language file", "$language - the language name"),
         this.file,
         this.fileConfiguration,
         "languageDownloadingMessage",
         "&eDownloading $language.csv...",
         true
      );
      languageDownloadFailedMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when a language download fails", "$language - the language name"),
         this.file,
         this.fileConfiguration,
         "languageDownloadFailedMessage",
         "&cFailed to download $language.csv. Language not changed.",
         true
      );
      languageDownloadSuccessMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when a language file is downloaded successfully", "$language - the language name"),
         this.file,
         this.fileConfiguration,
         "languageDownloadSuccessMessage",
         "&2Downloaded $language.csv successfully.",
         true
      );
      languageSetMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when language is set to a community language", "$language - the language name"),
         this.file,
         this.fileConfiguration,
         "languageSetMessage",
         "&2Language set to $language! &eTranslations are community-managed. Use at your own discretion.",
         true
      );
      skillPlayerNotFoundMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when a player is not found for skill commands", "$player - the player name"),
         this.file,
         this.fileConfiguration,
         "skillPlayerNotFoundMessage",
         "&cPlayer not found: $player",
         true
      );
      skillCheckHeaderMessage = ConfigurationEngine.setString(
         List.of("Sets the header message for skill check", "$player - the player name"),
         this.file,
         this.fileConfiguration,
         "skillCheckHeaderMessage",
         "&6=== $player's Skills ===",
         true
      );
      skillCheckEntryFormat = ConfigurationEngine.setString(
         List.of(
            "Sets the format for each skill entry in skill check",
            "$skill - the skill name",
            "$level - the skill level",
            "$xpProgress - current XP progress",
            "$xpNeeded - XP needed for next level",
            "$progress - percentage progress"
         ),
         this.file,
         this.fileConfiguration,
         "skillCheckEntryFormat",
         "&7$skill: &aLevel $level &7($xpProgress/$xpNeeded XP, $progress%)",
         true
      );
      skillInvalidTypeMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when an invalid skill type is specified", "$type - the invalid type"),
         this.file,
         this.fileConfiguration,
         "skillInvalidTypeMessage",
         "&cInvalid skill type: $type",
         true
      );
      skillValidTypesMessage = ConfigurationEngine.setString(
         List.of("Sets the message listing valid skill types", "$types - the valid types"),
         this.file,
         this.fileConfiguration,
         "skillValidTypesMessage",
         "&7Valid types: $types",
         true
      );
      skillLevelMinMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when a skill level is below minimum"),
         this.file,
         this.fileConfiguration,
         "skillLevelMinMessage",
         "&cLevel must be at least 1!",
         true
      );
      skillLevelWarningMessage = ConfigurationEngine.setString(
         List.of("Sets the warning message for levels above soft cap", "$level - the level"),
         this.file,
         this.fileConfiguration,
         "skillLevelWarningMessage",
         "&eWarning: Level $level is above the soft cap of 100.",
         true
      );
      skillSetSuccessMessage = ConfigurationEngine.setString(
         List.of(
            "Sets the message sent when a skill is set successfully",
            "$player - the player name",
            "$skill - the skill name",
            "$level - the level",
            "$xp - the XP amount"
         ),
         this.file,
         this.fileConfiguration,
         "skillSetSuccessMessage",
         "&aSet $player's $skill skill to level $level ($xp XP)",
         true
      );
      skillSetAllSuccessMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when all skills are set successfully", "$player - the player name", "$level - the level", "$xp - the XP amount"),
         this.file,
         this.fileConfiguration,
         "skillSetAllSuccessMessage",
         "&aSet all of $player's skills to level $level ($xp XP)",
         true
      );
      skillTestOnlyPlayerMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when a non-player tries to run a skill test"),
         this.file,
         this.fileConfiguration,
         "skillTestOnlyPlayerMessage",
         "&cThis command can only be run by a player!",
         true
      );
      skillTestAlreadyRunningMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when a skill test is already running"),
         this.file,
         this.fileConfiguration,
         "skillTestAlreadyRunningMessage",
         "&cA test is already running! Use &e/em skill test cancel &cto stop it.",
         true
      );
      skillTestStartMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when a skill test starts"),
         this.file,
         this.fileConfiguration,
         "skillTestStartMessage",
         "&a[Test] Starting skill system test...",
         true
      );
      skillTestInfoMessage1 = ConfigurationEngine.setString(
         List.of("Sets the first info message for skill test"),
         this.file,
         this.fileConfiguration,
         "skillTestInfoMessage1",
         "&7This will temporarily modify your skill levels and selections.",
         true
      );
      skillTestInfoMessage2 = ConfigurationEngine.setString(
         List.of("Sets the second info message for skill test"),
         this.file,
         this.fileConfiguration,
         "skillTestInfoMessage2",
         "&7Your original state will be restored after the test.",
         true
      );
      skillTestCancelNoTestMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when there is no active test to cancel"),
         this.file,
         this.fileConfiguration,
         "skillTestCancelNoTestMessage",
         "&cNo active test to cancel.",
         true
      );
      skillTestCancelSuccessMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when a skill test is cancelled successfully"),
         this.file,
         this.fileConfiguration,
         "skillTestCancelSuccessMessage",
         "&aTest cancelled and player state restored.",
         true
      );
      debugEnabledMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when debug mode is enabled"),
         this.file,
         this.fileConfiguration,
         "debugEnabledMessage",
         "&aDebug mode &2enabled&a. Combat damage calculations will be logged to console.",
         true
      );
      debugDisabledMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when debug mode is disabled"),
         this.file,
         this.fileConfiguration,
         "debugDisabledMessage",
         "&cDebug mode &4disabled&c.",
         true
      );
      discordMessage = ConfigurationEngine.setString(
         List.of("Sets the message showing the Discord link"),
         this.file,
         this.fileConfiguration,
         "discordMessage",
         "&6Discord room for support & downloads: &9",
         true
      );
      discordMessageSentMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when a Discord message is sent"),
         this.file,
         this.fileConfiguration,
         "discordMessageSentMessage",
         "&aAttempted to send a message to Discord!",
         true
      );
      eliteScrollConfigMessage = ConfigurationEngine.setString(
         List.of("Sets the message shown when elite scrolls are not enabled (long config explanation)"),
         this.file,
         this.fileConfiguration,
         "eliteScrollConfigMessage",
         "Elite Scrolls are not currently enabled on this server! They should only be used if the server uses an item system other than EliteMobs' built in item system. To enable elite scrolls, an admin has to set them to true in the ~/plugins/EliteMobs/ItemSettings.yml and set useEliteItemScrolls to true.",
         true
      );
      scrollsNotEnabledMessage = ConfigurationEngine.setString(
         List.of("Sets the short message when elite scrolls are not enabled"),
         this.file,
         this.fileConfiguration,
         "scrollsNotEnabledMessage",
         "Elite Scrolls are not currently enabled on this server! Ask the admin to enable them in the EliteMobs DefaultConfig.yml",
         true
      );
      scrollInvalidNumberMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when scroll level/amount is not a valid integer"),
         this.file,
         this.fileConfiguration,
         "scrollInvalidNumberMessage",
         "Level and amount must be valid integers.",
         true
      );
      scrollLevelZeroMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when scroll level is zero or negative"),
         this.file,
         this.fileConfiguration,
         "scrollLevelZeroMessage",
         "Scroll level must be greater than zero.",
         true
      );
      scrollAmountZeroMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when scroll amount is zero or negative"),
         this.file,
         this.fileConfiguration,
         "scrollAmountZeroMessage",
         "Amount must be greater than zero.",
         true
      );
      scrollGaveMessage = ConfigurationEngine.setString(
         List.of(
            "Sets the message sent when scrolls are given to a player",
            "$amount - the number of scrolls",
            "$level - the scroll level",
            "$player - the player name"
         ),
         this.file,
         this.fileConfiguration,
         "scrollGaveMessage",
         "Gave $amount level $level Elite Scroll(s) to $player.",
         true
      );
      helpHeaderMessage = ConfigurationEngine.setString(
         List.of("Sets the header message for the help command"), this.file, this.fileConfiguration, "helpHeaderMessage", "Commands:", true
      );
      wormholeInvalidOptionMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when an invalid wormhole option is specified"),
         this.file,
         this.fileConfiguration,
         "wormholeInvalidOptionMessage",
         "Not a valid wormhole option! Pick 1 or 2 to set either end of the wormhole.",
         true
      );
      wormholeSetFailedMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when setting a wormhole location fails"),
         this.file,
         this.fileConfiguration,
         "wormholeSetFailedMessage",
         "Failed to set location for this wormhole.",
         true
      );
      protectionBypassMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when toggling dungeon protection bypass", "$status - the bypass status"),
         this.file,
         this.fileConfiguration,
         "protectionBypassMessage",
         "Bypassing dungeon protections is now $status",
         true
      );
      setupDoneDisableMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when setup messages are disabled"),
         this.file,
         this.fileConfiguration,
         "setupDoneDisableMessage",
         "&aEliteMobs will no longer send messages on login. You can do [/em setup done] again to revert this.",
         true
      );
      setupDoneEnableMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when setup messages are re-enabled"),
         this.file,
         this.fileConfiguration,
         "setupDoneEnableMessage",
         "&aEliteMobs will once again send messages on login. You can do [/em setup done] again to revert this.",
         true
      );
      setupNotValidPackageMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when a setup package is not valid"),
         this.file,
         this.fileConfiguration,
         "setupNotValidPackageMessage",
         "Not a valid em package!",
         true
      );
      setupInstalledMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when content is installed successfully"),
         this.file,
         this.fileConfiguration,
         "setupInstalledMessage",
         "Successfully installed content!",
         true
      );
      setupUninstalledMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when content is uninstalled successfully"),
         this.file,
         this.fileConfiguration,
         "setupUninstalledMessage",
         "Successfully uninstalled content!",
         true
      );
      shopPlayerNotFoundMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when a shop player is not found"),
         this.file,
         this.fileConfiguration,
         "shopPlayerNotFoundMessage",
         "Failed to get player with that username!",
         true
      );
      notQueuedForInstanceMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when a player is not queued for instanced content"),
         this.file,
         this.fileConfiguration,
         "notQueuedForInstanceMessage",
         "You are not queued for instanced content!",
         true
      );
      reloadStartMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when the plugin starts reloading"),
         this.file,
         this.fileConfiguration,
         "reloadStartMessage",
         "Plugin reloading...",
         true
      );
      reloadSuccessMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when the plugin is reloaded"), this.file, this.fileConfiguration, "reloadSuccessMessage", "Plugin reloaded!", true
      );
      getTierGaveGearMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when test gear is given", "$level - the tier level"),
         this.file,
         this.fileConfiguration,
         "getTierGaveGearMessage",
         "&aGave level $level test gear and set all skills to level $level.",
         true
      );
      getTierIronSwordMessage = ConfigurationEngine.setString(
         List.of("Sets the message about the iron sword for balanced testing"),
         this.file,
         this.fileConfiguration,
         "getTierIronSwordMessage",
         "&7Use the &fIron Sword &7for balanced damage testing.",
         true
      );
      getTierCheatSwordMessage = ConfigurationEngine.setString(
         List.of("Sets the message about the cheat sword"),
         this.file,
         this.fileConfiguration,
         "getTierCheatSwordMessage",
         "&7The &cCHEAT SWORD &7has Sharpness 100 - use it only to skip fights!",
         true
      );
      transitiveBlockStartMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when starting large selection for transitive blocks"),
         this.file,
         this.fileConfiguration,
         "transitiveBlockStartMessage",
         "[EliteMobs] Now registering large selection for transitive blocks! Left click to set corner 1, right click to set corner 2! Run the same command again to stop registering blocks and run /em cancelblocks to cancel!",
         true
      );
      transitiveBlockInvalidBossMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when the boss file is not valid"),
         this.file,
         this.fileConfiguration,
         "transitiveBlockInvalidBossMessage",
         "Boss file isn't valid! Try again with a valid filename for your custom boss.",
         true
      );
      transitiveBlockNotRegionalMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when the boss file is not for a regional boss"),
         this.file,
         this.fileConfiguration,
         "transitiveBlockNotRegionalMessage",
         "&cBoss file isn't for a regional boss! This feature only works for regional bosses.",
         true
      );
      transitiveBlockIgnoreWarningMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent to ignore the regional boss warning for phase bosses"),
         this.file,
         this.fileConfiguration,
         "transitiveBlockIgnoreWarningMessage",
         "&aIgnore this warning if you are setting blocks for a phase boss whose first phase is a regional boss!",
         true
      );
      transitiveBlockInvalidTypeMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when the transitive block type is not valid"),
         this.file,
         this.fileConfiguration,
         "transitiveBlockInvalidTypeMessage",
         "Not a valid transitive block type, use ON_SPAWN or ON_REMOVE !",
         true
      );
      transitiveBlockCorner1Message = ConfigurationEngine.setString(
         List.of("Sets the message sent when corner 1 is set"), this.file, this.fileConfiguration, "transitiveBlockCorner1Message", "Set corner 1!", true
      );
      transitiveBlockCorner2Message = ConfigurationEngine.setString(
         List.of("Sets the message sent when corner 2 is set"), this.file, this.fileConfiguration, "transitiveBlockCorner2Message", "Set corner 2!", true
      );
      transitiveBlockMissingCornersMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when a block area is missing a corner"),
         this.file,
         this.fileConfiguration,
         "transitiveBlockMissingCornersMessage",
         "Select both corners before saving. Your selection is still active.",
         true
      );
      transitiveBlockWorldMismatchMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when selected blocks are outside the regional boss world"),
         this.file,
         this.fileConfiguration,
         "transitiveBlockWorldMismatchMessage",
         "Select blocks in the regional boss's world. Your selection is still active.",
         true
      );
      transitiveBlockSelectionCountMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent showing block selection count", "$count - the number of blocks selected", "$limit - the recommended block limit"),
         this.file,
         this.fileConfiguration,
         "transitiveBlockSelectionCountMessage",
         "[EliteMobs] Current selection has $count blocks selected. For performance reasons, it is recommended you don't go over $limit blocks!",
         true
      );
      transitiveBlockRegisteredMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when a block is registered", "$type - the block type"),
         this.file,
         this.fileConfiguration,
         "transitiveBlockRegisteredMessage",
         "Registered $type block!",
         true
      );
      transitiveBlockRegisteredAirMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when an air block is registered"),
         this.file,
         this.fileConfiguration,
         "transitiveBlockRegisteredAirMessage",
         "Registered air block!",
         true
      );
      transitiveBlockUnregisteredMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when a block is unregistered"),
         this.file,
         this.fileConfiguration,
         "transitiveBlockUnregisteredMessage",
         "Unregistered block!",
         true
      );
      transitiveBlockTooManyMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when too many blocks are registered at once", "$limit - the recommended block limit"),
         this.file,
         this.fileConfiguration,
         "transitiveBlockTooManyMessage",
         "[EliteMobs] You registered more than $limit blocks at once. For performance reasons, it is recommended you keep the selections low. Avoid things like selecting a lot of unnecessary air blocks!",
         true
      );
      transitiveBlockRequiresRegionalMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when transitive blocks require a regional boss to be placed"),
         this.file,
         this.fileConfiguration,
         "transitiveBlockRequiresRegionalMessage",
         "&c[EliteMobs] Transitive blocks require one Regional Boss from the configuration files to be placed in order to add transitive blocks relative to the spawn coordinates of that boss.",
         true
      );
      transitiveBlockCancellingMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when block registration is cancelled"),
         this.file,
         this.fileConfiguration,
         "transitiveBlockCancellingMessage",
         "&c[EliteMobs] Cancelling block registration!",
         true
      );
      transitiveBlockMultipleBossesMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when multiple regional bosses are found"),
         this.file,
         this.fileConfiguration,
         "transitiveBlockMultipleBossesMessage",
         "&c[EliteMobs] Transitive blocks require having ONLY one Regional Boss from the configuration files to be placed in order to add transitive blocks relative to the spawn coordinates of that boss. If there is more than one boss, the plugin can't determine which boss specifically should be getting the transitive blocks",
         true
      );
      transitiveBlockMultipleBossesHintMessage = ConfigurationEngine.setString(
         List.of("Sets the hint message about needing one config file per boss"),
         this.file,
         this.fileConfiguration,
         "transitiveBlockMultipleBossesHintMessage",
         "&c[EliteMobs] If want multiple bosses to have transitive blocks, you will need one configuration file per boss that has them!",
         true
      );
      transitiveBlockNowRegisteringMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when block registration starts", "$type - the transitive block type"),
         this.file,
         this.fileConfiguration,
         "transitiveBlockNowRegisteringMessage",
         "&a[EliteMobs] Now registering $type blocks! &2Punch blocks to register air at that location, or right click blocks to register the block you right clicked!",
         true
      );
      transitiveBlockRunSameCommandMessage = ConfigurationEngine.setString(
         List.of("Sets the message about running the same command to stop"),
         this.file,
         this.fileConfiguration,
         "transitiveBlockRunSameCommandMessage",
         "&6[EliteMobs] Run the same command to stop registering blocks and save!",
         true
      );
      transitiveBlockCancelCommandMessage = ConfigurationEngine.setString(
         List.of("Sets the message about running the cancel command"),
         this.file,
         this.fileConfiguration,
         "transitiveBlockCancelCommandMessage",
         "&6[EliteMobs] Or run the command /em cancelblocks to cancel the registration!",
         true
      );
      transitiveBlockCancelledMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when block registration is successfully cancelled"),
         this.file,
         this.fileConfiguration,
         "transitiveBlockCancelledMessage",
         "&c[EliteMobs] Block registration successfully cancelled!",
         true
      );
      transitiveBlockNowSavingMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when saving transitive blocks", "$type - the transitive block type"),
         this.file,
         this.fileConfiguration,
         "transitiveBlockNowSavingMessage",
         "&a[EliteMobs] Now saving $type blocks!",
         true
      );
      transitiveBlockRegisteredCornerMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when blocks are registered between corners", "$count - the number of blocks registered"),
         this.file,
         this.fileConfiguration,
         "transitiveBlockRegisteredCornerMessage",
         "Successfully registered $count blocks between your corners!",
         true
      );
      transitiveBlockLocationsRegisteredMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when locations are registered correctly"),
         this.file,
         this.fileConfiguration,
         "transitiveBlockLocationsRegisteredMessage",
         "Locations registered correctly!",
         true
      );
      transitiveBlockLocationsFailedMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when locations fail to commit"),
         this.file,
         this.fileConfiguration,
         "transitiveBlockLocationsFailedMessage",
         "Failed to commit locations!",
         true
      );
      lootStatsHeader = ConfigurationEngine.setString(
         List.of("Sets the header message for loot stats"), this.file, this.fileConfiguration, "lootStatsHeader", "[EliteMobs] Item Stats:", true
      );
      lootStatsAttackSpeed = ConfigurationEngine.setString(
         List.of("Sets the label for item attack speed"), this.file, this.fileConfiguration, "lootStatsAttackSpeed", "Item attack speed: ", true
      );
      lootStatsDamage = ConfigurationEngine.setString(
         List.of("Sets the label for item damage"), this.file, this.fileConfiguration, "lootStatsDamage", "Item damage: ", true
      );
      lootStatsEdps = ConfigurationEngine.setString(
         List.of("Sets the label for item EDPS"), this.file, this.fileConfiguration, "lootStatsEdps", "Item EDPS: ", true
      );
      lootStatsLevel = ConfigurationEngine.setString(
         List.of("Sets the label for item level"), this.file, this.fileConfiguration, "lootStatsLevel", "Item level: ", true
      );
      lootStatsBonusEdps = ConfigurationEngine.setString(
         List.of("Sets the label for item bonus EDPS"), this.file, this.fileConfiguration, "lootStatsBonusEdps", "Item bonus EDPS: ", true
      );
      simLootPlayerNotFound = ConfigurationEngine.setString(
         List.of("Sets the message sent when a player is not found for sim loot", "$player - the player name"),
         this.file,
         this.fileConfiguration,
         "simLootPlayerNotFound",
         "[EliteMobs] Failed to get a player named \"$player\"!",
         true
      );
      simLootFinishedMultiple = ConfigurationEngine.setString(
         List.of(
            "Sets the message sent when sim loot finishes running multiple times",
            "$player - the player name",
            "$times - the number of times run",
            "$level - the level"
         ),
         this.file,
         this.fileConfiguration,
         "simLootFinishedMultiple",
         "[EliteMobs] Finished running simulation command for player $player $times times at level $level .",
         true
      );
      simLootFinishedSingle = ConfigurationEngine.setString(
         List.of("Sets the message sent when sim loot finishes running once", "$player - the player name", "$level - the level"),
         this.file,
         this.fileConfiguration,
         "simLootFinishedSingle",
         "[EliteMobs] Finished running simulation command for player $player at level $level .",
         true
      );
      simLootCouldNotSend = ConfigurationEngine.setString(
         List.of("Sets the message sent when an item could not be sent to the player", "$player - the player name"),
         this.file,
         this.fileConfiguration,
         "simLootCouldNotSend",
         "&c[EliteMobs] Could not send item to player $player - player with this name was not found!",
         true
      );
      trackedBossCountMessage = ConfigurationEngine.setString(
         List.of("Sets the label for tracked boss count"), this.file, this.fileConfiguration, "trackedBossCountMessage", "Tracked boss count: ", true
      );
      trackedNpcCountMessage = ConfigurationEngine.setString(
         List.of("Sets the label for tracked NPC count"), this.file, this.fileConfiguration, "trackedNpcCountMessage", "Tracked NPC count: ", true
      );
      statsSeparator = ConfigurationEngine.setString(
         List.of("Sets the separator line for the stats command."), this.file, this.fileConfiguration, "statsSeparator", NightbreakChatStyle.separator(), true
      );
      statsVersionHeader = ConfigurationEngine.setString(
         List.of("Sets the version header for the stats command.", "$version is the placeholder for the plugin version."),
         this.file,
         this.fileConfiguration,
         "statsVersionHeader",
         "&7[EM] &a&lEliteMobs v. $version stats:",
         true
      );
      statsEntityCountMessage = ConfigurationEngine.setString(
         List.of(
            "Sets the entity count message for the stats command.",
            "$total is the placeholder for total elite entities.",
            "$aggressive is the placeholder for elite mobs count."
         ),
         this.file,
         this.fileConfiguration,
         "statsEntityCountMessage",
         "&7[EM] &2There are currently &l&6$total &f&2EliteMobs entities in the world.",
         true
      );
      statsBreakdownPrefix = ConfigurationEngine.setString(
         List.of("Sets the breakdown prefix for the stats command."), this.file, this.fileConfiguration, "statsBreakdownPrefix", "&2Breakdown: &a", true
      );
      statsHighestThreatMessage = ConfigurationEngine.setString(
         List.of(
            "Sets the highest threat message for the stats command.",
            "$player is the placeholder for the player name.",
            "$threat is the placeholder for the threat tier."
         ),
         this.file,
         this.fileConfiguration,
         "statsHighestThreatMessage",
         "&7[EM] &2Highest online threat tier: &a$player &2at total threat tier &a$threat",
         true
      );
      statsAverageThreatMessage = ConfigurationEngine.setString(
         List.of("Sets the average threat message for the stats command.", "$average is the placeholder for the average threat tier."),
         this.file,
         this.fileConfiguration,
         "statsAverageThreatMessage",
         "&7[EM] &2Average threat tier: &a$average",
         true
      );
      npcFeatureComingSoonMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when a feature is coming soon"),
         this.file,
         this.fileConfiguration,
         "npcFeatureComingSoonMessage",
         "[EliteMobs] This feature is coming soon!",
         true
      );
      npcCannotRenameMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when a player tries to rename an NPC"),
         this.file,
         this.fileConfiguration,
         "npcCannotRenameMessage",
         "[EliteMobs] You can't rename NPCs using name tags!",
         true
      );
      npcFeatureReplacedMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when a player interacts with a deprecated NPC (enhancer/refiner/smelter)"),
         this.file,
         this.fileConfiguration,
         "npcFeatureReplacedMessage",
         "&8[EliteMobs] &cThis feature has been replaced! This NPC should be removed by an admin as soon as possible.",
         true
      );
      npcRemoveInstructions = ConfigurationEngine.setString(
         List.of("Sets the instructions sent to admins on how to remove a deprecated NPC"),
         this.file,
         this.fileConfiguration,
         "npcRemoveInstructions",
         "&2To remove this NPC, use the command &6/em remove &2and hit the NPC!",
         true
      );
      lootVoteSeparator = ConfigurationEngine.setString(
         List.of("Sets the separator line for loot votes"), this.file, this.fileConfiguration, "lootVoteSeparator", NightbreakChatStyle.separator(), true
      );
      lootVoteMessage = ConfigurationEngine.setString(
         List.of("Sets the loot vote message prefix, before the clickable /em loot command.", "$count - the number of items to vote on, used in the suffix."),
         this.file,
         this.fileConfiguration,
         "lootVoteMessage",
         "&8[EliteMobs] &6Shared loot awaiting a roll. Click ",
         true
      );
      lootVoteMessageSuffix = ConfigurationEngine.setString(
         List.of("Sets the loot vote message suffix, after the clickable /em loot command.", "$count - the number of items to vote on"),
         this.file,
         this.fileConfiguration,
         "lootVoteMessageSuffix",
         " &6to vote on $count items!",
         true
      );
      versionCheckConnectionWarning = ConfigurationEngine.setString(
         List.of("Sets the warning message when connection to update servers fails"),
         this.file,
         this.fileConfiguration,
         "versionCheckConnectionWarning",
         "&8[EliteMobs] &eWarning: Could not connect to update servers. Version checking is currently unavailable. Check your internet connection or try again later.",
         true
      );
      versionOutdatedMessage = ConfigurationEngine.setString(
         List.of("Sets the message prefix when the plugin is outdated.", "Followed by a clickable nightbreak.io/elitemobs link."),
         this.file,
         this.fileConfiguration,
         "versionOutdatedMessage",
         "&cYour version of EliteMobs is outdated. &aYou can download the latest version from ",
         true
      );
      resourcePackUpdatedMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when the resource pack has been updated"),
         this.file,
         this.fileConfiguration,
         "resourcePackUpdatedMessage",
         "&8[EliteMobs] &cThe EliteMobs resource pack has updated! This means that the current resource pack will not fully work until you restart your server. You only need to restart once!",
         true
      );
      contentUpdatesAvailable = ConfigurationEngine.setString(
         List.of("Sets the message header for content updates available", "$count - the number of updates available"),
         this.file,
         this.fileConfiguration,
         "contentUpdatesAvailable",
         "&e$count content update(s) available:",
         true
      );
      contentUpdateEntry = ConfigurationEngine.setString(
         List.of("Sets the format for each content update entry", "$name - the name of the content"),
         this.file,
         this.fileConfiguration,
         "contentUpdateEntry",
         "&e- $name",
         true
      );
      dungeonsOutdatedMessage = ConfigurationEngine.setString(
         List.of("Sets the message header for outdated dungeons on player login"),
         this.file,
         this.fileConfiguration,
         "dungeonsOutdatedMessage",
         "&cThe following dungeons are outdated:",
         true
      );
      versionUseSetupMessage = ConfigurationEngine.setString(
         List.of("Message telling admins to use /em setup to view and update content.", "Followed by a clickable /em setup command and content link."),
         this.file,
         this.fileConfiguration,
         "versionUseSetupMessage",
         "&7Use ",
         true
      );
      versionClickDownloadHover = ConfigurationEngine.setString(
         List.of("Hover text on outdated content entries with a download link."),
         this.file,
         this.fileConfiguration,
         "versionClickDownloadHover",
         "&9Click to go to download link!",
         true
      );
      versionNoDownloadLink = ConfigurationEngine.setString(
         List.of("Suffix shown after an outdated content name when no download link is available."),
         this.file,
         this.fileConfiguration,
         "versionNoDownloadLink",
         " &7(no download link available)",
         true
      );
      versionDownloadAtMessage = ConfigurationEngine.setString(
         List.of("Message telling admins where to download updates."),
         this.file,
         this.fileConfiguration,
         "versionDownloadAtMessage",
         "&8[EliteMobs]&f You can download the update at ",
         true
      );
      versionUpdateEasyMessage = ConfigurationEngine.setString(
         List.of("Message telling admins that updating is easy."),
         this.file,
         this.fileConfiguration,
         "versionUpdateEasyMessage",
         "&2Updating is quick & easy! ",
         true
      );
      versionClickHereLabel = ConfigurationEngine.setString(
         List.of("Clickable 'Click here' label linking to the wiki."), this.file, this.fileConfiguration, "versionClickHereLabel", "&9&nClick here", true
      );
      versionInstallInfoMessage = ConfigurationEngine.setString(
         List.of("Message fragment about install info and support."),
         this.file,
         this.fileConfiguration,
         "versionInstallInfoMessage",
         " &2for info on how to install updates and ",
         true
      );
      versionHereLabel = ConfigurationEngine.setString(
         List.of("Clickable 'here' label linking to Discord support."), this.file, this.fileConfiguration, "versionHereLabel", "&9&nhere", true
      );
      versionSupportRoomMessage = ConfigurationEngine.setString(
         List.of("Message fragment about the support room."), this.file, this.fileConfiguration, "versionSupportRoomMessage", " &2for the support room.", true
      );
      versionAutoUpdateMessage = ConfigurationEngine.setString(
         List.of("Clickable message to auto-update all content."),
         this.file,
         this.fileConfiguration,
         "versionAutoUpdateMessage",
         "&a[Click here to update all content automatically]",
         true
      );
      versionAutoUpdateHover = ConfigurationEngine.setString(
         List.of("Hover text for the auto-update button."), this.file, this.fileConfiguration, "versionAutoUpdateHover", "&eRuns /em updatecontent", true
      );
      versionOutdatedEntryPrefix = ConfigurationEngine.setString(
         List.of("Prefix for each outdated content entry in the list."), this.file, this.fileConfiguration, "versionOutdatedEntryPrefix", "&c- ", true
      );
      versionNightbreakHover = ConfigurationEngine.setString(
         List.of("Hover text for the content link."), this.file, this.fileConfiguration, "versionNightbreakHover", "Click for the content page", true
      );
      versionWikiHover = ConfigurationEngine.setString(
         List.of("Hover text for the wiki link."), this.file, this.fileConfiguration, "versionWikiHover", "Click for wiki link", true
      );
      versionDiscordHover = ConfigurationEngine.setString(
         List.of("Hover text for the Discord support link."), this.file, this.fileConfiguration, "versionDiscordHover", "Discord support link", true
      );
      wormholeOpDownloadMessage = ConfigurationEngine.setString(
         List.of("Sets the OP-only message prefix about download links for wormhole content.", "Followed by clickable /em setup and Discord links."),
         this.file,
         this.fileConfiguration,
         "wormholeOpDownloadMessage",
         "&8[OP] &7This content is not installed. Use ",
         true
      );
      wormholeOpSetupMessage = ConfigurationEngine.setString(
         List.of(
            "Sets the OP-only message shown when a wormhole destination is not set up yet.",
            "$filename - the wormhole config filename",
            "$number - the wormhole entry number (1 or 2)"
         ),
         this.file,
         this.fileConfiguration,
         "wormholeOpSetupMessage",
         "&8[EliteMobs - OP-only message] &fTo set up this portal, stand at the desired destination and run: &a/em place wormhole $filename $number",
         true
      );
      wormholeAgSpawnRecommendation = ConfigurationEngine.setString(
         List.of("Sets the OP-only message recommending where to place the Adventurer's Guild wormhole."),
         this.file,
         this.fileConfiguration,
         "wormholeAgSpawnRecommendation",
         "&8[EliteMobs - OP-only message] &fThe official recommendation is to place this at your server's spawn so players can access the Adventurer's Guild through this portal!",
         true
      );
      downloadAllNoTokenMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when no account token is registered for download all"),
         this.file,
         this.fileConfiguration,
         "downloadAllNoTokenMessage",
         "&c[EliteMobs] No account token registered. Use /nightbreaklogin <token> first.",
         true
      );
      downloadAllNothingMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when all available packages are already downloaded"),
         this.file,
         this.fileConfiguration,
         "downloadAllNothingMessage",
         "&a[EliteMobs] No new content to download! All available packages are already downloaded.",
         true
      );
      downloadAllNoAccessMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when no accessible content is found"),
         this.file,
         this.fileConfiguration,
         "downloadAllNoAccessMessage",
         "&c[EliteMobs] No accessible content found. Connect this server and ensure you have access.",
         true
      );
      downloadAllFoundMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when downloadable packages are found", "$count - the number of packages"),
         this.file,
         this.fileConfiguration,
         "downloadAllFoundMessage",
         "&e[EliteMobs] Found $count packages to download. Starting...",
         true
      );
      downloadAllProgressMessage = ConfigurationEngine.setString(
         List.of("Sets the message showing download progress", "$current - current package number", "$total - total packages", "$name - package name"),
         this.file,
         this.fileConfiguration,
         "downloadAllProgressMessage",
         "&7[EliteMobs] ($current/$total) Downloading: $name...",
         true
      );
      downloadAllDownloadedMoreMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when a package is downloaded with more remaining", "$name - package name", "$remaining - number remaining"),
         this.file,
         this.fileConfiguration,
         "downloadAllDownloadedMoreMessage",
         "&a[EliteMobs] Downloaded $name! &7Please hold on, $remaining more to go...",
         true
      );
      downloadAllDownloadedMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when a package is downloaded (last one)", "$name - package name"),
         this.file,
         this.fileConfiguration,
         "downloadAllDownloadedMessage",
         "&a[EliteMobs] Downloaded $name!",
         true
      );
      downloadAllFailedMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when a package download fails", "$name - package name"),
         this.file,
         this.fileConfiguration,
         "downloadAllFailedMessage",
         "&c[EliteMobs] Failed to download: $name",
         true
      );
      downloadAllCompleteMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when all downloads are complete", "$completed - number of successful downloads", "$failed - number of failed downloads"),
         this.file,
         this.fileConfiguration,
         "downloadAllCompleteMessage",
         "&a[EliteMobs] Download complete! Downloaded: $completed, Failed: $failed",
         true
      );
      downloadAllReloadingMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when reloading to apply downloads"),
         this.file,
         this.fileConfiguration,
         "downloadAllReloadingMessage",
         "&a[EliteMobs] Reloading to apply downloads...",
         true
      );
      downloadAllAlreadyRunningMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when a bulk download is already in progress"),
         this.file,
         this.fileConfiguration,
         "downloadAllAlreadyRunningMessage",
         "&c[EliteMobs] A bulk download is already in progress! Please wait for it to finish.",
         true
      );
      downloadAllFailedListMessage = ConfigurationEngine.setString(
         List.of("Sets the message listing which packages failed to download", "$names - comma-separated list of failed package names"),
         this.file,
         this.fileConfiguration,
         "downloadAllFailedListMessage",
         "&c[EliteMobs] Failed packages: $names",
         true
      );
      downloadAllSetupClickMessage = ConfigurationEngine.setString(
         List.of("Clickable message to download all available content from the setup menu"),
         this.file,
         this.fileConfiguration,
         "downloadAllSetupClickMessage",
         "&a[Click to download all available content]",
         true
      );
      downloadAllSetupClickHover = ConfigurationEngine.setString(
         List.of("Hover text for the download all button in setup menu"),
         this.file,
         this.fileConfiguration,
         "downloadAllSetupClickHover",
         "&eRuns /em downloadall",
         true
      );
      updateNoTokenMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when no account token is registered"),
         this.file,
         this.fileConfiguration,
         "updateNoTokenMessage",
         "&c[EliteMobs] No account token registered. Use /nightbreaklogin <token> first.",
         true
      );
      updateAllUpToDateMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when all content is up to date"),
         this.file,
         this.fileConfiguration,
         "updateAllUpToDateMessage",
         "&a[EliteMobs] All content is up to date!",
         true
      );
      updateFoundOutdatedMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when outdated packages are found", "$count - the number of outdated packages"),
         this.file,
         this.fileConfiguration,
         "updateFoundOutdatedMessage",
         "&e[EliteMobs] Found $count outdated packages. Starting updates...",
         true
      );
      updateCompleteMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when updates are complete", "$completed - number of successful downloads", "$failed - number of failed downloads"),
         this.file,
         this.fileConfiguration,
         "updateCompleteMessage",
         "&a[EliteMobs] Update complete! Downloaded: $completed, Failed: $failed",
         true
      );
      updateReloadingMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when reloading to apply updates"),
         this.file,
         this.fileConfiguration,
         "updateReloadingMessage",
         "&a[EliteMobs] Reloading to apply updates...",
         true
      );
      updateProgressMessage = ConfigurationEngine.setString(
         List.of("Sets the message showing download progress", "$current - current package number", "$total - total packages", "$name - package name"),
         this.file,
         this.fileConfiguration,
         "updateProgressMessage",
         "&7[EliteMobs] ($current/$total) Downloading: $name...",
         true
      );
      updateDownloadedMoreMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when a package is downloaded with more remaining", "$name - package name", "$remaining - number remaining"),
         this.file,
         this.fileConfiguration,
         "updateDownloadedMoreMessage",
         "&a[EliteMobs] Downloaded $name! &7Please hold on, $remaining more to go...",
         true
      );
      updateDownloadedMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when a package is downloaded (last one)", "$name - package name"),
         this.file,
         this.fileConfiguration,
         "updateDownloadedMessage",
         "&a[EliteMobs] Downloaded $name!",
         true
      );
      updateFailedMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when a package download fails", "$name - package name"),
         this.file,
         this.fileConfiguration,
         "updateFailedMessage",
         "&c[EliteMobs] Failed to download: $name",
         true
      );
      farmingWarningMessage = ConfigurationEngine.setString(
         List.of("Sets the message shown when a player is farming natural elites too quickly.", "$time is the placeholder for the time until reset."),
         this.file,
         this.fileConfiguration,
         "farmingWarningMessage",
         "&c[EliteMobs] &7You've been farming natural elites too quickly! No drops or XP will be awarded. Spread out your farming area or wait for the timeout to reset. Time until reset: &e$time &7seconds.",
         true
      );
      farmingResetMessage = ConfigurationEngine.setString(
         List.of("Sets the message shown when a player's natural elite kill limit has been reset."),
         this.file,
         this.fileConfiguration,
         "farmingResetMessage",
         "&a[EliteMobs] &7Your natural elite kill limit has been reset!",
         true
      );
      farmingCapMessage = ConfigurationEngine.setString(
         List.of("Sets the message shown when a player is temporarily capped from earning rewards.", "$time is the placeholder for the time until reset."),
         this.file,
         this.fileConfiguration,
         "farmingCapMessage",
         "&c[EliteMobs] &7You're temporarily capped from earning rewards from natural elites in this area. Time until reset: &e$time &7seconds.",
         true
      );
      lootNeedMessage = ConfigurationEngine.setString(
         List.of(
            "Sets the message shown when a player selects need for a loot item.",
            "$player is the placeholder for the player's display name.",
            "$item is the placeholder for the item's display name."
         ),
         this.file,
         this.fileConfiguration,
         "lootNeedMessage",
         "$player &chas selected need for $item!",
         true
      );
      lootGreedMessage = ConfigurationEngine.setString(
         List.of(
            "Sets the message shown when a player selects greed for a loot item.",
            "$player is the placeholder for the player's display name.",
            "$item is the placeholder for the item's display name."
         ),
         this.file,
         this.fileConfiguration,
         "lootGreedMessage",
         "$player &2has selected greed for $item!",
         true
      );
      lootWinnerMessage = ConfigurationEngine.setString(
         List.of(
            "Sets the message shown when a player wins a shared loot item.",
            "$player is the placeholder for the winning player's display name.",
            "$item is the placeholder for the item's display name."
         ),
         this.file,
         this.fileConfiguration,
         "lootWinnerMessage",
         "$player &awon $item!",
         true
      );
      questInvalidIdMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when a quest ID is invalid."),
         this.file,
         this.fileConfiguration,
         "questInvalidIdMessage",
         "&c[EliteMobs] Invalid quest ID!",
         true
      );
      questBypassOnMessage = ConfigurationEngine.setString(
         List.of("Sets the message shown when quest bypass is enabled."),
         this.file,
         this.fileConfiguration,
         "questBypassOnMessage",
         "Now bypassing quest permission requirements!",
         true
      );
      questBypassOffMessage = ConfigurationEngine.setString(
         List.of("Sets the message shown when quest bypass is disabled."),
         this.file,
         this.fileConfiguration,
         "questBypassOffMessage",
         "No longer bypassing quest permission requirements!",
         true
      );
      invalidTreasureChestFilename = ConfigurationEngine.setString(
         List.of("Sets the message shown when a treasure chest file name is invalid."),
         this.file,
         this.fileConfiguration,
         "invalidTreasureChestFilename",
         "&c[EliteMobs] Invalid Treasure Chest file name!",
         true
      );
      relativePositionMessage = ConfigurationEngine.setString(
         List.of(
            "Sets the message showing the relative position to an anchor point.",
            "$filename is the placeholder for the dungeon name.",
            "$coordinates is the placeholder for the relative coordinates."
         ),
         this.file,
         this.fileConfiguration,
         "relativePositionMessage",
         "&8[EliteMobs] &fRelative position to anchor point of &2$filename&f is &2$coordinates",
         true
      );
      cheatSwordDisplayName = ConfigurationEngine.setString(
         List.of("Display name for the cheat sword given by the get tier command."),
         this.file,
         this.fileConfiguration,
         "cheatSwordDisplayName",
         "CHEAT SWORD",
         true
      );
   }

   @Generated
   public static String getPayNiceTryMessage() {
      return payNiceTryMessage;
   }

   @Generated
   public static String getPayPlayerNotOnlineMessage() {
      return payPlayerNotOnlineMessage;
   }

   @Generated
   public static String getCurrencyPlayerNotValidMessage() {
      return currencyPlayerNotValidMessage;
   }

   @Generated
   public static String getCurrencyDataNotReadyMessage() {
      return currencyDataNotReadyMessage;
   }

   @Generated
   public static String getCurrencyAddedMessage() {
      return currencyAddedMessage;
   }

   @Generated
   public static String getCurrencyNowHasMessage() {
      return currencyNowHasMessage;
   }

   @Generated
   public static String getCurrencyAddedAllMessage() {
      return currencyAddedAllMessage;
   }

   @Generated
   public static String getCurrencySubtractedMessage() {
      return currencySubtractedMessage;
   }

   @Generated
   public static String getCurrencySetMessage() {
      return currencySetMessage;
   }

   @Generated
   public static String getCurrencyCheckMessage() {
      return currencyCheckMessage;
   }

   @Generated
   public static String getDungeonNotValidMessage() {
      return dungeonNotValidMessage;
   }

   @Generated
   public static String getDungeonNotInstalledMessage() {
      return dungeonNotInstalledMessage;
   }

   @Generated
   public static String getAlreadyInInstanceMessage() {
      return alreadyInInstanceMessage;
   }

   @Generated
   public static String getDungeonTeleportNotSetMessage() {
      return dungeonTeleportNotSetMessage;
   }

   @Generated
   public static String getDungeonLockoutPlayerNotValidMessage() {
      return dungeonLockoutPlayerNotValidMessage;
   }

   @Generated
   public static String getDungeonLockoutResetSuccessMessage() {
      return dungeonLockoutResetSuccessMessage;
   }

   @Generated
   public static String getUnbindForceSuccessMessage() {
      return unbindForceSuccessMessage;
   }

   @Generated
   public static String getUnbindForceNotSoulboundMessage() {
      return unbindForceNotSoulboundMessage;
   }

   @Generated
   public static String getUnbindForceAllMessage() {
      return unbindForceAllMessage;
   }

   @Generated
   public static String getQuestPlayerNotValidMessage() {
      return questPlayerNotValidMessage;
   }

   @Generated
   public static String getQuestResetSuccessMessage() {
      return questResetSuccessMessage;
   }

   @Generated
   public static String getQuestNameNotValidMessage() {
      return questNameNotValidMessage;
   }

   @Generated
   public static String getQuestCompletedSuccessMessage() {
      return questCompletedSuccessMessage;
   }

   @Generated
   public static String getNoActiveQuestsMessage() {
      return noActiveQuestsMessage;
   }

   @Generated
   public static String getQuestNotFoundMessage() {
      return questNotFoundMessage;
   }

   @Generated
   public static String getNoSafeSpawnMessage() {
      return noSafeSpawnMessage;
   }

   @Generated
   public static String getBossNotValidRegionalMessage() {
      return bossNotValidRegionalMessage;
   }

   @Generated
   public static String getLeashRadiusFailedMessage() {
      return leashRadiusFailedMessage;
   }

   @Generated
   public static String getInvalidEntityTypeMessage() {
      return invalidEntityTypeMessage;
   }

   @Generated
   public static String getFilenameNotValidMessage() {
      return filenameNotValidMessage;
   }

   @Generated
   public static String getWorldNotValidMessage() {
      return worldNotValidMessage;
   }

   @Generated
   public static String getInvalidPowerMessage() {
      return invalidPowerMessage;
   }

   @Generated
   public static String getKilledElitesMessage() {
      return killedElitesMessage;
   }

   @Generated
   public static String getKilledEliteTypeMessage() {
      return killedEliteTypeMessage;
   }

   @Generated
   public static String getInvalidEliteTypeMessage() {
      return invalidEliteTypeMessage;
   }

   @Generated
   public static String getRemoveElitesOffMessage() {
      return removeElitesOffMessage;
   }

   @Generated
   public static String getRemoveElitesOnMessage() {
      return removeElitesOnMessage;
   }

   @Generated
   public static String getRemovedNotEliteMobsMessage() {
      return removedNotEliteMobsMessage;
   }

   @Generated
   public static String getRemovedHijackedMessage() {
      return removedHijackedMessage;
   }

   @Generated
   public static String getRemovedTreasureChestMessage() {
      return removedTreasureChestMessage;
   }

   @Generated
   public static String getRemovedSpawnLocationMessage() {
      return removedSpawnLocationMessage;
   }

   @Generated
   public static String getInvalidNpcFilenameMessage() {
      return invalidNpcFilenameMessage;
   }

   @Generated
   public static String getGaveItemMessage() {
      return gaveItemMessage;
   }

   @Generated
   public static String getInvalidItemFilenameMessage() {
      return invalidItemFilenameMessage;
   }

   @Generated
   public static String getInvalidPlayerForItemMessage() {
      return invalidPlayerForItemMessage;
   }

   @Generated
   public static String getEventQueuedMessage() {
      return eventQueuedMessage;
   }

   @Generated
   public static String getInvalidEventMessage() {
      return invalidEventMessage;
   }

   @Generated
   public static String getEventCancelledMessage() {
      return eventCancelledMessage;
   }

   @Generated
   public static String getEventNoSpawnTypeMessage() {
      return eventNoSpawnTypeMessage;
   }

   @Generated
   public static String getEventInvalidSpawnTypeMessage() {
      return eventInvalidSpawnTypeMessage;
   }

   @Generated
   public static String getEventNoValidBossesMessage() {
      return eventNoValidBossesMessage;
   }

   @Generated
   public static String getInvalidMinidungeonMessage() {
      return invalidMinidungeonMessage;
   }

   @Generated
   public static String getMinidungeonNotInstalledMessage() {
      return minidungeonNotInstalledMessage;
   }

   @Generated
   public static String getInvalidAnchorPointMessage() {
      return invalidAnchorPointMessage;
   }

   @Generated
   public static String getPackageDoneMessage() {
      return packageDoneMessage;
   }

   @Generated
   public static String getPackageDontForgetMessage() {
      return packageDontForgetMessage;
   }

   @Generated
   public static String getPackageZipFailedMessage() {
      return packageZipFailedMessage;
   }

   @Generated
   public static String getPackageZippedMessage() {
      return packageZippedMessage;
   }

   @Generated
   public static String getPackageNeedsDungeonNameMessage() {
      return packageNeedsDungeonNameMessage;
   }

   @Generated
   public static String getPackageNeedsNumberMessage() {
      return packageNeedsNumberMessage;
   }

   @Generated
   public static String getPackageNoSubdirectoryMessage() {
      return packageNoSubdirectoryMessage;
   }

   @Generated
   public static String getPackageFailedDirectoryMessage() {
      return packageFailedDirectoryMessage;
   }

   @Generated
   public static String getMissingPermissionMessage() {
      return missingPermissionMessage;
   }

   @Generated
   public static String getRegionalBossStatsMessage() {
      return regionalBossStatsMessage;
   }

   @Generated
   public static String getLanguageNotFoundMessage() {
      return languageNotFoundMessage;
   }

   @Generated
   public static String getLanguageListPrefix() {
      return languageListPrefix;
   }

   @Generated
   public static String getLanguageSetEnglishMessage() {
      return languageSetEnglishMessage;
   }

   @Generated
   public static String getLanguageGeneratingCustomMessage() {
      return languageGeneratingCustomMessage;
   }

   @Generated
   public static String getLanguageGenerateFailedMessage() {
      return languageGenerateFailedMessage;
   }

   @Generated
   public static String getLanguageGenerateSuccessMessage() {
      return languageGenerateSuccessMessage;
   }

   @Generated
   public static String getLanguageSetCustomMessage() {
      return languageSetCustomMessage;
   }

   @Generated
   public static String getLanguageDownloadingMessage() {
      return languageDownloadingMessage;
   }

   @Generated
   public static String getLanguageDownloadFailedMessage() {
      return languageDownloadFailedMessage;
   }

   @Generated
   public static String getLanguageDownloadSuccessMessage() {
      return languageDownloadSuccessMessage;
   }

   @Generated
   public static String getLanguageSetMessage() {
      return languageSetMessage;
   }

   @Generated
   public static String getSkillPlayerNotFoundMessage() {
      return skillPlayerNotFoundMessage;
   }

   @Generated
   public static String getSkillCheckHeaderMessage() {
      return skillCheckHeaderMessage;
   }

   @Generated
   public static String getSkillCheckEntryFormat() {
      return skillCheckEntryFormat;
   }

   @Generated
   public static String getSkillInvalidTypeMessage() {
      return skillInvalidTypeMessage;
   }

   @Generated
   public static String getSkillValidTypesMessage() {
      return skillValidTypesMessage;
   }

   @Generated
   public static String getSkillLevelMinMessage() {
      return skillLevelMinMessage;
   }

   @Generated
   public static String getSkillLevelWarningMessage() {
      return skillLevelWarningMessage;
   }

   @Generated
   public static String getSkillSetSuccessMessage() {
      return skillSetSuccessMessage;
   }

   @Generated
   public static String getSkillSetAllSuccessMessage() {
      return skillSetAllSuccessMessage;
   }

   @Generated
   public static String getSkillTestOnlyPlayerMessage() {
      return skillTestOnlyPlayerMessage;
   }

   @Generated
   public static String getSkillTestAlreadyRunningMessage() {
      return skillTestAlreadyRunningMessage;
   }

   @Generated
   public static String getSkillTestStartMessage() {
      return skillTestStartMessage;
   }

   @Generated
   public static String getSkillTestInfoMessage1() {
      return skillTestInfoMessage1;
   }

   @Generated
   public static String getSkillTestInfoMessage2() {
      return skillTestInfoMessage2;
   }

   @Generated
   public static String getSkillTestCancelNoTestMessage() {
      return skillTestCancelNoTestMessage;
   }

   @Generated
   public static String getSkillTestCancelSuccessMessage() {
      return skillTestCancelSuccessMessage;
   }

   @Generated
   public static String getDebugEnabledMessage() {
      return debugEnabledMessage;
   }

   @Generated
   public static String getDebugDisabledMessage() {
      return debugDisabledMessage;
   }

   @Generated
   public static String getDiscordMessage() {
      return discordMessage;
   }

   @Generated
   public static String getDiscordMessageSentMessage() {
      return discordMessageSentMessage;
   }

   @Generated
   public static String getEliteScrollConfigMessage() {
      return eliteScrollConfigMessage;
   }

   @Generated
   public static String getScrollsNotEnabledMessage() {
      return scrollsNotEnabledMessage;
   }

   @Generated
   public static String getScrollInvalidNumberMessage() {
      return scrollInvalidNumberMessage;
   }

   @Generated
   public static String getScrollLevelZeroMessage() {
      return scrollLevelZeroMessage;
   }

   @Generated
   public static String getScrollAmountZeroMessage() {
      return scrollAmountZeroMessage;
   }

   @Generated
   public static String getScrollGaveMessage() {
      return scrollGaveMessage;
   }

   @Generated
   public static String getHelpHeaderMessage() {
      return helpHeaderMessage;
   }

   @Generated
   public static String getWormholeInvalidOptionMessage() {
      return wormholeInvalidOptionMessage;
   }

   @Generated
   public static String getWormholeSetFailedMessage() {
      return wormholeSetFailedMessage;
   }

   @Generated
   public static String getProtectionBypassMessage() {
      return protectionBypassMessage;
   }

   @Generated
   public static String getSetupDoneDisableMessage() {
      return setupDoneDisableMessage;
   }

   @Generated
   public static String getSetupDoneEnableMessage() {
      return setupDoneEnableMessage;
   }

   @Generated
   public static String getSetupNotValidPackageMessage() {
      return setupNotValidPackageMessage;
   }

   @Generated
   public static String getSetupInstalledMessage() {
      return setupInstalledMessage;
   }

   @Generated
   public static String getSetupUninstalledMessage() {
      return setupUninstalledMessage;
   }

   @Generated
   public static String getShopPlayerNotFoundMessage() {
      return shopPlayerNotFoundMessage;
   }

   @Generated
   public static String getNotQueuedForInstanceMessage() {
      return notQueuedForInstanceMessage;
   }

   @Generated
   public static String getReloadStartMessage() {
      return reloadStartMessage;
   }

   @Generated
   public static String getReloadSuccessMessage() {
      return reloadSuccessMessage;
   }

   @Generated
   public static String getGetTierGaveGearMessage() {
      return getTierGaveGearMessage;
   }

   @Generated
   public static String getGetTierIronSwordMessage() {
      return getTierIronSwordMessage;
   }

   @Generated
   public static String getGetTierCheatSwordMessage() {
      return getTierCheatSwordMessage;
   }

   @Generated
   public static String getTransitiveBlockStartMessage() {
      return transitiveBlockStartMessage;
   }

   @Generated
   public static String getTransitiveBlockInvalidBossMessage() {
      return transitiveBlockInvalidBossMessage;
   }

   @Generated
   public static String getTransitiveBlockNotRegionalMessage() {
      return transitiveBlockNotRegionalMessage;
   }

   @Generated
   public static String getTransitiveBlockIgnoreWarningMessage() {
      return transitiveBlockIgnoreWarningMessage;
   }

   @Generated
   public static String getTransitiveBlockInvalidTypeMessage() {
      return transitiveBlockInvalidTypeMessage;
   }

   @Generated
   public static String getTransitiveBlockCorner1Message() {
      return transitiveBlockCorner1Message;
   }

   @Generated
   public static String getTransitiveBlockCorner2Message() {
      return transitiveBlockCorner2Message;
   }

   @Generated
   public static String getTransitiveBlockMissingCornersMessage() {
      return transitiveBlockMissingCornersMessage;
   }

   @Generated
   public static String getTransitiveBlockWorldMismatchMessage() {
      return transitiveBlockWorldMismatchMessage;
   }

   @Generated
   public static String getTransitiveBlockSelectionCountMessage() {
      return transitiveBlockSelectionCountMessage;
   }

   @Generated
   public static String getTransitiveBlockRegisteredMessage() {
      return transitiveBlockRegisteredMessage;
   }

   @Generated
   public static String getTransitiveBlockRegisteredAirMessage() {
      return transitiveBlockRegisteredAirMessage;
   }

   @Generated
   public static String getTransitiveBlockUnregisteredMessage() {
      return transitiveBlockUnregisteredMessage;
   }

   @Generated
   public static String getTransitiveBlockTooManyMessage() {
      return transitiveBlockTooManyMessage;
   }

   @Generated
   public static String getTransitiveBlockRequiresRegionalMessage() {
      return transitiveBlockRequiresRegionalMessage;
   }

   @Generated
   public static String getTransitiveBlockCancellingMessage() {
      return transitiveBlockCancellingMessage;
   }

   @Generated
   public static String getTransitiveBlockMultipleBossesMessage() {
      return transitiveBlockMultipleBossesMessage;
   }

   @Generated
   public static String getTransitiveBlockMultipleBossesHintMessage() {
      return transitiveBlockMultipleBossesHintMessage;
   }

   @Generated
   public static String getTransitiveBlockNowRegisteringMessage() {
      return transitiveBlockNowRegisteringMessage;
   }

   @Generated
   public static String getTransitiveBlockRunSameCommandMessage() {
      return transitiveBlockRunSameCommandMessage;
   }

   @Generated
   public static String getTransitiveBlockCancelCommandMessage() {
      return transitiveBlockCancelCommandMessage;
   }

   @Generated
   public static String getTransitiveBlockCancelledMessage() {
      return transitiveBlockCancelledMessage;
   }

   @Generated
   public static String getTransitiveBlockNowSavingMessage() {
      return transitiveBlockNowSavingMessage;
   }

   @Generated
   public static String getTransitiveBlockRegisteredCornerMessage() {
      return transitiveBlockRegisteredCornerMessage;
   }

   @Generated
   public static String getTransitiveBlockLocationsRegisteredMessage() {
      return transitiveBlockLocationsRegisteredMessage;
   }

   @Generated
   public static String getTransitiveBlockLocationsFailedMessage() {
      return transitiveBlockLocationsFailedMessage;
   }

   @Generated
   public static String getLootStatsHeader() {
      return lootStatsHeader;
   }

   @Generated
   public static String getLootStatsAttackSpeed() {
      return lootStatsAttackSpeed;
   }

   @Generated
   public static String getLootStatsDamage() {
      return lootStatsDamage;
   }

   @Generated
   public static String getLootStatsEdps() {
      return lootStatsEdps;
   }

   @Generated
   public static String getLootStatsLevel() {
      return lootStatsLevel;
   }

   @Generated
   public static String getLootStatsBonusEdps() {
      return lootStatsBonusEdps;
   }

   @Generated
   public static String getSimLootPlayerNotFound() {
      return simLootPlayerNotFound;
   }

   @Generated
   public static String getSimLootFinishedMultiple() {
      return simLootFinishedMultiple;
   }

   @Generated
   public static String getSimLootFinishedSingle() {
      return simLootFinishedSingle;
   }

   @Generated
   public static String getSimLootCouldNotSend() {
      return simLootCouldNotSend;
   }

   @Generated
   public static String getTrackedBossCountMessage() {
      return trackedBossCountMessage;
   }

   @Generated
   public static String getTrackedNpcCountMessage() {
      return trackedNpcCountMessage;
   }

   @Generated
   public static String getStatsSeparator() {
      return statsSeparator;
   }

   @Generated
   public static String getStatsVersionHeader() {
      return statsVersionHeader;
   }

   @Generated
   public static String getStatsEntityCountMessage() {
      return statsEntityCountMessage;
   }

   @Generated
   public static String getStatsBreakdownPrefix() {
      return statsBreakdownPrefix;
   }

   @Generated
   public static String getStatsHighestThreatMessage() {
      return statsHighestThreatMessage;
   }

   @Generated
   public static String getStatsAverageThreatMessage() {
      return statsAverageThreatMessage;
   }

   @Generated
   public static String getNpcFeatureComingSoonMessage() {
      return npcFeatureComingSoonMessage;
   }

   @Generated
   public static String getNpcCannotRenameMessage() {
      return npcCannotRenameMessage;
   }

   @Generated
   public static String getNpcFeatureReplacedMessage() {
      return npcFeatureReplacedMessage;
   }

   @Generated
   public static String getNpcRemoveInstructions() {
      return npcRemoveInstructions;
   }

   @Generated
   public static String getLootVoteSeparator() {
      return lootVoteSeparator;
   }

   @Generated
   public static String getLootVoteMessage() {
      return lootVoteMessage;
   }

   @Generated
   public static String getLootVoteMessageSuffix() {
      return lootVoteMessageSuffix;
   }

   @Generated
   public static String getVersionCheckConnectionWarning() {
      return versionCheckConnectionWarning;
   }

   @Generated
   public static String getVersionOutdatedMessage() {
      return versionOutdatedMessage;
   }

   @Generated
   public static String getResourcePackUpdatedMessage() {
      return resourcePackUpdatedMessage;
   }

   @Generated
   public static String getContentUpdatesAvailable() {
      return contentUpdatesAvailable;
   }

   @Generated
   public static String getContentUpdateEntry() {
      return contentUpdateEntry;
   }

   @Generated
   public static String getDungeonsOutdatedMessage() {
      return dungeonsOutdatedMessage;
   }

   @Generated
   public static String getVersionUseSetupMessage() {
      return versionUseSetupMessage;
   }

   @Generated
   public static String getVersionClickDownloadHover() {
      return versionClickDownloadHover;
   }

   @Generated
   public static String getVersionNoDownloadLink() {
      return versionNoDownloadLink;
   }

   @Generated
   public static String getVersionDownloadAtMessage() {
      return versionDownloadAtMessage;
   }

   @Generated
   public static String getVersionUpdateEasyMessage() {
      return versionUpdateEasyMessage;
   }

   @Generated
   public static String getVersionClickHereLabel() {
      return versionClickHereLabel;
   }

   @Generated
   public static String getVersionInstallInfoMessage() {
      return versionInstallInfoMessage;
   }

   @Generated
   public static String getVersionHereLabel() {
      return versionHereLabel;
   }

   @Generated
   public static String getVersionSupportRoomMessage() {
      return versionSupportRoomMessage;
   }

   @Generated
   public static String getVersionAutoUpdateMessage() {
      return versionAutoUpdateMessage;
   }

   @Generated
   public static String getVersionAutoUpdateHover() {
      return versionAutoUpdateHover;
   }

   @Generated
   public static String getVersionOutdatedEntryPrefix() {
      return versionOutdatedEntryPrefix;
   }

   @Generated
   public static String getVersionNightbreakHover() {
      return versionNightbreakHover;
   }

   @Generated
   public static String getVersionWikiHover() {
      return versionWikiHover;
   }

   @Generated
   public static String getVersionDiscordHover() {
      return versionDiscordHover;
   }

   @Generated
   public static String getWormholeOpDownloadMessage() {
      return wormholeOpDownloadMessage;
   }

   @Generated
   public static String getWormholeOpSetupMessage() {
      return wormholeOpSetupMessage;
   }

   @Generated
   public static String getWormholeAgSpawnRecommendation() {
      return wormholeAgSpawnRecommendation;
   }

   @Generated
   public static String getFarmingWarningMessage() {
      return farmingWarningMessage;
   }

   @Generated
   public static String getFarmingResetMessage() {
      return farmingResetMessage;
   }

   @Generated
   public static String getFarmingCapMessage() {
      return farmingCapMessage;
   }

   @Generated
   public static String getLootNeedMessage() {
      return lootNeedMessage;
   }

   @Generated
   public static String getLootGreedMessage() {
      return lootGreedMessage;
   }

   @Generated
   public static String getLootWinnerMessage() {
      return lootWinnerMessage;
   }

   @Generated
   public static String getQuestInvalidIdMessage() {
      return questInvalidIdMessage;
   }

   @Generated
   public static String getQuestBypassOnMessage() {
      return questBypassOnMessage;
   }

   @Generated
   public static String getQuestBypassOffMessage() {
      return questBypassOffMessage;
   }

   @Generated
   public static String getInvalidTreasureChestFilename() {
      return invalidTreasureChestFilename;
   }

   @Generated
   public static String getRelativePositionMessage() {
      return relativePositionMessage;
   }

   @Generated
   public static String getCheatSwordDisplayName() {
      return cheatSwordDisplayName;
   }

   @Generated
   public static String getDownloadAllNoTokenMessage() {
      return downloadAllNoTokenMessage;
   }

   @Generated
   public static String getDownloadAllNothingMessage() {
      return downloadAllNothingMessage;
   }

   @Generated
   public static String getDownloadAllNoAccessMessage() {
      return downloadAllNoAccessMessage;
   }

   @Generated
   public static String getDownloadAllFoundMessage() {
      return downloadAllFoundMessage;
   }

   @Generated
   public static String getDownloadAllProgressMessage() {
      return downloadAllProgressMessage;
   }

   @Generated
   public static String getDownloadAllDownloadedMoreMessage() {
      return downloadAllDownloadedMoreMessage;
   }

   @Generated
   public static String getDownloadAllDownloadedMessage() {
      return downloadAllDownloadedMessage;
   }

   @Generated
   public static String getDownloadAllFailedMessage() {
      return downloadAllFailedMessage;
   }

   @Generated
   public static String getDownloadAllCompleteMessage() {
      return downloadAllCompleteMessage;
   }

   @Generated
   public static String getDownloadAllReloadingMessage() {
      return downloadAllReloadingMessage;
   }

   @Generated
   public static String getDownloadAllAlreadyRunningMessage() {
      return downloadAllAlreadyRunningMessage;
   }

   @Generated
   public static String getDownloadAllFailedListMessage() {
      return downloadAllFailedListMessage;
   }

   @Generated
   public static String getDownloadAllSetupClickMessage() {
      return downloadAllSetupClickMessage;
   }

   @Generated
   public static String getDownloadAllSetupClickHover() {
      return downloadAllSetupClickHover;
   }

   @Generated
   public static String getUpdateNoTokenMessage() {
      return updateNoTokenMessage;
   }

   @Generated
   public static String getUpdateAllUpToDateMessage() {
      return updateAllUpToDateMessage;
   }

   @Generated
   public static String getUpdateFoundOutdatedMessage() {
      return updateFoundOutdatedMessage;
   }

   @Generated
   public static String getUpdateCompleteMessage() {
      return updateCompleteMessage;
   }

   @Generated
   public static String getUpdateReloadingMessage() {
      return updateReloadingMessage;
   }

   @Generated
   public static String getUpdateProgressMessage() {
      return updateProgressMessage;
   }

   @Generated
   public static String getUpdateDownloadedMoreMessage() {
      return updateDownloadedMoreMessage;
   }

   @Generated
   public static String getUpdateDownloadedMessage() {
      return updateDownloadedMessage;
   }

   @Generated
   public static String getUpdateFailedMessage() {
      return updateFailedMessage;
   }
}
