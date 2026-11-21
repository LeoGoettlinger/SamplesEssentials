package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.magmacore.config.ConfigurationFile;
import java.io.File;
import java.util.List;
import lombok.Generated;

public class PartyConfig extends ConfigurationFile {
   private static boolean enabled;
   private static double sharedProgressRange;
   private static int inviteTimeoutSeconds;
   private static int dungeonReadyCheckTimeoutSeconds;
   private static int lootVoteMaximumLifetimeSeconds;
   private static boolean sidebarEnabled;
   private static int sidebarRotationSeconds;
   private static boolean playerInteractionHintsEnabled;
   private static int playerInteractionHintCooldownSeconds;
   private static String prefix;
   private static String unknownPlayerName;
   private static String disabledMessage;
   private static String alreadyInPartyMessage;
   private static String notInPartyMessage;
   private static String partyCreatedMessage;
   private static String partyFullMessage;
   private static String playerUnavailableMessage;
   private static String playerAlreadyInPartyMessage;
   private static String selfInviteMessage;
   private static String inviteSentMessage;
   private static String inviteAlreadyPendingMessage;
   private static String playerCannotUsePartiesMessage;
   private static String inviteReceivedMessage;
   private static String inviteAcceptButton;
   private static String inviteAcceptHover;
   private static String noPendingInviteMessage;
   private static String inviteExpiredMessage;
   private static String joinedPartyMessage;
   private static String memberJoinedMessage;
   private static String leftPartyMessage;
   private static String memberLeftMessage;
   private static String leaderChangedMessage;
   private static String dungeonPartyTooLargeMessage;
   private static String dungeonPartyUnavailableMessage;
   private static String dungeonPartyInInstanceMessage;
   private static String dungeonPartyNoPermissionMessage;
   private static String dungeonPartyChangedMessage;
   private static String dungeonPartyJoinFailedMessage;
   private static String dungeonReadyCheckPendingMessage;
   private static String dungeonReadyCheckStartedMessage;
   private static String dungeonReadyCheckPromptMessage;
   private static String dungeonReadyCheckReadyButton;
   private static String dungeonReadyCheckReadyHover;
   private static String dungeonReadyCheckDeclineButton;
   private static String dungeonReadyCheckDeclineHover;
   private static String dungeonReadyCheckCancelPromptMessage;
   private static String dungeonReadyCheckCancelButton;
   private static String dungeonReadyCheckNoPendingMessage;
   private static String dungeonReadyCheckAlreadyReadyMessage;
   private static String dungeonReadyCheckPlayerReadyMessage;
   private static String dungeonReadyCheckCompleteMessage;
   private static String dungeonReadyCheckDeclinedMessage;
   private static String dungeonReadyCheckExpiredMessage;
   private static String dungeonReadyCheckUnknownDungeonName;
   private static String dungeonReadyCheckLevelFormat;
   private static String sidebarTitle;
   private static String sidebarLeaderLine;
   private static String sidebarMemberLine;
   private static String sidebarQuestLine;
   private static String sidebarHealthGlyph;
   private static String sidebarHealthHealthyColor;
   private static String sidebarHealthWoundedColor;
   private static String sidebarHealthCriticalColor;
   private static String sidebarHealthMissingColor;
   private static String sidebarDownedDisplay;
   private static String sidebarLivesDisplay;
   private static String sidebarInviteAction;
   private static String sidebarLeaveAction;
   private static String playerInteractionHintMessage;
   private static String playerInteractionHintInviteButton;
   private static String playerInteractionHintInviteHover;
   private static String playerInteractionHintDisableButton;
   private static String playerInteractionHintDisableHover;
   private static String playerInteractionHintDisabledMessage;
   private static String inventoryControlsTitle;
   private static String inventoryInvitePlayersTitle;
   private static String inventoryInteractionTitle;
   private static String inventoryInvitationTitle;
   private static String inventoryReadyCheckTitle;
   private static String inventoryInviteName;
   private static String inventoryInviteLore;
   private static String inventoryLeaveName;
   private static String inventoryLeaveLore;
   private static String inventoryCreateName;
   private static String inventoryCreateLore;
   private static String inventoryBackName;
   private static String inventoryBackToPartyName;
   private static String inventoryPreviousName;
   private static String inventoryNextName;
   private static String inventoryInvitePlayerName;
   private static String inventorySelectInviteLore;
   private static String inventoryNoInvitePlayersName;
   private static String inventoryNoInvitePlayersLore;
   private static String inventoryInteractionInviteName;
   private static String inventoryInteractionInviteLore;
   private static String inventoryNeverShowName;
   private static String inventoryNeverShowLore;
   private static String inventoryAcceptInviteName;
   private static String inventoryAcceptInviteLore;
   private static String inventoryDeclineName;
   private static String inventoryDeclineLore;
   private static String inventoryReadyDungeonName;
   private static String inventoryReadyWaitingLore;
   private static String inventoryReadyConfirmLore;
   private static String inventoryReadyName;
   private static String inventoryReadyLore;
   private static String inventoryCancelReadyName;
   private static String inventoryCancelReadyLore;
   private static String inventoryNoPermissionMessage;
   private static String commandCreateDescription;
   private static String commandInviteDescription;
   private static String commandMenuDescription;
   private static String commandAcceptDescription;
   private static String commandLeaveDescription;
   private static String commandReadyDescription;
   private static String commandDeclineDescription;
   private static String commandHideInteractionHintDescription;
   private static String readyCheckTokenHint;

