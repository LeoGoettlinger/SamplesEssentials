package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.magmacore.config.ConfigurationFile;
import java.util.List;
import lombok.Generated;

public class CombatTagConfig extends ConfigurationFile {
   private static boolean enableCombatTag;
   private static String combatTagMessage;
   private static boolean enableTeleportTimer;
   private static String teleportTimeLeft;
   private static String teleportCancelled;
   private static boolean useActionBarMessagesInsteadOfChat;
   private static boolean preventFlyToggleInDungeons;

   public CombatTagConfig() {
      super("CombatTag.yml");
   }

   @Override
   public void initializeValues() {
      enableCombatTag = ConfigurationEngine.setBoolean(
         List.of("Whether the combat tag is enabled. Flying players who enter combat are forced to stop flying."),
         this.fileConfiguration,
         "enableCombatTag",
         true
      );
      combatTagMessage = ConfigurationEngine.setString(
         List.of("Message shown when a player's combat tag activates."),
         this.file,
         this.fileConfiguration,
         "combatTagMessage",
         "&c[EliteMobs] Combat tag activated!",
         true
      );
      enableTeleportTimer = ConfigurationEngine.setBoolean(
         List.of("Whether the /ag teleport has a countdown timer before teleporting."), this.fileConfiguration, "enableAdventurersGuildTeleportTimer", true
      );
      teleportTimeLeft = ConfigurationEngine.setString(
         List.of("Action bar message shown during the teleport countdown.", "Placeholder: $time"),
         this.file,
         this.fileConfiguration,
         "teleportTimeLeft",
         "&7[EM] Teleporting in &a$time &7seconds...",
         true
      );
      teleportCancelled = ConfigurationEngine.setString(
         List.of("Message shown when a player moves and cancels a pending teleport."),
         this.file,
         this.fileConfiguration,
         "teleportCancelled",
         "&7[EM] &cTeleport interrupted!",
         true
      );
      useActionBarMessagesInsteadOfChat = ConfigurationEngine.setBoolean(
         List.of("Whether to show the teleport countdown in the action bar instead of chat."),
         this.fileConfiguration,
         "useActionBarMessagesInsteadOfChat",
         true
      );
      preventFlyToggleInDungeons = ConfigurationEngine.setBoolean(
         List.of("Whether non-OP players are prevented from toggling flight in EliteMobs worlds."), this.fileConfiguration, "preventFlyToggleInDungeons", true
      );
   }

   @Generated
   public static boolean isEnableCombatTag() {
      return enableCombatTag;
   }

   @Generated
   public static String getCombatTagMessage() {
      return combatTagMessage;
   }

   @Generated
   public static boolean isEnableTeleportTimer() {
      return enableTeleportTimer;
   }

   @Generated
   public static String getTeleportTimeLeft() {
      return teleportTimeLeft;
   }

   @Generated
   public static String getTeleportCancelled() {
      return teleportCancelled;
   }

   @Generated
   public static boolean isUseActionBarMessagesInsteadOfChat() {
      return useActionBarMessagesInsteadOfChat;
   }

   @Generated
   public static boolean isPreventFlyToggleInDungeons() {
      return preventFlyToggleInDungeons;
   }
}
