package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.magmacore.config.ConfigurationFile;
import java.util.List;
import lombok.Generated;

public class WormholesConfig extends ConfigurationFile {
   private static String dungeonNotInstalledMessage;
   private static String defaultPortalMissingMessage;
   private static boolean reducedParticlesMode;
   private static boolean noParticlesMode;
   private static String insufficientCurrencyForWormholeMessage;

   public WormholesConfig() {
      super("Wormholes.yml");
   }

   @Override
   public void initializeValues() {
      dungeonNotInstalledMessage = ConfigurationEngine.setString(
         List.of("Sets the message that appears when a teleport is used for a dungeon that is not installed."),
         this.file,
         this.fileConfiguration,
         "dungeonNotInstalledMessage",
         "&8[EliteMobs] &cDungeon $dungeonID &cis not installed! This teleport will not work.",
         true
      );
      defaultPortalMissingMessage = ConfigurationEngine.setString(
         List.of("Sets the message that appears when a wormhole is used for a dungeon that is not installed."),
         this.file,
         this.fileConfiguration,
         "defaultPortalMissingMessage",
         "&8[EliteMobs] &cThis portal doesn't seem to lead anywhere!",
         true
      );
      reducedParticlesMode = ConfigurationEngine.setBoolean(
         List.of("Sets if the reduced particles mode for wormholes is used. This is especially recommended if you are allowing bedrock clients in."),
         this.fileConfiguration,
         "reducedParticlesMode",
         true
      );
      noParticlesMode = ConfigurationEngine.setBoolean(
         List.of("Sets if wormholes don't use particles at all. Not recommended, but might be necessary for really bad bedrock clients."),
         this.fileConfiguration,
         "noParticlesMode",
         false
      );
      insufficientCurrencyForWormholeMessage = ConfigurationEngine.setString(
         List.of("Sets the message that is sent when a player tries to use a wormhole but does not have enough currency to use it."),
         this.file,
         this.fileConfiguration,
         "insufficientCurrencyForWormholeMessage",
         "&8[EliteMobs] &cInsufficient currency! You need $amount to use this wormhole!",
         true
      );
   }

   @Generated
   public static String getDungeonNotInstalledMessage() {
      return dungeonNotInstalledMessage;
   }

   @Generated
   public static String getDefaultPortalMissingMessage() {
      return defaultPortalMissingMessage;
   }

   @Generated
   public static boolean isReducedParticlesMode() {
      return reducedParticlesMode;
   }

   @Generated
   public static boolean isNoParticlesMode() {
      return noParticlesMode;
   }

   @Generated
   public static String getInsufficientCurrencyForWormholeMessage() {
      return insufficientCurrencyForWormholeMessage;
   }
}
