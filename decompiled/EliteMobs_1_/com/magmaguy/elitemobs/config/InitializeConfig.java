package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.magmacore.config.ConfigurationFile;
import com.magmaguy.elitemobs.magmacore.nightbreak.NightbreakChatStyle;
import com.magmaguy.elitemobs.magmacore.nightbreak.NightbreakSetupMenuHelper;
import java.util.List;
import lombok.Generated;

public class InitializeConfig extends ConfigurationFile {
   private static String menuTitle;
   private static String menuSubtitle;
   private static String separatorLine;
   private static String infoButtonName;
   private static String infoButtonLore1;
   private static String infoButtonLore2;
   private static String infoButtonLore3;
   private static String infoButtonClickSetupLink;
   private static String infoButtonClickContentBrowse;
   private static String infoButtonClickDiscord;
   private static String recommendedPresetName;
   private static String recommendedPresetLore1;
   private static String recommendedPresetLore2;
   private static String recommendedPresetLore3;
   private static String recommendedPresetLore4;
   private static String recommendedPresetLore5;
   private static String recommendedPresetClickConfirm;
   private static String recommendedPresetClickNightbreakLogin;
   private static String recommendedPresetClickDownloadAll;
   private static String recommendedPresetClickSetupGuide;
   private static String recommendedPresetClickDiscord;
   private static String recommendedPresetClickWiki;
   private static String contentOnlyPresetName;
   private static String contentOnlyPresetLore1;
   private static String contentOnlyPresetLore2;
   private static String contentOnlyPresetLore3;
   private static String contentOnlyPresetLore4;
   private static String contentOnlyPresetLore5;
   private static String contentOnlyPresetClickConfirm;
   private static String contentOnlyPresetClickNightbreakLogin;
   private static String contentOnlyPresetClickDownloadAll;
   private static String contentOnlyPresetClickSetupGuide;
   private static String contentOnlyPresetClickDiscord;
   private static String contentOnlyPresetClickWiki;
   private static String nothingPresetName;
   private static String nothingPresetLore1;
   private static String nothingPresetLore2;
   private static String nothingPresetLore3;
   private static String nothingPresetLore4;
   private static String nothingPresetLore5;
   private static String nothingPresetLore6;
   private static String nothingPresetClickConfirm;
   private static String nothingPresetClickWarning;
   private static String nothingPresetClickBetterStructures;
   private static String nothingPresetClickSetupGuide;
   private static String loginHeader;
   private static String loginWelcome;
   private static String loginHelpHint;
   private static String loginDiscord;
   private static String loginDismiss;
   private static String loginLanguagePrefix;
   private static String discordLinkDisplay;
   private static String discordLinkHover;
   private static String accountLinkDisplay;
   private static String accountLinkHover;
   private static String contentLinkDisplay;
   private static String contentLinkHover;
   private static String wikiLinkDisplay;
   private static String wikiLinkHover;
   private static String emInitializeDisplay;
   private static String emInitializeHover;
   private static String emSetupDisplay;
   private static String emSetupHover;
   private static String emCommandDisplay;
   private static String emCommandHover;
   private static String emLootDisplay;
   private static String emLootHover;
   private static String loginHelpHintSuffix;
   private static String infoButtonClickContentBrowseSuffix;
   private static String recommendedPresetClickDownloadAllSuffix;
   private static String contentOnlyPresetClickDownloadAllSuffix;

   public InitializeConfig() {
      super("initialize.yml");
   }