   public PartyConfig() {
      super("Party.yml");
   }

   public PartyConfig(File file) {
      super(file);
   }

   @Override
   public void initializeValues() {
      enabled = ConfigurationEngine.setBoolean(
         List.of("Enables session-scoped player parties.", "Party membership is never saved and ends when players log out or leave."),
         this.fileConfiguration,
         "enabled",
         true
      );
      sharedProgressRange = Math.max(
         1.0,
         ConfigurationEngine.setDouble(
            List.of(
               "Maximum same-world distance in blocks for party loot voting and shared quest objective credit.",
               "This prevents remote or AFK party members from receiving progression."
            ),
            this.fileConfiguration,
            "sharedProgressRange",
            128.0
         )
      );
      inviteTimeoutSeconds = Math.max(
         5, ConfigurationEngine.setInt(List.of("How long a party invitation remains valid."), this.fileConfiguration, "inviteTimeoutSeconds", 60)
      );
      dungeonReadyCheckTimeoutSeconds = Math.max(
         5,
         ConfigurationEngine.setInt(
            List.of("How long party members have to accept an instanced dungeon ready check."), this.fileConfiguration, "dungeonReadyCheckTimeoutSeconds", 30
         )
      );
      lootVoteMaximumLifetimeSeconds = Math.max(
         60,
         ConfigurationEngine.setInt(
            List.of(
               "Hard maximum lifetime for one party need/greed vote session.",
               "New party drops still refresh the normal 60-second voting window, but can never extend a session past this limit."
            ),
            this.fileConfiguration,
            "lootVoteMaximumLifetimeSeconds",
            120
         )
      );
      sidebarEnabled = ConfigurationEngine.setBoolean(
         List.of(
            "Shows the combined party status and tracked quest sidebar while players are in a party.",
            "Disabling this keeps parties active and falls back to the normal EliteMobs quest scoreboard."
         ),
         this.fileConfiguration,
         "sidebarEnabled",
         true
      );
      sidebarRotationSeconds = Math.max(
         3,
         ConfigurationEngine.setInt(
            List.of("How often the party sidebar alternates between its invite and leave command hints."), this.fileConfiguration, "sidebarRotationSeconds", 10
         )
      );
      playerInteractionHintsEnabled = ConfigurationEngine.setBoolean(
         List.of(
            "Shows a short party invitation hint when a player right-clicks another player.", "This has no effect when the party system itself is disabled."
         ),
         this.fileConfiguration,
         "playerInteractionHintsEnabled",
         true
      );
      playerInteractionHintCooldownSeconds = Math.max(
         1,
         ConfigurationEngine.setInt(
            List.of("Minimum time before a player can receive another right-click party hint."),
            this.fileConfiguration,
            "playerInteractionHintCooldownSeconds",
            300
         )
      );
      this.migrateDefault("sidebarLeaderLine", "&6♛ &f$player", "&6♛ &f$player$health$lives");
      this.migrateDefault("sidebarMemberLine", "&a● &f$player", "&a● &f$player$health$lives");
      this.migrateWipVisualDefaults();
      prefix = this.message("prefix", "&8[<g:#A04468:#E07A9A>Party</g>&8] &7");
      unknownPlayerName = this.message("unknownPlayerName", "Unknown");
      disabledMessage = this.message("disabledMessage", "$prefixThe party system is disabled on this server.");
      alreadyInPartyMessage = this.message("alreadyInPartyMessage", "$prefixYou are already in a party.");
      notInPartyMessage = this.message("notInPartyMessage", "$prefixYou are not in a party.");
      partyCreatedMessage = this.message("partyCreatedMessage", "$prefix&aParty created! &7Invite players with &f/em party invite <player>&7.");
      this.migrateDefault("partyFullMessage", "$prefixThat party already has six players.", "$prefixThat party already has five players.");
      partyFullMessage = this.message("partyFullMessage", "$prefixThat party already has five players.");
      playerUnavailableMessage = this.message("playerUnavailableMessage", "$prefixThat player is not online.");
      playerAlreadyInPartyMessage = this.message("playerAlreadyInPartyMessage", "$prefixThat player is already in a party.");
      selfInviteMessage = this.message("selfInviteMessage", "$prefixYou cannot invite yourself.");
      inviteSentMessage = this.message("inviteSentMessage", "$prefixInvitation sent to &f$player&7.");
      inviteAlreadyPendingMessage = this.message("inviteAlreadyPendingMessage", "$prefix&f$player &7already has a pending party invitation.");
      playerCannotUsePartiesMessage = this.message("playerCannotUsePartiesMessage", "$prefix&f$player &7does not have permission to use parties.");
      inviteReceivedMessage = this.message("inviteReceivedMessage", "$prefix&f$player &7invited you to join their party. ");
      inviteAcceptButton = this.message("inviteAcceptButton", "<g:#2E7D4F:#69C56F>[ACCEPT]</g>");
      inviteAcceptHover = this.message("inviteAcceptHover", "&7Click to join the party");
      noPendingInviteMessage = this.message("noPendingInviteMessage", "$prefixYou do not have a pending party invitation.");
      inviteExpiredMessage = this.message("inviteExpiredMessage", "$prefixThat party invitation expired.");
      joinedPartyMessage = this.message("joinedPartyMessage", "$prefix&aYou joined the party!");
      memberJoinedMessage = this.message("memberJoinedMessage", "$prefix&f$player &ajoined the party.");
      leftPartyMessage = this.message("leftPartyMessage", "$prefixYou left the party.");
      memberLeftMessage = this.message("memberLeftMessage", "$prefix&f$player &cleft the party.");
      leaderChangedMessage = this.message("leaderChangedMessage", "$prefix&f$player &7is now the party leader.");
      dungeonPartyTooLargeMessage = this.message(
         "dungeonPartyTooLargeMessage", "$prefixParty entry needs &f$count &7open spots, but this dungeon only has &f$max &7available."
      );
      dungeonPartyUnavailableMessage = this.message("dungeonPartyUnavailableMessage", "$prefixParty entry stopped because &f$player &7is no longer available.");
      dungeonPartyInInstanceMessage = this.message(
         "dungeonPartyInInstanceMessage", "$prefixParty entry stopped because &f$player &7is already in another instance."
      );
      dungeonPartyNoPermissionMessage = this.message(
         "dungeonPartyNoPermissionMessage", "$prefixParty entry stopped because &f$player &7does not have permission for this dungeon."
      );
      dungeonPartyChangedMessage = this.message(
         "dungeonPartyChangedMessage", "$prefixThe party changed while the dungeon was being prepared. Please try again."
      );
      dungeonPartyJoinFailedMessage = this.message("dungeonPartyJoinFailedMessage", "$prefixThe party could not enter together, so nobody was moved.");
      dungeonReadyCheckPendingMessage = this.message("dungeonReadyCheckPendingMessage", "$prefixYour party already has a dungeon ready check in progress.");
      dungeonReadyCheckStartedMessage = this.message(
         "dungeonReadyCheckStartedMessage", "$prefix&f$player &7wants to enter &f$dungeon&7. Waiting for the party (&f$ready&7/&f$total&7)."
      );
      dungeonReadyCheckPromptMessage = this.message("dungeonReadyCheckPromptMessage", "$prefixReady to enter &f$dungeon&7? ");
      dungeonReadyCheckReadyButton = this.message("dungeonReadyCheckReadyButton", "<g:#2E7D4F:#69C56F>[READY]</g>");
      dungeonReadyCheckReadyHover = this.message("dungeonReadyCheckReadyHover", "&7Click to confirm dungeon entry");
      dungeonReadyCheckDeclineButton = this.message("dungeonReadyCheckDeclineButton", " <g:#7A1F2B:#C2414A>[DECLINE]</g>");
      dungeonReadyCheckDeclineHover = this.message("dungeonReadyCheckDeclineHover", "&7Click to cancel this party entry");
      dungeonReadyCheckCancelPromptMessage = this.message("dungeonReadyCheckCancelPromptMessage", "$prefixStarted the wrong dungeon? ");
      dungeonReadyCheckCancelButton = this.message("dungeonReadyCheckCancelButton", "<g:#7A1F2B:#C2414A>[CANCEL]</g>");
      dungeonReadyCheckNoPendingMessage = this.message("dungeonReadyCheckNoPendingMessage", "$prefixThat dungeon ready check is no longer active.");
      dungeonReadyCheckAlreadyReadyMessage = this.message("dungeonReadyCheckAlreadyReadyMessage", "$prefixYou are already ready for this dungeon.");
      dungeonReadyCheckPlayerReadyMessage = this.message("dungeonReadyCheckPlayerReadyMessage", "$prefix&f$player &ais ready &7(&f$ready&7/&f$total&7).");
      dungeonReadyCheckCompleteMessage = this.message("dungeonReadyCheckCompleteMessage", "$prefix&aEveryone is ready! &7Preparing &f$dungeon&7...");
      dungeonReadyCheckDeclinedMessage = this.message("dungeonReadyCheckDeclinedMessage", "$prefix&f$player &cdeclined &7the ready check for &f$dungeon&7.");
      dungeonReadyCheckExpiredMessage = this.message("dungeonReadyCheckExpiredMessage", "$prefixThe ready check for &f$dungeon &7expired.");
      dungeonReadyCheckUnknownDungeonName = this.message("dungeonReadyCheckUnknownDungeonName", "this dungeon");
      dungeonReadyCheckLevelFormat = this.message("dungeonReadyCheckLevelFormat", "$dungeon - level $level");
      sidebarTitle = this.message("sidebarTitle", "<g:#A04468:#E07A9A>♥ Elite Party</g>");
      sidebarLeaderLine = this.message("sidebarLeaderLine", "&6★ &f$player$health$lives");
      sidebarMemberLine = this.message("sidebarMemberLine", "&a● &f$player$health$lives");
      sidebarQuestLine = this.message("sidebarQuestLine", "&bQuest: &f$quest");
      sidebarHealthGlyph = this.message("sidebarHealthGlyph", "❤");
      sidebarHealthHealthyColor = this.message("sidebarHealthHealthyColor", "&a");
      sidebarHealthWoundedColor = this.message("sidebarHealthWoundedColor", "&e");
      sidebarHealthCriticalColor = this.message("sidebarHealthCriticalColor", "&c");
      sidebarHealthMissingColor = this.message("sidebarHealthMissingColor", "&8");
      sidebarDownedDisplay = this.message("sidebarDownedDisplay", " &c☠");
      sidebarLivesDisplay = this.message("sidebarLivesDisplay", " &b◆&f$lives");
      sidebarInviteAction = this.message("sidebarInviteAction", "&b✉ &f/em party invite <player>");
      sidebarLeaveAction = this.message("sidebarLeaveAction", "&c✘ &f/em party leave");
      playerInteractionHintMessage = this.message("playerInteractionHintMessage", "$prefixWant to team up with &f$player&7? ");
      playerInteractionHintInviteButton = this.message("playerInteractionHintInviteButton", "<g:#2E7D4F:#69C56F>[INVITE]</g>");
      playerInteractionHintInviteHover = this.message("playerInteractionHintInviteHover", "&7Invite &f$player &7to your party");
      playerInteractionHintDisableButton = this.message("playerInteractionHintDisableButton", "&8[Don't show again]");
      playerInteractionHintDisableHover = this.message("playerInteractionHintDisableHover", "&7Permanently hide right-click party hints");
      playerInteractionHintDisabledMessage = this.message("playerInteractionHintDisabledMessage", "$prefixRight-click party hints have been disabled for you.");
      inventoryControlsTitle = this.message("inventoryControlsTitle", "<g:#A04468:#E07A9A>♥ Elite Party</g>");
      inventoryInvitePlayersTitle = this.message("inventoryInvitePlayersTitle", "<g:#A04468:#E07A9A>✉ Invite a Player</g>");
      inventoryInteractionTitle = this.message("inventoryInteractionTitle", "<g:#A04468:#E07A9A>♥ Party with</g> &f$player");
      inventoryInvitationTitle = this.message("inventoryInvitationTitle", "<g:#A04468:#E07A9A>✉ Party Invitation</g>");
      inventoryReadyCheckTitle = this.message("inventoryReadyCheckTitle", "<g:#6D3AA8:#A855F7>★ Dungeon Ready Check</g>");
      inventoryInviteName = this.message("inventoryInviteName", "<g:#267A78:#58B8A9>✉ Invite a Player</g>");
      inventoryInviteLore = this.message("inventoryInviteLore", "&7Choose an online player to invite.");
      inventoryLeaveName = this.message("inventoryLeaveName", "<g:#7A1F2B:#C2414A>✘ Leave Party</g>");
      inventoryLeaveLore = this.message("inventoryLeaveLore", "&7Leave your current party.");
      inventoryCreateName = this.message("inventoryCreateName", "<g:#2E7D4F:#69C56F>+ Create a Party</g>");
      inventoryCreateLore = this.message("inventoryCreateLore", "&7Create a party for up to five players.");
      inventoryBackName = this.message("inventoryBackName", "&eBack to /em");
      inventoryBackToPartyName = this.message("inventoryBackToPartyName", "&eBack to Party Controls");
      inventoryPreviousName = this.message("inventoryPreviousName", "&ePrevious Page");
      inventoryNextName = this.message("inventoryNextName", "&eNext Page");
      inventoryInvitePlayerName = this.message("inventoryInvitePlayerName", "<g:#267A78:#58B8A9>✉ Invite</g> &f$player");
      inventorySelectInviteLore = this.message("inventorySelectInviteLore", "&7Click to invite.");
      inventoryNoInvitePlayersName = this.message("inventoryNoInvitePlayersName", "&7No Players Available");
      inventoryNoInvitePlayersLore = this.message("inventoryNoInvitePlayersLore", "&8Nobody online can currently be invited.");
      inventoryInteractionInviteName = this.message("inventoryInteractionInviteName", "<g:#267A78:#58B8A9>✉ Invite</g> &f$player");
      inventoryInteractionInviteLore = this.message("inventoryInteractionInviteLore", "&7Invite this player to your party.");
      inventoryNeverShowName = this.message("inventoryNeverShowName", "&8Never Show This Again");
      inventoryNeverShowLore = this.message("inventoryNeverShowLore", "&7Permanently hide right-click party hints.");
      inventoryAcceptInviteName = this.message("inventoryAcceptInviteName", "<g:#2E7D4F:#69C56F>Accept Invite</g> &f— $player");
      inventoryAcceptInviteLore = this.message("inventoryAcceptInviteLore", "&7Join their EliteMobs party.");
      inventoryDeclineName = this.message("inventoryDeclineName", "<g:#7A1F2B:#C2414A>Decline</g>");
      inventoryDeclineLore = this.message("inventoryDeclineLore", "&7Ignore this invitation.");
      inventoryReadyDungeonName = this.message("inventoryReadyDungeonName", "<g:#6D3AA8:#A855F7>★ Dungeon</g> &f$dungeon");
      inventoryReadyWaitingLore = this.message("inventoryReadyWaitingLore", "&7Waiting for your party.");
      inventoryReadyConfirmLore = this.message("inventoryReadyConfirmLore", "&7Confirm that you are ready to enter.");
      inventoryReadyName = this.message("inventoryReadyName", "<g:#2E7D4F:#69C56F>Ready</g>");
      inventoryReadyLore = this.message("inventoryReadyLore", "&7Enter when everyone is ready.");
      inventoryCancelReadyName = this.message("inventoryCancelReadyName", "<g:#7A1F2B:#C2414A>Cancel Ready Check</g>");
      inventoryCancelReadyLore = this.message("inventoryCancelReadyLore", "&7Cancel this party dungeon entry.");
      inventoryNoPermissionMessage = this.message("inventoryNoPermissionMessage", "&cYou do not have permission to use EliteMobs parties.");
      commandCreateDescription = this.message("commandCreateDescription", "Creates a session-scoped EliteMobs party.");
      commandInviteDescription = this.message("commandInviteDescription", "Invites an online player to your EliteMobs party.");
      commandMenuDescription = this.message("commandMenuDescription", "Opens compatible EliteMobs party controls.");
      commandAcceptDescription = this.message("commandAcceptDescription", "Accepts your pending EliteMobs party invitation.");
      commandLeaveDescription = this.message("commandLeaveDescription", "Leaves your current EliteMobs party.");
      commandReadyDescription = this.message("commandReadyDescription", "Accepts an active party dungeon ready check.");
      commandDeclineDescription = this.message("commandDeclineDescription", "Declines an active party dungeon ready check.");
      commandHideInteractionHintDescription = this.message("commandHideInteractionHintDescription", "Permanently hides right-click party invitation hints.");
      readyCheckTokenHint = this.message("readyCheckTokenHint", "ready check token");
   }

