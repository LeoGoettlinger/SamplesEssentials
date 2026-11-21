package com.magmaguy.elitemobs.magmacore.match;

import lombok.Generated;

public final class MatchMessages {
   private final String notAccepting;
   private final String full;
   private final String noPermission;
   private final String alreadyInMatch;
   private final String joinedMessage;
   private final String joinedTitle;
   private final String joinedSubtitle;
   private final String spectatorMessage;
   private final String spectatorTitle;
   private final String spectatorSubtitle;
   private final String notEnoughPlayers;
   private final String startingTitle;
   private final String startingSubtitle;
   private final String waitingHint;

   public static MatchMessages defaults() {
      return builder().build();
   }

   public static MatchMessages none() {
      return builder()
         .notAccepting(null)
         .full(null)
         .noPermission(null)
         .alreadyInMatch(null)
         .joinedMessage(null)
         .joinedTitle(null)
         .joinedSubtitle(null)
         .spectatorMessage(null)
         .spectatorTitle(null)
         .spectatorSubtitle(null)
         .notEnoughPlayers(null)
         .startingTitle(null)
         .startingSubtitle(null)
         .waitingHint(null)
         .build();
   }

   @Generated
   private static String $default$notAccepting() {
      return "&cThis match is not accepting players right now.";
   }

   @Generated
   private static String $default$full() {
      return "&cThis match is full.";
   }

   @Generated
   private static String $default$noPermission() {
      return "&cYou don't have permission to join this match.";
   }

   @Generated
   private static String $default$alreadyInMatch() {
      return "&cYou are already in a match.";
   }

   @Generated
   private static String $default$joinedMessage() {
      return "&aYou joined the match. It needs $count players to start.";
   }

   @Generated
   private static String $default$joinedTitle() {
      return "Joined!";
   }

   @Generated
   private static String $default$joinedSubtitle() {
      return null;
   }

   @Generated
   private static String $default$spectatorMessage() {
      return "&aYou are now spectating this match.";
   }

   @Generated
   private static String $default$spectatorTitle() {
      return null;
   }

   @Generated
   private static String $default$spectatorSubtitle() {
      return null;
   }

   @Generated
   private static String $default$notEnoughPlayers() {
      return "&cThis match needs $amount players to start.";
   }

   @Generated
   private static String $default$startingTitle() {
      return "Match starting!";
   }

   @Generated
   private static String $default$startingSubtitle() {
      return "in $count...";
   }

   @Generated
   private static String $default$waitingHint() {
      return "&7Waiting to start. This match needs $count players.";
   }

   @Generated
   MatchMessages(
      String notAccepting,
      String full,
      String noPermission,
      String alreadyInMatch,
      String joinedMessage,
      String joinedTitle,
      String joinedSubtitle,
      String spectatorMessage,
      String spectatorTitle,
      String spectatorSubtitle,
      String notEnoughPlayers,
      String startingTitle,
      String startingSubtitle,
      String waitingHint
   ) {
      this.notAccepting = notAccepting;
      this.full = full;
      this.noPermission = noPermission;
      this.alreadyInMatch = alreadyInMatch;
      this.joinedMessage = joinedMessage;
      this.joinedTitle = joinedTitle;
      this.joinedSubtitle = joinedSubtitle;
      this.spectatorMessage = spectatorMessage;
      this.spectatorTitle = spectatorTitle;
      this.spectatorSubtitle = spectatorSubtitle;
      this.notEnoughPlayers = notEnoughPlayers;
      this.startingTitle = startingTitle;
      this.startingSubtitle = startingSubtitle;
      this.waitingHint = waitingHint;
   }

   @Generated
   public static MatchMessages.MatchMessagesBuilder builder() {
      return new MatchMessages.MatchMessagesBuilder();
   }

   @Generated
   public MatchMessages.MatchMessagesBuilder toBuilder() {
      return new MatchMessages.MatchMessagesBuilder()
         .notAccepting(this.notAccepting)
         .full(this.full)
         .noPermission(this.noPermission)
         .alreadyInMatch(this.alreadyInMatch)
         .joinedMessage(this.joinedMessage)
         .joinedTitle(this.joinedTitle)
         .joinedSubtitle(this.joinedSubtitle)
         .spectatorMessage(this.spectatorMessage)
         .spectatorTitle(this.spectatorTitle)
         .spectatorSubtitle(this.spectatorSubtitle)
         .notEnoughPlayers(this.notEnoughPlayers)
         .startingTitle(this.startingTitle)
         .startingSubtitle(this.startingSubtitle)
         .waitingHint(this.waitingHint);
   }

   @Generated
   public String getNotAccepting() {
      return this.notAccepting;
   }

   @Generated
   public String getFull() {
      return this.full;
   }

   @Generated
   public String getNoPermission() {
      return this.noPermission;
   }