   @Override
   public void initializeValues() {
      this.fileConfiguration.addDefault("menuTitle", "<g:#228B22:#32CD32>EliteMobs</g>");
      this.fileConfiguration.setComments("menuTitle", List.of("Title shown in the /em initialize menu"));
      menuTitle = this.fileConfiguration.getString("menuTitle");
      this.fileConfiguration
         .addDefault(
            "menuSubtitle",
            "<g:#CD7F32:#FFD700>Events</g>&7, <g:#2E8B57:#3CB371>Dungeons</g>&7, <g:#4A7A9A:#6A9ABA>Arenas</g> &7& <g:#7B2FBE:#A855F7>More</g>&7!"
         );
      this.fileConfiguration.setComments("menuSubtitle", List.of("Subtitle shown in the /em initialize menu"));
      menuSubtitle = this.fileConfiguration.getString("menuSubtitle");
      NightbreakChatStyle.migrateSeparators(this.fileConfiguration, "separatorLine");
      separatorLine = ConfigurationEngine.setString(
         List.of("Separator line used in chat messages"), this.file, this.fileConfiguration, "separatorLine", NightbreakSetupMenuHelper.getSeparator(), true
      );
      infoButtonName = ConfigurationEngine.setString(
         List.of("Display name for the info button"),
         this.file,
         this.fileConfiguration,
         "infoButtonName",
         "&fWelcome to <g:#228B22:#32CD32>EliteMobs</g>&f, by <g:#CC4400:#DAA520>MagmaGuy</g>&f!",
         true
      );
      infoButtonLore1 = ConfigurationEngine.setString(
         List.of("First lore line for the info button"),
         this.file,
         this.fileConfiguration,
         "infoButtonLore1",
         "&7Click to get setup links and resources.",
         true
      );
      infoButtonLore2 = ConfigurationEngine.setString(
         List.of("Second lore line for the info button"),
         this.file,
         this.fileConfiguration,
         "infoButtonLore2",
         "&7Choose a preset below to configure your server.",
         true
      );
      infoButtonLore3 = ConfigurationEngine.setString(
         List.of("Third lore line for the info button"),
         this.file,
         this.fileConfiguration,
         "infoButtonLore3",
         "&6We recommend the &aRecommended preset&6!",
         true
      );
      infoButtonClickSetupLink = ConfigurationEngine.setString(
         List.of("Setup guide link sent when info button is clicked"), this.file, this.fileConfiguration, "infoButtonClickSetupLink", "&7Setup guide: ", true
      );
      infoButtonClickContentBrowse = ConfigurationEngine.setString(
         List.of("Content browse message sent when info button is clicked"),
         this.file,
         this.fileConfiguration,
         "infoButtonClickContentBrowse",
         "&7Browse content: ",
         true
      );
      infoButtonClickContentBrowseSuffix = ConfigurationEngine.setString(
         List.of("Text between the /em setup command and the content link"),
         this.file,
         this.fileConfiguration,
         "infoButtonClickContentBrowseSuffix",
         " &8or ",
         true
      );
      infoButtonClickDiscord = ConfigurationEngine.setString(
         List.of("Discord link sent when info button is clicked"), this.file, this.fileConfiguration, "infoButtonClickDiscord", "&7Discord: ", true
      );
      recommendedPresetName = ConfigurationEngine.setString(
         List.of("Display name for the recommended preset button"),
         this.file,
         this.fileConfiguration,
         "recommendedPresetName",
         "<g:#228B22:#32CD32>Recommended Preset</g> &7- Full Install",
         true
      );
      recommendedPresetLore1 = ConfigurationEngine.setString(
         List.of("First lore line for the recommended preset"),
         this.file,
         this.fileConfiguration,
         "recommendedPresetLore1",
         "&7Enables all EliteMobs features.",
         true
      );
      recommendedPresetLore2 = ConfigurationEngine.setString(
         List.of("Second lore line for the recommended preset"),
         this.file,
         this.fileConfiguration,
         "recommendedPresetLore2",
         "&7Custom items, MMORPG progression",
         true
      );
      recommendedPresetLore3 = ConfigurationEngine.setString(
         List.of("Third lore line for the recommended preset"),
         this.file,
         this.fileConfiguration,
         "recommendedPresetLore3",
         "&7& naturally spawning elites.",
         true
      );
      recommendedPresetLore4 = ConfigurationEngine.setString(
         List.of("Fourth lore line for the recommended preset"),
         this.file,
         this.fileConfiguration,
         "recommendedPresetLore4",
         "&7Works with &fBetterStructures&7.",
         true
      );
      recommendedPresetLore5 = ConfigurationEngine.setString(
         List.of("Fifth lore line for the recommended preset"), this.file, this.fileConfiguration, "recommendedPresetLore5", "&aClick to select!", true
      );
      recommendedPresetClickConfirm = ConfigurationEngine.setString(
         List.of("Confirmation message sent after selecting recommended preset"),
         this.file,
         this.fileConfiguration,
         "recommendedPresetClickConfirm",
         "<g:#228B22:#32CD32>Recommended installation</g> &7activated!",
         true
      );
      recommendedPresetClickNightbreakLogin = ConfigurationEngine.setString(
         List.of("Account token prompt sent after selecting recommended preset"),
         this.file,
         this.fileConfiguration,
         "recommendedPresetClickNightbreakLogin",
         "<g:#B8860B:#DAA520>Step 1:</g> &7Connect this server: ",
         true
      );
      recommendedPresetClickDownloadAll = ConfigurationEngine.setString(
         List.of("In-game content management message sent after selecting recommended preset"),
         this.file,
         this.fileConfiguration,
         "recommendedPresetClickDownloadAll",
         "<g:#B8860B:#DAA520>Step 2:</g> &7Install & manage content in-game with ",
         true
      );
      recommendedPresetClickDownloadAllSuffix = ConfigurationEngine.setString(
         List.of("Text after the /em setup command in the recommended preset"),
         this.file,
         this.fileConfiguration,
         "recommendedPresetClickDownloadAllSuffix",
         "&7!",
         true
      );
      recommendedPresetClickSetupGuide = ConfigurationEngine.setString(
         List.of("Browse content link sent after selecting recommended preset"),
         this.file,
         this.fileConfiguration,
         "recommendedPresetClickSetupGuide",
         "&7Browse all content: ",
         true
      );
      recommendedPresetClickDiscord = ConfigurationEngine.setString(
         List.of("Manual install note sent after selecting recommended preset"),
         this.file,
         this.fileConfiguration,
         "recommendedPresetClickDiscord",
         "&8Or install manually (less convenient): ",
         true
      );
      recommendedPresetClickWiki = ConfigurationEngine.setString(
         List.of("Discord link sent after selecting recommended preset"),
         this.file,
         this.fileConfiguration,
         "recommendedPresetClickWiki",
         "&7Need help? ",
         true
      );
      contentOnlyPresetName = ConfigurationEngine.setString(
         List.of("Display name for the content-only preset button"),
         this.file,
         this.fileConfiguration,
         "contentOnlyPresetName",
         "<g:#DAA520:#F0C040>Content-Only Preset</g>",
         true
      );
      contentOnlyPresetLore1 = ConfigurationEngine.setString(
         List.of("First lore line for the content-only preset"),
         this.file,
         this.fileConfiguration,
         "contentOnlyPresetLore1",
         "<g:#8B0000:#CC0000>Disables natural elite spawns.</g>",
         true
      );
      contentOnlyPresetLore2 = ConfigurationEngine.setString(
         List.of("Second lore line for the content-only preset"),
         this.file,
         this.fileConfiguration,
         "contentOnlyPresetLore2",
         "&7Keeps installed content and",
         true
      );
      contentOnlyPresetLore3 = ConfigurationEngine.setString(
         List.of("Third lore line for the content-only preset"),
         this.file,
         this.fileConfiguration,
         "contentOnlyPresetLore3",
         "&7the MMORPG progression system.",
         true
      );
      contentOnlyPresetLore4 = ConfigurationEngine.setString(
         List.of("Fourth lore line for the content-only preset"),
         this.file,
         this.fileConfiguration,
         "contentOnlyPresetLore4",
         "&7Works with &fBetterStructures&7.",
         true
      );
      contentOnlyPresetLore5 = ConfigurationEngine.setString(
         List.of("Fifth lore line for the content-only preset"), this.file, this.fileConfiguration, "contentOnlyPresetLore5", "&eClick to select!", true
      );
      contentOnlyPresetClickConfirm = ConfigurationEngine.setString(
         List.of("Confirmation message sent after selecting content-only preset"),
         this.file,
         this.fileConfiguration,
         "contentOnlyPresetClickConfirm",
         "<g:#DAA520:#F0C040>Content-only installation</g> &7activated!",
         true
      );
      contentOnlyPresetClickNightbreakLogin = ConfigurationEngine.setString(
         List.of("Account token prompt sent after selecting content-only preset"),
         this.file,
         this.fileConfiguration,
         "contentOnlyPresetClickNightbreakLogin",
         "<g:#B8860B:#DAA520>Step 1:</g> &7Connect this server: ",
         true
      );
      contentOnlyPresetClickDownloadAll = ConfigurationEngine.setString(
         List.of("In-game content management message sent after selecting content-only preset"),
         this.file,
         this.fileConfiguration,
         "contentOnlyPresetClickDownloadAll",
         "<g:#B8860B:#DAA520>Step 2:</g> &7Install & manage content in-game with ",
         true
      );
      contentOnlyPresetClickDownloadAllSuffix = ConfigurationEngine.setString(
         List.of("Text after the /em setup command in the content-only preset"),
         this.file,
         this.fileConfiguration,
         "contentOnlyPresetClickDownloadAllSuffix",
         "&7!",
         true
      );
      contentOnlyPresetClickSetupGuide = ConfigurationEngine.setString(
         List.of("Browse content link sent after selecting content-only preset"),
         this.file,
         this.fileConfiguration,
         "contentOnlyPresetClickSetupGuide",
         "&7Browse all content: ",
         true
      );
      contentOnlyPresetClickDiscord = ConfigurationEngine.setString(
         List.of("Manual install note sent after selecting content-only preset"),
         this.file,
         this.fileConfiguration,
         "contentOnlyPresetClickDiscord",
         "&8Or install manually (less convenient): ",
         true
      );
      contentOnlyPresetClickWiki = ConfigurationEngine.setString(
         List.of("Discord link sent after selecting content-only preset"),
         this.file,
         this.fileConfiguration,
         "contentOnlyPresetClickWiki",
         "&7Need help? ",
         true
      );
      nothingPresetName = ConfigurationEngine.setString(
         List.of("Display name for the nothing preset button"),
         this.file,
         this.fileConfiguration,
         "nothingPresetName",
         "<g:#8B0000:#CC0000>No Progression Preset</g> &8- Not Recommended",
         true
      );
      nothingPresetLore1 = ConfigurationEngine.setString(
         List.of("First lore line for the nothing preset"),
         this.file,
         this.fileConfiguration,
         "nothingPresetLore1",
         "&cDisables elite spawns, loot drops,",
         true
      );
      nothingPresetLore2 = ConfigurationEngine.setString(
         List.of("Second lore line for the nothing preset"), this.file, this.fileConfiguration, "nothingPresetLore2", "&cthe skill system, and leveling.", true
      );
      nothingPresetLore3 = ConfigurationEngine.setString(
         List.of("Third lore line for the nothing preset"),
         this.file,
         this.fileConfiguration,
         "nothingPresetLore3",
         "&4&lNot recommended! &cMost features disabled.",
         true
      );
      nothingPresetLore4 = ConfigurationEngine.setString(
         List.of("Fourth lore line for the nothing preset"),
         this.file,
         this.fileConfiguration,
         "nothingPresetLore4",
         "&7Works with &fBetterStructures&7.",
         true
      );
      nothingPresetLore5 = ConfigurationEngine.setString(
         List.of("Fifth lore line for the nothing preset"), this.file, this.fileConfiguration, "nothingPresetLore5", "&7Click to select.", true
      );
      nothingPresetLore6 = ConfigurationEngine.setString(
         List.of("Sixth lore line for the nothing preset"), this.file, this.fileConfiguration, "nothingPresetLore6", "", true
      );
      nothingPresetClickConfirm = ConfigurationEngine.setString(
         List.of("Confirmation message sent after selecting nothing preset"),
         this.file,
         this.fileConfiguration,
         "nothingPresetClickConfirm",
         "<g:#8B0000:#CC0000>No progression</g> &7mode activated.",
         true
      );
      nothingPresetClickWarning = ConfigurationEngine.setString(
         List.of("Warning message sent after selecting nothing preset"),
         this.file,
         this.fileConfiguration,
         "nothingPresetClickWarning",
         "&c&lWarning: &7Elite spawns, loot, skills, and leveling are all &cdisabled&7. Most EliteMobs features will not work.",
         true
      );
      nothingPresetClickBetterStructures = ConfigurationEngine.setString(
         List.of("BetterStructures note sent after selecting nothing preset"),
         this.file,
         this.fileConfiguration,
         "nothingPresetClickBetterStructures",
         "&7Content from &fBetterStructures &7will still function normally.",
         true
      );
      nothingPresetClickSetupGuide = ConfigurationEngine.setString(
         List.of("Setup guide link sent after selecting nothing preset"),
         this.file,
         this.fileConfiguration,
         "nothingPresetClickSetupGuide",
         "&7Need help? ",
         true
      );
      loginHeader = ConfigurationEngine.setString(
         List.of("Header for the first-time login message"),
         this.file,
         this.fileConfiguration,
         "loginHeader",
         "&fWelcome to <g:#228B22:#32CD32>EliteMobs</g>&f, by <g:#CC4400:#DAA520>MagmaGuy</g>&f!",
         true
      );
      loginWelcome = ConfigurationEngine.setString(
         List.of("Welcome message for the first-time login"),
         this.file,
         this.fileConfiguration,
         "loginWelcome",
         "<g:#2E8B57:#3CB371>Dungeons</g>&7, <g:#CD7F32:#FFD700>events</g>&7, <g:#4A7A9A:#6A9ABA>arenas</g> &7& <g:#7B2FBE:#A855F7>more</g>&7!",
         true
      );
      loginHelpHint = ConfigurationEngine.setString(
         List.of("Help hint for the first-time login"), this.file, this.fileConfiguration, "loginHelpHint", "&7Setup required! Run ", true
      );
      loginHelpHintSuffix = ConfigurationEngine.setString(
         List.of("Text after the /em initialize command in the help hint"),
         this.file,
         this.fileConfiguration,
         "loginHelpHintSuffix",
         " &7to get started.",
         true
      );
      loginDiscord = ConfigurationEngine.setString(
         List.of("Discord link for the first-time login"), this.file, this.fileConfiguration, "loginDiscord", "&7Need help? Reach out on ", true
      );
      loginDismiss = ConfigurationEngine.setString(
         List.of("Dismiss instruction for the first-time login"),
         this.file,
         this.fileConfiguration,
         "loginDismiss",
         "&8This message will stop appearing after setup.",
         true
      );
      loginLanguagePrefix = ConfigurationEngine.setString(
         List.of("Prefix label before the language selector in the first-time login message"),
         this.file,
         this.fileConfiguration,
         "loginLanguagePrefix",
         "&7Language: ",
         true
      );
      discordLinkDisplay = ConfigurationEngine.setString(
         List.of("Display text for the clickable Discord link"), this.file, this.fileConfiguration, "discordLinkDisplay", "&9&nDiscord", true
      );
      discordLinkHover = ConfigurationEngine.setString(
         List.of("Hover tooltip for the Discord link"), this.file, this.fileConfiguration, "discordLinkHover", "&7Click to join the Discord!", true
      );
      accountLinkDisplay = ConfigurationEngine.setString(
         List.of("Display text for the clickable account token link"),
         this.file,
         this.fileConfiguration,
         "accountLinkDisplay",
         "&9&nnightbreak.io/account",
         true
      );
      accountLinkHover = ConfigurationEngine.setString(
         List.of("Hover tooltip for the account token link"),
         this.file,
         this.fileConfiguration,
         "accountLinkHover",
         "&7Click to open the account token page",
         true
      );
      contentLinkDisplay = ConfigurationEngine.setString(
         List.of("Display text for the clickable content browse link"),
         this.file,
         this.fileConfiguration,
         "contentLinkDisplay",
         "&9&nnightbreak.io/elitemobs",
         true
      );
      contentLinkHover = ConfigurationEngine.setString(
         List.of("Hover tooltip for the content browse link"),
         this.file,
         this.fileConfiguration,
         "contentLinkHover",
         "&7Click to browse EliteMobs content",
         true
      );
      wikiLinkDisplay = ConfigurationEngine.setString(
         List.of("Display text for the clickable wiki link"), this.file, this.fileConfiguration, "wikiLinkDisplay", "&8&nwiki.nightbreak.io", true
      );
      wikiLinkHover = ConfigurationEngine.setString(
         List.of("Hover tooltip for the wiki link"), this.file, this.fileConfiguration, "wikiLinkHover", "&7Click to open the setup wiki", true
      );
      emInitializeDisplay = ConfigurationEngine.setString(
         List.of("Display text for the clickable /em initialize command"), this.file, this.fileConfiguration, "emInitializeDisplay", "&a&n/em initialize", true
      );
      emInitializeHover = ConfigurationEngine.setString(
         List.of("Hover tooltip for the /em initialize command"), this.file, this.fileConfiguration, "emInitializeHover", "&7Click to run /em initialize", true
      );
      emSetupDisplay = ConfigurationEngine.setString(
         List.of("Display text for the clickable /em setup command"), this.file, this.fileConfiguration, "emSetupDisplay", "&a&n/em setup", true
      );
      emSetupHover = ConfigurationEngine.setString(
         List.of("Hover tooltip for the /em setup command"), this.file, this.fileConfiguration, "emSetupHover", "&7Click to run /em setup", true
      );
      emCommandDisplay = ConfigurationEngine.setString(
         List.of("Display text for the clickable /em command"), this.file, this.fileConfiguration, "emCommandDisplay", "&a&n/em", true
      );
      emCommandHover = ConfigurationEngine.setString(
         List.of("Hover tooltip for the /em command"), this.file, this.fileConfiguration, "emCommandHover", "&7Click to open the EliteMobs menu", true
      );
      emLootDisplay = ConfigurationEngine.setString(
         List.of("Display text for the clickable /em loot command"), this.file, this.fileConfiguration, "emLootDisplay", "&a&n/em loot", true
      );
      emLootHover = ConfigurationEngine.setString(
         List.of("Hover tooltip for the /em loot command"), this.file, this.fileConfiguration, "emLootHover", "&7Click to vote on loot", true
      );
   }