   private void migrateWipVisualDefaults() {
      this.migrateDefault("prefix", "&8[&6Party&8] &7", "&8[<g:#A04468:#E07A9A>Party</g>&8] &7");
      this.migrateDefault("inviteAcceptButton", "&a&l[ACCEPT]", "<g:#2E7D4F:#69C56F>[ACCEPT]</g>");
      this.migrateDefault("dungeonReadyCheckReadyButton", "&a&l[READY]", "<g:#2E7D4F:#69C56F>[READY]</g>");
      this.migrateDefault("dungeonReadyCheckDeclineButton", " &c&l[DECLINE]", " <g:#7A1F2B:#C2414A>[DECLINE]</g>");
      this.migrateDefault("dungeonReadyCheckCancelButton", "&c&l[CANCEL]", "<g:#7A1F2B:#C2414A>[CANCEL]</g>");
      this.migrateDefault("playerInteractionHintInviteButton", "&a&l[INVITE]", "<g:#2E7D4F:#69C56F>[INVITE]</g>");
      this.migrateDefault("sidebarTitle", "&6&lElite Party", "<g:#A04468:#E07A9A>♥ Elite Party</g>");
      this.migrateDefault("sidebarLeaderLine", "&6♛ &f$player$health$lives", "&6★ &f$player$health$lives");
      this.migrateDefault("sidebarLivesDisplay", " &b✦&f$lives", " &b◆&f$lives");
      this.migrateDefault("sidebarInviteAction", "&a➕ &f/em party invite <player>", "&b✉ &f/em party invite <player>");
      this.migrateDefault("sidebarLeaveAction", "&c✖ &f/em party leave", "&c✘ &f/em party leave");
      this.migrateDefault("inventoryControlsTitle", "&6Elite Party", "<g:#A04468:#E07A9A>♥ Elite Party</g>");
      this.migrateDefault("inventoryInvitePlayersTitle", "&6Invite a Player", "<g:#A04468:#E07A9A>✉ Invite a Player</g>");
      this.migrateDefault("inventoryInteractionTitle", "&6Party with $player", "<g:#A04468:#E07A9A>♥ Party with</g> &f$player");
      this.migrateDefault("inventoryInvitationTitle", "&6Party Invitation", "<g:#A04468:#E07A9A>✉ Party Invitation</g>");
      this.migrateDefault("inventoryReadyCheckTitle", "&6Dungeon Ready Check", "<g:#6D3AA8:#A855F7>★ Dungeon Ready Check</g>");
      this.migrateDefault("inventoryInviteName", "&aInvite a Player", "<g:#267A78:#58B8A9>✉ Invite a Player</g>");
      this.migrateDefault("inventoryLeaveName", "&cLeave Party", "<g:#7A1F2B:#C2414A>✘ Leave Party</g>");
      this.migrateDefault("inventoryCreateName", "&aCreate a Party", "<g:#2E7D4F:#69C56F>+ Create a Party</g>");
      this.migrateDefault("inventoryInvitePlayerName", "&a$player", "<g:#267A78:#58B8A9>✉ Invite</g> &f$player");
      this.migrateDefault("inventoryInteractionInviteName", "&aInvite $player", "<g:#267A78:#58B8A9>✉ Invite</g> &f$player");
      this.migrateDefault("inventoryAcceptInviteName", "&aAccept $player's Invite", "<g:#2E7D4F:#69C56F>Accept Invite</g> &f— $player");
      this.migrateDefault("inventoryDeclineName", "&cDecline", "<g:#7A1F2B:#C2414A>Decline</g>");
      this.migrateDefault("inventoryReadyDungeonName", "&e$dungeon", "<g:#6D3AA8:#A855F7>★ Dungeon</g> &f$dungeon");
      this.migrateDefault("inventoryReadyName", "&aReady", "<g:#2E7D4F:#69C56F>Ready</g>");
      this.migrateDefault("inventoryCancelReadyName", "&cCancel Ready Check", "<g:#7A1F2B:#C2414A>Cancel Ready Check</g>");
   }

