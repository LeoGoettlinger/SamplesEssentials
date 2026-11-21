package com.viaversion.viaversion.protocols.v1_20_3to1_20_5.storage;

import com.viaversion.viaversion.connection.ProtocolStorablesBase;

public final class ProtocolStorables1_20_5 extends ProtocolStorablesBase {
   private final ArmorTrimStorage armorTrims = new ArmorTrimStorage();
   private AcknowledgedMessagesStorage acknowledgedMessages = new AcknowledgedMessagesStorage();
   private ScoreboardTeamStorage scoreboardTeams = new ScoreboardTeamStorage();
   private BannerPatternStorage bannerPatterns;
   private TagKeys tagKeys;

   public AcknowledgedMessagesStorage acknowledgedMessages() {
      return this.acknowledgedMessages;
   }

   public void setAcknowledgedMessages(AcknowledgedMessagesStorage acknowledgedMessages) {
      this.acknowledgedMessages = acknowledgedMessages;
   }

   public ArmorTrimStorage armorTrims() {
      return this.armorTrims;
   }

   public ScoreboardTeamStorage scoreboardTeams() {
      return this.scoreboardTeams;
   }

   public void setScoreboardTeams(ScoreboardTeamStorage scoreboardTeams) {
      this.scoreboardTeams = scoreboardTeams;
   }

   public BannerPatternStorage bannerPatterns() {
      return this.bannerPatterns;
   }

   public void setBannerPatterns(BannerPatternStorage bannerPatterns) {
      this.bannerPatterns = bannerPatterns;
   }

   public TagKeys tagKeys() {
      return this.tagKeys;
   }

   public void setTagKeys(TagKeys tagKeys) {
      this.tagKeys = tagKeys;
   }
}
