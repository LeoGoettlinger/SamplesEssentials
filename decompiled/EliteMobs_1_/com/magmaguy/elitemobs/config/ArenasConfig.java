package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.magmacore.config.ConfigurationFile;
import java.util.List;
import lombok.Generated;

public class ArenasConfig extends ConfigurationFile {
   private static String notEnoughPlayersMessage;
   private static String startingTitle;
   private static String startingSubtitle;
   private static String arenaFullMessage;
   private static String arenasOngoingMessage;
   private static String arenaStartHintMessage;
   private static String arenaQuitHintMessage;
   private static String arenaJoinPlayerMessage;
   private static String arenaJoinSpectatorMessage;
   private static String waveTitle;
   private static String waveSubtitle;
   private static String victoryTitle;
   private static String victorySubtitle;
   private static String defeatTitle;
   private static String defeatSubtitle;
   private static String victoryBroadcast;
   private static String joinPlayerTitle;
   private static String joinPlayerSubtitle;
   private static String joinSpectatorTitle;
   private static String joinSpectatorSubtitle;
   private static String noArenaPermissionMessage;

   public ArenasConfig() {
      super("Arenas.yml");
   }

   @Override
   public void initializeValues() {
      notEnoughPlayersMessage = ConfigurationEngine.setString(
         List.of("Message that appears when not enough players are present to start an arena."),
         this.file,
         this.fileConfiguration,
         "notEnoughPlayersMessage",
         "&8[EliteMobs] &cYou need at least $amount to start the match!",
         true
      );
      startingTitle = ConfigurationEngine.setString(
         List.of("Title that appears when an arena is starting."), this.file, this.fileConfiguration, "startingMessage", "&2Starting!", true
      );
      startingSubtitle = ConfigurationEngine.setString(
         List.of("Subtitle that appears when an arena is starting."), this.file, this.fileConfiguration, "startingSubtitle", "&2in $count...", true
      );
      arenaFullMessage = ConfigurationEngine.setString(
         List.of("Message that appears when an arena is full."),
         this.file,
         this.fileConfiguration,
         "arenaFullMessage",
         "&4[EliteMobs] &cArena is full! You can spectate instead while you wait for it to finish!",
         true
      );
      arenasOngoingMessage = ConfigurationEngine.setString(
         List.of("Message that appears when a player attempts to join an active arena."),
         this.file,
         this.fileConfiguration,
         "arenasOngoingMessage",
         "&4[EliteMobs] &cCan't join the arena now - a match is currently happening! You can spectate instead while you wait for it to finish!",
         true
      );
      arenaStartHintMessage = ConfigurationEngine.setString(
         List.of("Message that appears to remind players how to start an arena after joining as a player."),
         this.file,
         this.fileConfiguration,
         "instanceStartHintMessage",
         "&2[EliteMobs] &aYou can start the instance by doing &2/em start",
         true
      );
      arenaQuitHintMessage = ConfigurationEngine.setString(
         List.of("Message that appears to remind players how to leave an arena."),
         this.file,
         this.fileConfiguration,
         "instanceQuitHintMessage",
         "&4[EliteMobs] &cYou can leave the instance by doing &4/em quit",
         true
      );
      arenaJoinPlayerMessage = ConfigurationEngine.setString(
         List.of("Message that appears when players join an arena."),
         this.file,
         this.fileConfiguration,
         "arenaJoinPlayerMessage",
         "&2[EliteMobs] &aYou can start the arena by doing &2/em start &aif there are at least &2$count &aplayers in it! \nYou can leave the arena by doing &c/em quit",
         true
      );
      arenaJoinSpectatorMessage = ConfigurationEngine.setString(
         List.of("Message that appears to remind players how to leave an arena after joining as a spectator."),
         this.file,
         this.fileConfiguration,
         "arenaJoinSpectatorMessage",
         "&2[EliteMobs] &aYou can leave the arena at any time using &2/em quit",
         true
      );
      waveTitle = ConfigurationEngine.setString(
         List.of("Title message that appears when a wave is starting."), this.file, this.fileConfiguration, "waveTitle", "&aWave &2$wave", true
      );
      waveSubtitle = ConfigurationEngine.setString(
         List.of("Subtitle message that appears when a wave is starting."), this.file, this.fileConfiguration, "waveSubtitle", "", true
      );
      victoryTitle = ConfigurationEngine.setString(
         List.of("Title that appears when an arena is completed."), this.file, this.fileConfiguration, "victoryTitle", "&2Victory!", true
      );
      victorySubtitle = ConfigurationEngine.setString(
         List.of("Subtitle that appears when an arena is completed."),
         this.file,
         this.fileConfiguration,
         "victorySubtitle",
         "&aCompleted &2$wave &awaves!",
         true
      );
      defeatTitle = ConfigurationEngine.setString(
         List.of("Title that appears when players fail an arena."), this.file, this.fileConfiguration, "defeatTitle", "&4Defeat!", true
      );
      defeatSubtitle = ConfigurationEngine.setString(
         List.of("Subitle that appears when players fail an arena."), this.file, this.fileConfiguration, "defeatSubtitle", "&cReached wave &4$wave&c!", true
      );
      victoryBroadcast = ConfigurationEngine.setString(
         List.of("Message broadcasted when players beat an arena."),
         this.file,
         this.fileConfiguration,
         "victoryBroadcast",
         "Arena $arenaName was conquered by $players!",
         true
      );
      joinPlayerTitle = ConfigurationEngine.setString(
         List.of("Title that appears when a player joins an arena."),
         this.file,
         this.fileConfiguration,
         "joinPlayerTitle",
         "&aStart with &2/em start &a!",
         true
      );
      joinPlayerSubtitle = ConfigurationEngine.setString(
         List.of("Subtitle that appears when a player joins an arena."),
         this.file,
         this.fileConfiguration,
         "joinPlayerSubtitle",
         "&cLeave with &4/em quit &c!",
         true
      );
      joinSpectatorTitle = ConfigurationEngine.setString(
         List.of("Title that appears when a spectator joins an arena."), this.file, this.fileConfiguration, "joinSpectatorTitle", "&aNow spectating!", true
      );
      joinSpectatorSubtitle = ConfigurationEngine.setString(
         List.of("Subtitle that appears when a spectator joins an arena."),
         this.file,
         this.fileConfiguration,
         "joinSpectatorSubtitle",
         "&cLeave with &4/em quit &c!",
         true
      );
      noArenaPermissionMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent to players if they do not have permission to enter an arena."),
         this.file,
         this.fileConfiguration,
         "noArenaPermissionMessage",
         "[EliteMobs] You don't have the permission to enter this arena!",
         true
      );
   }

   @Generated
   public static String getNotEnoughPlayersMessage() {
      return notEnoughPlayersMessage;
   }

   @Generated
   public static String getStartingTitle() {
      return startingTitle;
   }

   @Generated
   public static String getStartingSubtitle() {
      return startingSubtitle;
   }

   @Generated
   public static String getArenaFullMessage() {
      return arenaFullMessage;
   }

   @Generated
   public static String getArenasOngoingMessage() {
      return arenasOngoingMessage;
   }

   @Generated
   public static String getArenaStartHintMessage() {
      return arenaStartHintMessage;
   }

   @Generated
   public static String getArenaQuitHintMessage() {
      return arenaQuitHintMessage;
   }

   @Generated
   public static String getArenaJoinPlayerMessage() {
      return arenaJoinPlayerMessage;
   }

   @Generated
   public static String getArenaJoinSpectatorMessage() {
      return arenaJoinSpectatorMessage;
   }

   @Generated
   public static String getWaveTitle() {
      return waveTitle;
   }

   @Generated
   public static String getWaveSubtitle() {
      return waveSubtitle;
   }

   @Generated
   public static String getVictoryTitle() {
      return victoryTitle;
   }

   @Generated
   public static String getVictorySubtitle() {
      return victorySubtitle;
   }

   @Generated
   public static String getDefeatTitle() {
      return defeatTitle;
   }

   @Generated
   public static String getDefeatSubtitle() {
      return defeatSubtitle;
   }

   @Generated
   public static String getVictoryBroadcast() {
      return victoryBroadcast;
   }

   @Generated
   public static String getJoinPlayerTitle() {
      return joinPlayerTitle;
   }

   @Generated
   public static String getJoinPlayerSubtitle() {
      return joinPlayerSubtitle;
   }

   @Generated
   public static String getJoinSpectatorTitle() {
      return joinSpectatorTitle;
   }

   @Generated
   public static String getJoinSpectatorSubtitle() {
      return joinSpectatorSubtitle;
   }

   @Generated
   public static String getNoArenaPermissionMessage() {
      return noArenaPermissionMessage;
   }
}