   private String message(String key, String defaultValue) {
      return ConfigurationEngine.setString(List.of(), this.file, this.fileConfiguration, key, defaultValue, true);
   }

   private void migrateDefault(String key, String previousDefault, String currentDefault) {
      if (previousDefault.equals(this.fileConfiguration.getString(key))) {
         this.fileConfiguration.set(key, currentDefault);
      }
   }

   @Generated
   public static boolean isEnabled() {
      return enabled;
   }

   @Generated
   public static double getSharedProgressRange() {
      return sharedProgressRange;
   }

   @Generated
   public static int getInviteTimeoutSeconds() {
      return inviteTimeoutSeconds;
   }

   @Generated
   public static int getDungeonReadyCheckTimeoutSeconds() {
      return dungeonReadyCheckTimeoutSeconds;
   }

   @Generated
   public static int getLootVoteMaximumLifetimeSeconds() {
      return lootVoteMaximumLifetimeSeconds;
   }

   @Generated
   public static boolean isSidebarEnabled() {
      return sidebarEnabled;
   }

   @Generated
   public static int getSidebarRotationSeconds() {
      return sidebarRotationSeconds;
   }

   @Generated
   public static boolean isPlayerInteractionHintsEnabled() {
      return playerInteractionHintsEnabled;
   }

