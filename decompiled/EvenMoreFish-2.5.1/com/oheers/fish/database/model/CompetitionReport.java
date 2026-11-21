package com.oheers.fish.database.model;

import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.CompetitionEntry;
import com.oheers.fish.database.data.FishRarityKey;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class CompetitionReport {
   private final String competitionConfigId;
   private final String winnerFish;
   private final UUID winnerUuid;
   private final List<UUID> contestants;
   private final float winnerScore;
   private final LocalDateTime startTime;
   private final LocalDateTime endTime;

   public CompetitionReport(
      String competitionConfigID,
      String winnerFish,
      String winnerUUIDString,
      float winnerScore,
      String contestants,
      LocalDateTime startTime,
      LocalDateTime endTime
   ) {
      this.competitionConfigId = competitionConfigID;
      this.winnerFish = winnerFish;
      this.winnerUuid = UUID.fromString(winnerUUIDString);
      this.winnerScore = winnerScore;
      this.contestants = new ArrayList<>();

      for (String contestant : contestants.split(",")) {
         this.contestants.add(UUID.fromString(contestant));
      }

      this.startTime = startTime;
      this.endTime = endTime;
   }

   public CompetitionReport(Competition competition, LocalDateTime startTime, LocalDateTime endTime) {
      this.competitionConfigId = competition.getCompetitionName();
      CompetitionEntry topEntry = competition.getLeaderboard().getTopEntry();
      if (topEntry == null) {
         this.winnerFish = FishRarityKey.empty().toStringDefault();
         this.winnerScore = 0.0F;
         this.winnerUuid = null;
         this.contestants = Collections.emptyList();
      } else {
         this.winnerFish = FishRarityKey.of(competition.getLeaderboard().getTopEntry().getFish()).toString();
         this.winnerScore = competition.getLeaderboard().getTopEntry().getValue();
         this.winnerUuid = competition.getLeaderboard().getTopEntry().getPlayer();
         this.contestants = competition.getLeaderboard().getEntries().stream().map(CompetitionEntry::getPlayer).toList();
      }

      this.startTime = startTime;
      this.endTime = endTime;
   }

   public String getCompetitionConfigId() {
      return this.competitionConfigId;
   }

   public String getWinnerFish() {
      return this.winnerFish;
   }

   public UUID getWinnerUuid() {
      return this.winnerUuid;
   }

   public List<UUID> getContestants() {
      return this.contestants;
   }

   public float getWinnerScore() {
      return this.winnerScore;
   }

   public LocalDateTime getStartTime() {
      return this.startTime;
   }

   public Timestamp getStartTimestamp() {
      return Timestamp.valueOf(this.startTime);
   }

   public LocalDateTime getEndTime() {
      return this.endTime;
   }

   public Timestamp getEndTimestamp() {
      return Timestamp.valueOf(this.endTime);
   }
}