   @Generated
   public String getAlreadyInMatch() {
      return this.alreadyInMatch;
   }

   @Generated
   public String getJoinedMessage() {
      return this.joinedMessage;
   }

   @Generated
   public String getJoinedTitle() {
      return this.joinedTitle;
   }

   @Generated
   public String getJoinedSubtitle() {
      return this.joinedSubtitle;
   }

   @Generated
   public String getSpectatorMessage() {
      return this.spectatorMessage;
   }

   @Generated
   public String getSpectatorTitle() {
      return this.spectatorTitle;
   }

   @Generated
   public String getSpectatorSubtitle() {
      return this.spectatorSubtitle;
   }

   @Generated
   public String getNotEnoughPlayers() {
      return this.notEnoughPlayers;
   }

   @Generated
   public String getStartingTitle() {
      return this.startingTitle;
   }

   @Generated
   public String getStartingSubtitle() {
      return this.startingSubtitle;
   }

   @Generated
   public String getWaitingHint() {
      return this.waitingHint;
   }

   @Generated
   public static class MatchMessagesBuilder {
      @Generated
      private boolean notAccepting$set;
      @Generated
      private String notAccepting$value;
      @Generated
      private boolean full$set;
      @Generated
      private String full$value;
      @Generated
      private boolean noPermission$set;
      @Generated
      private String noPermission$value;
      @Generated
      private boolean alreadyInMatch$set;
      @Generated
      private String alreadyInMatch$value;
      @Generated
      private boolean joinedMessage$set;
      @Generated
      private String joinedMessage$value;
      @Generated
      private boolean joinedTitle$set;
      @Generated
      private String joinedTitle$value;
      @Generated
      private boolean joinedSubtitle$set;
      @Generated
      private String joinedSubtitle$value;
      @Generated
      private boolean spectatorMessage$set;
      @Generated
      private String spectatorMessage$value;
      @Generated
      private boolean spectatorTitle$set;
      @Generated
      private String spectatorTitle$value;
      @Generated
      private boolean spectatorSubtitle$set;
      @Generated
      private String spectatorSubtitle$value;
      @Generated
      private boolean notEnoughPlayers$set;
      @Generated
      private String notEnoughPlayers$value;
      @Generated
      private boolean startingTitle$set;
      @Generated
      private String startingTitle$value;
      @Generated
      private boolean startingSubtitle$set;
      @Generated
      private String startingSubtitle$value;
      @Generated
      private boolean waitingHint$set;
      @Generated
      private String waitingHint$value;

      @Generated
      MatchMessagesBuilder() {
      }

      @Generated
      public MatchMessages.MatchMessagesBuilder notAccepting(String notAccepting) {
         this.notAccepting$value = notAccepting;
         this.notAccepting$set = true;
         return this;
      }

      @Generated
      public MatchMessages.MatchMessagesBuilder full(String full) {
         this.full$value = full;
         this.full$set = true;
         return this;
      }

      @Generated
      public MatchMessages.MatchMessagesBuilder noPermission(String noPermission) {
         this.noPermission$value = noPermission;
         this.noPermission$set = true;
         return this;
      }

      @Generated
      public MatchMessages.MatchMessagesBuilder alreadyInMatch(String alreadyInMatch) {
         this.alreadyInMatch$value = alreadyInMatch;
         this.alreadyInMatch$set = true;
         return this;
      }

      @Generated
      public MatchMessages.MatchMessagesBuilder joinedMessage(String joinedMessage) {
         this.joinedMessage$value = joinedMessage;
         this.joinedMessage$set = true;
         return this;
      }

      @Generated
      public MatchMessages.MatchMessagesBuilder joinedTitle(String joinedTitle) {
         this.joinedTitle$value = joinedTitle;
         this.joinedTitle$set = true;
         return this;
      }

      @Generated
      public MatchMessages.MatchMessagesBuilder joinedSubtitle(String joinedSubtitle) {
         this.joinedSubtitle$value = joinedSubtitle;
         this.joinedSubtitle$set = true;
         return this;
      }

      @Generated
      public MatchMessages.MatchMessagesBuilder spectatorMessage(String spectatorMessage) {
         this.spectatorMessage$value = spectatorMessage;
         this.spectatorMessage$set = true;
         return this;
      }

      @Generated
      public MatchMessages.MatchMessagesBuilder spectatorTitle(String spectatorTitle) {
         this.spectatorTitle$value = spectatorTitle;
         this.spectatorTitle$set = true;
         return this;
      }

      @Generated
      public MatchMessages.MatchMessagesBuilder spectatorSubtitle(String spectatorSubtitle) {
         this.spectatorSubtitle$value = spectatorSubtitle;
         this.spectatorSubtitle$set = true;
         return this;
      }

      @Generated
      public MatchMessages.MatchMessagesBuilder notEnoughPlayers(String notEnoughPlayers) {
         this.notEnoughPlayers$value = notEnoughPlayers;
         this.notEnoughPlayers$set = true;
         return this;
      }