   @Generated
   public static int getPlayerInteractionHintCooldownSeconds() {
      return playerInteractionHintCooldownSeconds;
   }

   @Generated
   public static String getPrefix() {
      return prefix;
   }

   @Generated
   public static String getUnknownPlayerName() {
      return unknownPlayerName;
   }

   @Generated
   public static String getDisabledMessage() {
      return disabledMessage;
   }

   @Generated
   public static String getAlreadyInPartyMessage() {
      return alreadyInPartyMessage;
   }

   @Generated
   public static String getNotInPartyMessage() {
      return notInPartyMessage;
   }

   @Generated
   public static String getPartyCreatedMessage() {
      return partyCreatedMessage;
   }

   @Generated
   public static String getPartyFullMessage() {
      return partyFullMessage;
   }

   @Generated
   public static String getPlayerUnavailableMessage() {
      return playerUnavailableMessage;
   }

   @Generated
   public static String getPlayerAlreadyInPartyMessage() {
      return playerAlreadyInPartyMessage;
   }

   @Generated
   public static String getSelfInviteMessage() {
      return selfInviteMessage;
   }

   @Generated
   public static String getInviteSentMessage() {
      return inviteSentMessage;
   }

   @Generated
   public static String getInviteAlreadyPendingMessage() {
      return inviteAlreadyPendingMessage;
   }