   @Generated
   public static String getMenuTitle() {
      return menuTitle;
   }

   @Generated
   public static String getMenuSubtitle() {
      return menuSubtitle;
   }

   @Generated
   public static String getSeparatorLine() {
      return separatorLine;
   }

   @Generated
   public static String getInfoButtonName() {
      return infoButtonName;
   }

   @Generated
   public static String getInfoButtonLore1() {
      return infoButtonLore1;
   }

   @Generated
   public static String getInfoButtonLore2() {
      return infoButtonLore2;
   }

   @Generated
   public static String getInfoButtonLore3() {
      return infoButtonLore3;
   }

   @Generated
   public static String getInfoButtonClickSetupLink() {
      return infoButtonClickSetupLink;
   }

   @Generated
   public static String getInfoButtonClickContentBrowse() {
      return infoButtonClickContentBrowse;
   }

   @Generated
   public static String getInfoButtonClickDiscord() {
      return infoButtonClickDiscord;
   }

   @Generated
   public static String getRecommendedPresetName() {
      return recommendedPresetName;
   }

   @Generated
   public static String getRecommendedPresetLore1() {
      return recommendedPresetLore1;
   }

   @Generated
   public static String getRecommendedPresetLore2() {
      return recommendedPresetLore2;
   }

   @Generated
   public static String getRecommendedPresetLore3() {
      return recommendedPresetLore3;
   }