      @Generated
      public MatchMessages.MatchMessagesBuilder startingTitle(String startingTitle) {
         this.startingTitle$value = startingTitle;
         this.startingTitle$set = true;
         return this;
      }

      @Generated
      public MatchMessages.MatchMessagesBuilder startingSubtitle(String startingSubtitle) {
         this.startingSubtitle$value = startingSubtitle;
         this.startingSubtitle$set = true;
         return this;
      }

      @Generated
      public MatchMessages.MatchMessagesBuilder waitingHint(String waitingHint) {
         this.waitingHint$value = waitingHint;
         this.waitingHint$set = true;
         return this;
      }

      @Generated
      public MatchMessages build() {
         String notAccepting$value = this.notAccepting$value;
         if (!this.notAccepting$set) {
            notAccepting$value = MatchMessages.$default$notAccepting();
         }

         String full$value = this.full$value;
         if (!this.full$set) {
            full$value = MatchMessages.$default$full();
         }

         String noPermission$value = this.noPermission$value;
         if (!this.noPermission$set) {
            noPermission$value = MatchMessages.$default$noPermission();
         }

         String alreadyInMatch$value = this.alreadyInMatch$value;
         if (!this.alreadyInMatch$set) {
            alreadyInMatch$value = MatchMessages.$default$alreadyInMatch();
         }

         String joinedMessage$value = this.joinedMessage$value;
         if (!this.joinedMessage$set) {
            joinedMessage$value = MatchMessages.$default$joinedMessage();
         }

         String joinedTitle$value = this.joinedTitle$value;
         if (!this.joinedTitle$set) {
            joinedTitle$value = MatchMessages.$default$joinedTitle();
         }

         String joinedSubtitle$value = this.joinedSubtitle$value;
         if (!this.joinedSubtitle$set) {
            joinedSubtitle$value = MatchMessages.$default$joinedSubtitle();
         }

         String spectatorMessage$value = this.spectatorMessage$value;
         if (!this.spectatorMessage$set) {
            spectatorMessage$value = MatchMessages.$default$spectatorMessage();
         }

         String spectatorTitle$value = this.spectatorTitle$value;
         if (!this.spectatorTitle$set) {
            spectatorTitle$value = MatchMessages.$default$spectatorTitle();
         }

         String spectatorSubtitle$value = this.spectatorSubtitle$value;
         if (!this.spectatorSubtitle$set) {
            spectatorSubtitle$value = MatchMessages.$default$spectatorSubtitle();
         }

         String notEnoughPlayers$value = this.notEnoughPlayers$value;
         if (!this.notEnoughPlayers$set) {
            notEnoughPlayers$value = MatchMessages.$default$notEnoughPlayers();
         }

         String startingTitle$value = this.startingTitle$value;
         if (!this.startingTitle$set) {
            startingTitle$value = MatchMessages.$default$startingTitle();
         }

         String startingSubtitle$value = this.startingSubtitle$value;
         if (!this.startingSubtitle$set) {
            startingSubtitle$value = MatchMessages.$default$startingSubtitle();
         }

         String waitingHint$value = this.waitingHint$value;
         if (!this.waitingHint$set) {
            waitingHint$value = MatchMessages.$default$waitingHint();
         }

         return new MatchMessages(
            notAccepting$value,
            full$value,
            noPermission$value,
            alreadyInMatch$value,
            joinedMessage$value,
            joinedTitle$value,
            joinedSubtitle$value,
            spectatorMessage$value,
            spectatorTitle$value,
            spectatorSubtitle$value,
            notEnoughPlayers$value,
            startingTitle$value,
            startingSubtitle$value,
            waitingHint$value
         );
      }

      @Generated
      @Override
      public String toString() {
         return "MatchMessages.MatchMessagesBuilder(notAccepting$value="
            + this.notAccepting$value
            + ", full$value="
            + this.full$value
            + ", noPermission$value="
            + this.noPermission$value
            + ", alreadyInMatch$value="
            + this.alreadyInMatch$value
            + ", joinedMessage$value="
            + this.joinedMessage$value
            + ", joinedTitle$value="
            + this.joinedTitle$value
            + ", joinedSubtitle$value="
            + this.joinedSubtitle$value
            + ", spectatorMessage$value="
            + this.spectatorMessage$value
            + ", spectatorTitle$value="
            + this.spectatorTitle$value
            + ", spectatorSubtitle$value="
            + this.spectatorSubtitle$value
            + ", notEnoughPlayers$value="
            + this.notEnoughPlayers$value
            + ", startingTitle$value="
            + this.startingTitle$value
            + ", startingSubtitle$value="
            + this.startingSubtitle$value
            + ", waitingHint$value="
            + this.waitingHint$value
            + ")";
      }
   }
}