   @Generated
   public static String getPlayerCannotUsePartiesMessage() {
      return playerCannotUsePartiesMessage;
   }

   @Generated
   public static String getInviteReceivedMessage() {
      return inviteReceivedMessage;
   }

   @Generated
   public static String getInviteAcceptButton() {
      return inviteAcceptButton;
   }

   @Generated
   public static String getInviteAcceptHover() {
      return inviteAcceptHover;
   }

   @Generated
   public static String getNoPendingInviteMessage() {
      return noPendingInviteMessage;
   }

   @Generated
   public static String getInviteExpiredMessage() {
      return inviteExpiredMessage;
   }

   @Generated
   public static String getJoinedPartyMessage() {
      return joinedPartyMessage;
   }

   @Generated
   public static String getMemberJoinedMessage() {
      return memberJoinedMessage;
   }

   @Generated
   public static String getLeftPartyMessage() {
      return leftPartyMessage;
   }

   @Generated
   public static String getMemberLeftMessage() {
      return memberLeftMessage;
   }

   @Generated
   public static String getLeaderChangedMessage() {
      return leaderChangedMessage;
   }

   @Generated
   public static String getDungeonPartyTooLargeMessage() {
      return dungeonPartyTooLargeMessage;
   }

   @Generated
   public static String getDungeonPartyUnavailableMessage() {
      return dungeonPartyUnavailableMessage;
   }