   @Generated
   public static String getRecommendedPresetLore4() {
      return recommendedPresetLore4;
   }

   @Generated
   public static String getRecommendedPresetLore5() {
      return recommendedPresetLore5;
   }

   @Generated
   public static String getRecommendedPresetClickConfirm() {
      return recommendedPresetClickConfirm;
   }

   @Generated
   public static String getRecommendedPresetClickNightbreakLogin() {
      return recommendedPresetClickNightbreakLogin;
   }

   @Generated
   public static String getRecommendedPresetClickDownloadAll() {
      return recommendedPresetClickDownloadAll;
   }

   @Generated
   public static String getRecommendedPresetClickSetupGuide() {
      return recommendedPresetClickSetupGuide;
   }

   @Generated
   public static String getRecommendedPresetClickDiscord() {
      return recommendedPresetClickDiscord;
   }

   @Generated
   public static String getRecommendedPresetClickWiki() {
      return recommendedPresetClickWiki;
   }

   @Generated
   public static String getContentOnlyPresetName() {
      return contentOnlyPresetName;
   }

   @Generated
   public static String getContentOnlyPresetLore1() {
      return contentOnlyPresetLore1;
   }

   @Generated
   public static String getContentOnlyPresetLore2() {
      return contentOnlyPresetLore2;
   }

