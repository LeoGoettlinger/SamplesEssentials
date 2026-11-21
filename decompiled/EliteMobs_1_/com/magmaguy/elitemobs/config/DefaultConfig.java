package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.commands.ReloadCommand;
import com.magmaguy.elitemobs.config.translations.TranslationsConfig;
import com.magmaguy.elitemobs.magmacore.config.ConfigurationFile;
import com.magmaguy.elitemobs.magmacore.nightbreak.NightbreakPluginUpdater;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import java.util.List;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

public class DefaultConfig extends ConfigurationFile {
   private static boolean useResourcePackEvenIfResourcePackManagerIsNotInstalled;
   public static DefaultConfig instance;
   private static boolean alwaysShowNametags;
   private static boolean usePassengerCombatLevelDisplay;
   private static boolean preventEliteMobConversionOfNamedMobs;
   private static boolean doStrictSpawningRules;
   private static boolean emLeadsToStatusMenu;
   private static boolean otherCommandsLeadToEMStatusMenu;
   private static boolean setupDone;
   private static Location defaultSpawnLocation;
   private static boolean doExplosionRegen;
   private static boolean preventVanillaReinforcementsForEliteEntities;
   private static boolean doRegenerateContainers;
   private static int defaultTransitiveBlockLimiter;
   private static int notificationThrottleSeconds;
   private static boolean onlyUseBedrockMenus;
   private static int characterLimitForBookMenuPages;
   private static boolean useGlassToFillMenuEmptySpace;
   private static String language;
   private static String trackMessage;
   private static String chestCooldownMessage;
   private static String dismissEMMessage;
   private static String switchEMStyleMessage;
   private static String treasureChestNoDropMessage;
   private static String bossAlreadyGoneMessage;
   private static boolean resetPlayerScaleOnLogin;
   private static String enchantmentChallengeFailureMessage;
   private static String enchantmentChallengeSuccessMessage;
   private static String enchantmentChallengeInventoryFullMessage;
   private static String enchantmentChallengeConsequencesMessage;
   private static String enchantmentChallengeCriticalFailureMessage;
   private static String enchantmentChallengeStartMessage;
   private static boolean useRandomizedScalingForElites;
   private static double bedrockNPCRoleYOffset;
   private static boolean autoDownloadPluginUpdates;

   public static boolean useResourcePackModels() {
      return useResourcePackEvenIfResourcePackManagerIsNotInstalled || Bukkit.getPluginManager().isPluginEnabled("ResourcePackManager");
   }

   public DefaultConfig() {
      super("config.yml");
      instance = this;
   }

   public static void toggleSetupDone() {
      setupDone = !setupDone;
      ConfigurationEngine.writeValue(setupDone, instance.file, instance.getFileConfiguration(), "setupDoneV5");
   }

   public static void toggleSetupDone(boolean value) {
      setupDone = value;
      ConfigurationEngine.writeValue(setupDone, instance.file, instance.getFileConfiguration(), "setupDoneV5");
   }

   public static void save() {
      ConfigurationEngine.fileSaverOnlyDefaults(instance.fileConfiguration, instance.file);
   }

   public static void setLanguage(CommandSender commandSender, String filename) {
      language = filename;
      instance.fileConfiguration.set("language", filename);
      ConfigurationEngine.fileSaverCustomValues(instance.fileConfiguration, instance.file);
      ReloadCommand.reload(commandSender);
   }