   @Generated
   public static String getDungeonPartyInInstanceMessage() {
      return dungeonPartyInInstanceMessage;
   }

   @Generated
   public static String getDungeonPartyNoPermissionMessage() {
      return dungeonPartyNoPermissionMessage;
   }

   @Generated
   public static String getDungeonPartyChangedMessage() {
      return dungeonPartyChangedMessage;
   }

   @Generated
   public static String getDungeonPartyJoinFailedMessage() {
      return dungeonPartyJoinFailedMessage;
   }

   @Generated
   public static String getDungeonReadyCheckPendingMessage() {
      return dungeonReadyCheckPendingMessage;
   }

   @Generated
   public static String getDungeonReadyCheckStartedMessage() {
      return dungeonReadyCheckStartedMessage;
   }

   @Generated
   public static String getDungeonReadyCheckPromptMessage() {
      return dungeonReadyCheckPromptMessage;
   }

   @Generated
   public static String getDungeonReadyCheckReadyButton() {
      return dungeonReadyCheckReadyButton;
   }

   @Generated
   public static String getDungeonReadyCheckReadyHover() {
      return dungeonReadyCheckReadyHover;
   }

   @Generated
   public static String getDungeonReadyCheckDeclineButton() {
      return dungeonReadyCheckDeclineButton;
   }

   @Generated
   public static String getDungeonReadyCheckDeclineHover() {
      return dungeonReadyCheckDeclineHover;
   }

   @Generated
   public static String getDungeonReadyCheckCancelPromptMessage() {
      return dungeonReadyCheckCancelPromptMessage;
   }

   @Generated
   public static String getDungeonReadyCheckCancelButton() {
      return dungeonReadyCheckCancelButton;
   }

   @Generated
   public static String getDungeonReadyCheckNoPendingMessage() {
      return dungeonReadyCheckNoPendingMessage;
   }

   @Generated
   public static String getDungeonReadyCheckAlreadyReadyMessage() {
      return dungeonReadyCheckAlreadyReadyMessage;
   }

   @Generated
   public static String getDungeonReadyCheckPlayerReadyMessage() {
      return dungeonReadyCheckPlayerReadyMessage;
   }

   @Generated
   public static String getDungeonReadyCheckCompleteMessage() {
      return dungeonReadyCheckCompleteMessage;
   }

   @Generated
   public static String getDungeonReadyCheckDeclinedMessage() {
      return dungeonReadyCheckDeclinedMessage;
   }

   @Generated
   public static String getDungeonReadyCheckExpiredMessage() {
      return dungeonReadyCheckExpiredMessage;
   }

   @Generated
   public static String getDungeonReadyCheckUnknownDungeonName() {
      return dungeonReadyCheckUnknownDungeonName;
   }

   @Generated
   public static String getDungeonReadyCheckLevelFormat() {
      return dungeonReadyCheckLevelFormat;
   }

   @Generated
   public static String getSidebarTitle() {
      return sidebarTitle;
   }

   @Generated
   public static String getSidebarLeaderLine() {
      return sidebarLeaderLine;
   }

   @Generated
   public static String getSidebarMemberLine() {
      return sidebarMemberLine;
   }

   @Generated
   public static String getSidebarQuestLine() {
      return sidebarQuestLine;
   }

   @Generated
   public static String getSidebarHealthGlyph() {
      return sidebarHealthGlyph;
   }

   @Generated
   public static String getSidebarHealthHealthyColor() {
      return sidebarHealthHealthyColor;
   }

   @Generated
   public static String getSidebarHealthWoundedColor() {
      return sidebarHealthWoundedColor;
   }

   @Generated
   public static String getSidebarHealthCriticalColor() {
      return sidebarHealthCriticalColor;
   }

   @Generated
   public static String getSidebarHealthMissingColor() {
      return sidebarHealthMissingColor;
   }

   @Generated
   public static String getSidebarDownedDisplay() {
      return sidebarDownedDisplay;
   }

   @Generated
   public static String getSidebarLivesDisplay() {
      return sidebarLivesDisplay;
   }

   @Generated
   public static String getSidebarInviteAction() {
      return sidebarInviteAction;
   }

   @Generated
   public static String getSidebarLeaveAction() {
      return sidebarLeaveAction;
   }

   @Generated
   public static String getPlayerInteractionHintMessage() {
      return playerInteractionHintMessage;
   }

   @Generated
   public static String getPlayerInteractionHintInviteButton() {
      return playerInteractionHintInviteButton;
   }

   @Generated
   public static String getPlayerInteractionHintInviteHover() {
      return playerInteractionHintInviteHover;
   }