   @Generated
   public static String getContentOnlyPresetLore3() {
      return contentOnlyPresetLore3;
   }

   @Generated
   public static String getContentOnlyPresetLore4() {
      return contentOnlyPresetLore4;
   }

   @Generated
   public static String getContentOnlyPresetLore5() {
      return contentOnlyPresetLore5;
   }

   @Generated
   public static String getContentOnlyPresetClickConfirm() {
      return contentOnlyPresetClickConfirm;
   }

   @Generated
   public static String getContentOnlyPresetClickNightbreakLogin() {
      return contentOnlyPresetClickNightbreakLogin;
   }

   @Generated
   public static String getContentOnlyPresetClickDownloadAll() {
      return contentOnlyPresetClickDownloadAll;
   }

   @Generated
   public static String getContentOnlyPresetClickSetupGuide() {
      return contentOnlyPresetClickSetupGuide;
   }

   @Generated
   public static String getContentOnlyPresetClickDiscord() {
      return contentOnlyPresetClickDiscord;
   }

   @Generated
   public static String getContentOnlyPresetClickWiki() {
      return contentOnlyPresetClickWiki;
   }

   @Generated
   public static String getNothingPresetName() {
      return nothingPresetName;
   }

   @Generated
   public static String getNothingPresetLore1() {
      return nothingPresetLore1;
   }