   @Override
   public void initializeValues() {
      language = ConfigurationEngine.setString(
         List.of(
            "Sets the language file used by EliteMobs",
            "Do NOT change this manually! It is meant to be installed with the command '/em language <languagefile>'"
         ),
         this.file,
         this.fileConfiguration,
         "language",
         "english",
         false
      );
      new TranslationsConfig();
      alwaysShowNametags = ConfigurationEngine.setBoolean(
         List.of("Sets whether elites and bosses spawned by elitemobs will always show their nametags.", "Not recommended!"),
         this.fileConfiguration,
         "alwaysShowEliteMobNameTags",
         false
      );
      boolean followingTextDefaultApplied = ConfigurationEngine.setBoolean(
         List.of("Internal marker for the one-time switch of combat labels to following text. Do not edit."),
         this.fileConfiguration,
         "combatLabelFollowingTextDefaultApplied",
         false
      );
      if (!followingTextDefaultApplied) {
         this.fileConfiguration.set("usePassengerCombatLevelDisplay", false);
         this.fileConfiguration.set("combatLabelFollowingTextDefaultApplied", true);
      }

      usePassengerCombatLevelDisplay = ConfigurationEngine.setBoolean(
         List.of(
            "Mounts player combat labels as packet-only passengers instead of following text.",
            "Minecraft then hides the player's own name from other players unless the player is on a scoreboard team.",
            "Following text moves with the same client smoothing as the player. Passenger mode still falls back to it while another entity rides the player."
         ),
         this.fileConfiguration,
         "usePassengerCombatLevelDisplay",
         false
      );
      preventEliteMobConversionOfNamedMobs = ConfigurationEngine.setBoolean(
         List.of("Sets whether EliteMobs will prevent converting named mobs to elites.", "Especially important for compatibility with other plugins."),
         this.fileConfiguration,
         "preventEliteMobConversionOfNamedMobs",
         true
      );
      doStrictSpawningRules = ConfigurationEngine.setBoolean(
         List.of(
            "Sets if EliteMobs will convert mobs with custom spawn reasons.",
            "Running certain boss plugins will automatically set this to true, no matter what the value in the config is."
         ),
         this.fileConfiguration,
         "enableHighCompatibilityMode",
         false
      );
      if (Bukkit.getPluginManager().isPluginEnabled("MythicMobs") || Bukkit.getPluginManager().isPluginEnabled("LevelledMobs")) {
         Logger.info("Other boss mob plugins have been detected, high compatibility mode will be used!");
         doStrictSpawningRules = true;
      }

      emLeadsToStatusMenu = ConfigurationEngine.setBoolean(
         List.of("Sets if the centralized /em command opens the main status page of the plugin.", "Highly recommended!"),
         this.fileConfiguration,
         "emLeadsToStatusMenu",
         true
      );
      otherCommandsLeadToEMStatusMenu = ConfigurationEngine.setBoolean(
         List.of("Sets if running specific commands like /em wallet will lead to the /em menu where that information is centralized."),
         this.fileConfiguration,
         "otherCommandsLeadToEMStatusMenu",
         true
      );
      setupDone = ConfigurationEngine.setBoolean(
         List.of("Sets if the setup is complete.", "Do not set this value manually, it is meant to be modified through in-game commands."),
         this.fileConfiguration,
         "setupDoneV5",
         false
      );
      preventVanillaReinforcementsForEliteEntities = ConfigurationEngine.setBoolean(
         List.of("Sets if elites will prevent spawning vanilla reinforcements, such as for the Zombie reinforcement feature."),
         this.fileConfiguration,
         "preventVanillaReinforcementsForEliteEntities",
         true
      );

      try {
         defaultSpawnLocation = ConfigurationLocation.serialize(
            ConfigurationEngine.setString(
               List.of("Sets the default spawn location of the server for EliteMobs. /em spawntp will lead to this location."),
               this.file,
               this.fileConfiguration,
               "defaultSpawnLocation",
               ConfigurationLocation.deserialize(((World)Bukkit.getWorlds().get(0)).getSpawnLocation()),
               false
            )
         );
      } catch (Exception var3) {
         Logger.warn("There is an issue with your defaultSpawnLocation in the config.yml configuration file! Fix it!");
      }

      doExplosionRegen = ConfigurationEngine.setBoolean(
         List.of("Sets if EliteMobs will regenerate blocks blown up by elites."), this.fileConfiguration, "doExplosionRegen", true
      );
      doRegenerateContainers = ConfigurationEngine.setBoolean(
         List.of(
            "Sets if the explosion regen will also regenerate the contents of containers such as chests.",
            "Turning it to false will make elite explosions not blow up containers."
         ),
         this.fileConfiguration,
         "doRegenerateContainers",
         true
      );
      defaultTransitiveBlockLimiter = ConfigurationEngine.setInt(this.fileConfiguration, "defaultTransitiveBlockLimiter", 500);
      notificationThrottleSeconds = Math.max(1, ConfigurationEngine.setInt(this.fileConfiguration, "notificationThrottleSeconds", 10));
      onlyUseBedrockMenus = ConfigurationEngine.setBoolean(
         List.of(
            "Sets whether the /em menu will only use the inventory-based menu style which is compatible with bedrock.",
            "As a reminder, players can otherwise do the command /em alt to switch between /em menu styles"
         ),
         this.fileConfiguration,
         "onlyUseBedrockMenus",
         false
      );
      characterLimitForBookMenuPages = ConfigurationEngine.setInt(
         List.of("Sets the character limit per line for book menu pages.", "Lower this amount if text is getting cut off in book menus such as for quests"),
         this.fileConfiguration,
         "characterLimitForBookMenuPagesV2",
         170
      );
      useGlassToFillMenuEmptySpace = ConfigurationEngine.setBoolean(
         List.of("Sets if empty menu space will be filled with glass panes.", "Not recommended if you are using the EliteMobs resource pack."),
         this.fileConfiguration,
         "useGlassToFillMenuEmptySpace",
         false
      );
      trackMessage = ConfigurationEngine.setString(
         List.of("Sets the tracking message for bosses that send tracking messages."),
         this.file,
         this.fileConfiguration,
         "trackMessage",
         "Track the $name",
         true
      );
      chestCooldownMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent to players that try to reopen a treasure chest they have already opened."),
         this.file,
         this.fileConfiguration,
         "chestCooldownMessage",
         "&7[EM] &cYou've already opened this chest recently! Wait $time!",
         true
      );
      dismissEMMessage = ConfigurationEngine.setString(
         List.of("Sets the message that appears recommending the /em alt command for players having issues with the /em command"),
         this.file,
         this.fileConfiguration,
         "dismissEMMessage",
         "&8[EliteMobs] &2/elitemobs &fmenu not working for you? Try &2/elitemobs alt &fto see an alternative version of the menu! &cDon't want to see this message again? &4/em dismiss",
         true
      );
      switchEMStyleMessage = ConfigurationEngine.setString(
         List.of("Sets the message that appears when players run the /em alt command."),
         this.file,
         this.fileConfiguration,
         "switchEMStyleMessage",
         "&8[EliteMobs] &2/elitemobs &fmenu style changed! Check it out!",
         true
      );
      treasureChestNoDropMessage = ConfigurationEngine.setString(
         List.of("Sets the message that appears when a player opens a treasure chest but gets nothing"),
         this.file,
         this.fileConfiguration,
         "treasureChestNoDropMessage",
         "&8[EliteMobs] &cYou didn't get anything! Better luck next time!",
         true
      );
      bossAlreadyGoneMessage = ConfigurationEngine.setString(
         List.of("Sets the message that appears when a player tries to track a boss that is no longer valid"),
         this.file,
         this.fileConfiguration,
         "bossAlreadyGoneMessage",
         "&c[EliteMobs] Sorry, this boss is already gone!",
         true
      );
      resetPlayerScaleOnLogin = ConfigurationEngine.setBoolean(
         List.of(
            "Sets whether to reset player scale (literally, the player size on login).",
            "This is important because some elite powers can modify it and if the server crashes players will be stuck to whatever scale was set when the server crashed, unless this option is set to true."
         ),
         this.fileConfiguration,
         "resetPlayerScale",
         true
      );
      enchantmentChallengeFailureMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent to players when they fail an enchantment challenge"),
         this.file,
         this.fileConfiguration,
         "enchantmentChallengeFailureMessage",
         "&8[EliteMobs] &cFailed enchantment! The enchantment did not bind to the item.",
         true
      );
      enchantmentChallengeSuccessMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent to players when they succeed an enchantment challenge"),
         this.file,
         this.fileConfiguration,
         "enchantmentChallengeSuccessMessage",
         "&8[EliteMobs] &2Successful enchantment!",
         true
      );
      enchantmentChallengeInventoryFullMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent to players when their inventory is full during an enchantment challenge"),
         this.file,
         this.fileConfiguration,
         "enchantmentChallengeInventoryFullMessage",
         "&8[EliteMobs] &cYour inventory was full so the item you were trying to upgrade has been deleted! It will be restored if your item is not full by the time you leave the instanced dungeon.",
         true
      );
      enchantmentChallengeConsequencesMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent to players about the consequences of an enchantment challenge"),
         this.file,
         this.fileConfiguration,
         "enchantmentChallengeConsequencesMessage",
         "&cThere's a 10% chance of losing your item if you lose the fight! Leaving the arena counts as losing.",
         true
      );
      enchantmentChallengeCriticalFailureMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent to players when they critically fail an enchantment challenge"),
         this.file,
         this.fileConfiguration,
         "enchantmentChallengeCriticalFailureMessage",
         "&8[EliteMobs] &4Critical failure! You lost the item!",
         true
      );
      enchantmentChallengeStartMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent to players at the start of an enchantment challenge"),
         this.file,
         this.fileConfiguration,
         "enchantmentChallengeStartMessage",
         "&8[EliteMobs] &6Challenge! Defeat the boss to get your upgraded item!",
         true
      );
      useResourcePackEvenIfResourcePackManagerIsNotInstalled = ConfigurationEngine.setBoolean(
         List.of("Enables the use of resource packs even if the resource pack manager is not installed"),
         this.fileConfiguration,
         "useResourcePackEvenIfResourcePackManagerIsNotInstalled",
         false
      );
      useRandomizedScalingForElites = ConfigurationEngine.setBoolean(
         List.of(
            "Sets whether the scale (size) of the elites will be slightly randomized, for variety.",
            "May mess with spawners, which may or may not be a good thing (I think it is >:] )"
         ),
         this.fileConfiguration,
         "useRandomizedScalingForElites",
         false
      );
      bedrockNPCRoleYOffset = ConfigurationEngine.setDouble(
         List.of(
            "Height (in blocks above the NPC's feet) of the role tag shown to Bedrock (Geyser) players.",
            "Bedrock clients can't render the Java TextDisplay role tag, so Bedrock players are instead shown",
            "an armor-stand name at this height, meant to sit just above the disguised NPC's name.",
            "Java players are unaffected (they keep the normal role tag). Tune this with a Bedrock client.",
            "Floodgate or Geyser must also run on this backend so EliteMobs can identify Bedrock players.",
            "A proxy-only install without a backend identity API cannot provide this fallback."
         ),
         this.fileConfiguration,
         "bedrockNPCRoleYOffset",
         2.2
      );
      autoDownloadPluginUpdates = NightbreakPluginUpdater.setAutoDownloadConfigDefault(this.fileConfiguration);
   }

   @Generated
   public static boolean isUseResourcePackEvenIfResourcePackManagerIsNotInstalled() {
      return useResourcePackEvenIfResourcePackManagerIsNotInstalled;
   }

   @Generated
   public static DefaultConfig getInstance() {
      return instance;
   }

   @Generated
   public static boolean isAlwaysShowNametags() {
      return alwaysShowNametags;
   }

   @Generated
   public static boolean isUsePassengerCombatLevelDisplay() {
      return usePassengerCombatLevelDisplay;
   }

   @Generated
   public static boolean isPreventEliteMobConversionOfNamedMobs() {
      return preventEliteMobConversionOfNamedMobs;
   }

   @Generated
   public static boolean isDoStrictSpawningRules() {
      return doStrictSpawningRules;
   }

   @Generated
   public static boolean isEmLeadsToStatusMenu() {
      return emLeadsToStatusMenu;
   }

   @Generated
   public static boolean isOtherCommandsLeadToEMStatusMenu() {
      return otherCommandsLeadToEMStatusMenu;
   }

   @Generated
   public static boolean isSetupDone() {
      return setupDone;
   }

   @Generated
   public static Location getDefaultSpawnLocation() {
      return defaultSpawnLocation;
   }

   @Generated
   public static boolean isDoExplosionRegen() {
      return doExplosionRegen;
   }

   @Generated
   public static boolean isPreventVanillaReinforcementsForEliteEntities() {
      return preventVanillaReinforcementsForEliteEntities;
   }

   @Generated
   public static boolean isDoRegenerateContainers() {
      return doRegenerateContainers;
   }

   @Generated
   public static int getDefaultTransitiveBlockLimiter() {
      return defaultTransitiveBlockLimiter;
   }

   @Generated
   public static int getNotificationThrottleSeconds() {
      return notificationThrottleSeconds;
   }

   @Generated
   public static boolean isOnlyUseBedrockMenus() {
      return onlyUseBedrockMenus;
   }

   @Generated
   public static int getCharacterLimitForBookMenuPages() {
      return characterLimitForBookMenuPages;
   }

   @Generated
   public static boolean isUseGlassToFillMenuEmptySpace() {
      return useGlassToFillMenuEmptySpace;
   }

   @Generated
   public static String getLanguage() {
      return language;
   }

   @Generated
   public static String getTrackMessage() {
      return trackMessage;
   }

   @Generated
   public static String getChestCooldownMessage() {
      return chestCooldownMessage;
   }

   @Generated
   public static String getDismissEMMessage() {
      return dismissEMMessage;
   }

   @Generated
   public static String getSwitchEMStyleMessage() {
      return switchEMStyleMessage;
   }

   @Generated
   public static String getTreasureChestNoDropMessage() {
      return treasureChestNoDropMessage;
   }

   @Generated
   public static String getBossAlreadyGoneMessage() {
      return bossAlreadyGoneMessage;
   }

   @Generated
   public static boolean isResetPlayerScaleOnLogin() {
      return resetPlayerScaleOnLogin;
   }

   @Generated
   public static String getEnchantmentChallengeFailureMessage() {
      return enchantmentChallengeFailureMessage;
   }

   @Generated
   public static String getEnchantmentChallengeSuccessMessage() {
      return enchantmentChallengeSuccessMessage;
   }

   @Generated
   public static String getEnchantmentChallengeInventoryFullMessage() {
      return enchantmentChallengeInventoryFullMessage;
   }

   @Generated
   public static String getEnchantmentChallengeConsequencesMessage() {
      return enchantmentChallengeConsequencesMessage;
   }

   @Generated
   public static String getEnchantmentChallengeCriticalFailureMessage() {
      return enchantmentChallengeCriticalFailureMessage;
   }

   @Generated
   public static String getEnchantmentChallengeStartMessage() {
      return enchantmentChallengeStartMessage;
   }

   @Generated
   public static boolean isUseRandomizedScalingForElites() {
      return useRandomizedScalingForElites;
   }

   @Generated
   public static double getBedrockNPCRoleYOffset() {
      return bedrockNPCRoleYOffset;
   }

   @Generated
   public static boolean isAutoDownloadPluginUpdates() {
      return autoDownloadPluginUpdates;
   }
}