   @Generated
   public static String getPlayerInteractionHintDisableButton() {
      return playerInteractionHintDisableButton;
   }

   @Generated
   public static String getPlayerInteractionHintDisableHover() {
      return playerInteractionHintDisableHover;
   }

   @Generated
   public static String getPlayerInteractionHintDisabledMessage() {
      return playerInteractionHintDisabledMessage;
   }

   @Generated
   public static String getInventoryControlsTitle() {
      return inventoryControlsTitle;
   }

   @Generated
   public static String getInventoryInvitePlayersTitle() {
      return inventoryInvitePlayersTitle;
   }

   @Generated
   public static String getInventoryInteractionTitle() {
      return inventoryInteractionTitle;
   }

   @Generated
   public static String getInventoryInvitationTitle() {
      return inventoryInvitationTitle;
   }

   @Generated
   public static String getInventoryReadyCheckTitle() {
      return inventoryReadyCheckTitle;
   }

   @Generated
   public static String getInventoryInviteName() {
      return inventoryInviteName;
   }

   @Generated
   public static String getInventoryInviteLore() {
      return inventoryInviteLore;
   }

   @Generated
   public static String getInventoryLeaveName() {
      return inventoryLeaveName;
   }

   @Generated
   public static String getInventoryLeaveLore() {
      return inventoryLeaveLore;
   }

   @Generated
   public static String getInventoryCreateName() {
      return inventoryCreateName;
   }

   @Generated
   public static String getInventoryCreateLore() {
      return inventoryCreateLore;
   }

   @Generated
   public static String getInventoryBackName() {
      return inventoryBackName;
   }

   @Generated
   public static String getInventoryBackToPartyName() {
      return inventoryBackToPartyName;
   }

   @Generated
   public static String getInventoryPreviousName() {
      return inventoryPreviousName;
   }

   @Generated
   public static String getInventoryNextName() {
      return inventoryNextName;
   }

   @Generated
   public static String getInventoryInvitePlayerName() {
      return inventoryInvitePlayerName;
   }

   @Generated
   public static String getInventorySelectInviteLore() {
      return inventorySelectInviteLore;
   }

   @Generated
   public static String getInventoryNoInvitePlayersName() {
      return inventoryNoInvitePlayersName;
   }

   @Generated
   public static String getInventoryNoInvitePlayersLore() {
      return inventoryNoInvitePlayersLore;
   }

   @Generated
   public static String getInventoryInteractionInviteName() {
      return inventoryInteractionInviteName;
   }

   @Generated
   public static String getInventoryInteractionInviteLore() {
      return inventoryInteractionInviteLore;
   }

   @Generated
   public static String getInventoryNeverShowName() {
      return inventoryNeverShowName;
   }

   @Generated
   public static String getInventoryNeverShowLore() {
      return inventoryNeverShowLore;
   }

   @Generated
   public static String getInventoryAcceptInviteName() {
      return inventoryAcceptInviteName;
   }

   @Generated
   public static String getInventoryAcceptInviteLore() {
      return inventoryAcceptInviteLore;
   }

   @Generated
   public static String getInventoryDeclineName() {
      return inventoryDeclineName;
   }

   @Generated
   public static String getInventoryDeclineLore() {
      return inventoryDeclineLore;
   }

   @Generated
   public static String getInventoryReadyDungeonName() {
      return inventoryReadyDungeonName;
   }

   @Generated
   public static String getInventoryReadyWaitingLore() {
      return inventoryReadyWaitingLore;
   }

   @Generated
   public static String getInventoryReadyConfirmLore() {
      return inventoryReadyConfirmLore;
   }

   @Generated
   public static String getInventoryReadyName() {
      return inventoryReadyName;
   }

   @Generated
   public static String getInventoryReadyLore() {
      return inventoryReadyLore;
   }

   @Generated
   public static String getInventoryCancelReadyName() {
      return inventoryCancelReadyName;
   }

   @Generated
   public static String getInventoryCancelReadyLore() {
      return inventoryCancelReadyLore;
   }

   @Generated
   public static String getInventoryNoPermissionMessage() {
      return inventoryNoPermissionMessage;
   }

   @Generated
   public static String getCommandCreateDescription() {
      return commandCreateDescription;
   }

   @Generated
   public static String getCommandInviteDescription() {
      return commandInviteDescription;
   }

   @Generated
   public static String getCommandMenuDescription() {
      return commandMenuDescription;
   }

   @Generated
   public static String getCommandAcceptDescription() {
      return commandAcceptDescription;
   }

   @Generated
   public static String getCommandLeaveDescription() {
      return commandLeaveDescription;
   }

   @Generated
   public static String getCommandReadyDescription() {
      return commandReadyDescription;
   }

   @Generated
   public static String getCommandDeclineDescription() {
      return commandDeclineDescription;
   }

   @Generated
   public static String getCommandHideInteractionHintDescription() {
      return commandHideInteractionHintDescription;
   }

   @Generated
   public static String getReadyCheckTokenHint() {
      return readyCheckTokenHint;
   }
}