   @Generated
   public static String getNothingPresetLore2() {
      return nothingPresetLore2;
   }

   @Generated
   public static String getNothingPresetLore3() {
      return nothingPresetLore3;
   }

   @Generated
   public static String getNothingPresetLore4() {
      return nothingPresetLore4;
   }

   @Generated
   public static String getNothingPresetLore5() {
      return nothingPresetLore5;
   }

   @Generated
   public static String getNothingPresetLore6() {
      return nothingPresetLore6;
   }

   @Generated
   public static String getNothingPresetClickConfirm() {
      return nothingPresetClickConfirm;
   }

   @Generated
   public static String getNothingPresetClickWarning() {
      return nothingPresetClickWarning;
   }

   @Generated
   public static String getNothingPresetClickBetterStructures() {
      return nothingPresetClickBetterStructures;
   }

   @Generated
   public static String getNothingPresetClickSetupGuide() {
      return nothingPresetClickSetupGuide;
   }

   @Generated
   public static String getLoginHeader() {
      return loginHeader;
   }

   @Generated
   public static String getLoginWelcome() {
      return loginWelcome;
   }

   @Generated
   public static String getLoginHelpHint() {
      return loginHelpHint;
   }

   @Generated
   public static String getLoginDiscord() {
      return loginDiscord;
   }

   @Generated
   public static String getLoginDismiss() {
      return loginDismiss;
   }

   @Generated
   public static String getLoginLanguagePrefix() {
      return loginLanguagePrefix;
   }

   @Generated
   public static String getDiscordLinkDisplay() {
      return discordLinkDisplay;
   }

   @Generated
   public static String getDiscordLinkHover() {
      return discordLinkHover;
   }

   @Generated
   public static String getAccountLinkDisplay() {
      return accountLinkDisplay;
   }

   @Generated
   public static String getAccountLinkHover() {
      return accountLinkHover;
   }

   @Generated
   public static String getContentLinkDisplay() {
      return contentLinkDisplay;
   }

   @Generated
   public static String getContentLinkHover() {
      return contentLinkHover;
   }

   @Generated
   public static String getWikiLinkDisplay() {
      return wikiLinkDisplay;
   }

   @Generated
   public static String getWikiLinkHover() {
      return wikiLinkHover;
   }

   @Generated
   public static String getEmInitializeDisplay() {
      return emInitializeDisplay;
   }

   @Generated
   public static String getEmInitializeHover() {
      return emInitializeHover;
   }

   @Generated
   public static String getEmSetupDisplay() {
      return emSetupDisplay;
   }

   @Generated
   public static String getEmSetupHover() {
      return emSetupHover;
   }

   @Generated
   public static String getEmCommandDisplay() {
      return emCommandDisplay;
   }

   @Generated
   public static String getEmCommandHover() {
      return emCommandHover;
   }

   @Generated
   public static String getEmLootDisplay() {
      return emLootDisplay;
   }

   @Generated
   public static String getEmLootHover() {
      return emLootHover;
   }

   @Generated
   public static String getLoginHelpHintSuffix() {
      return loginHelpHintSuffix;
   }

   @Generated
   public static String getInfoButtonClickContentBrowseSuffix() {
      return infoButtonClickContentBrowseSuffix;
   }

   @Generated
   public static String getRecommendedPresetClickDownloadAllSuffix() {
      return recommendedPresetClickDownloadAllSuffix;
   }

   @Generated
   public static String getContentOnlyPresetClickDownloadAllSuffix() {
      return contentOnlyPresetClickDownloadAllSuffix;
   }
}
