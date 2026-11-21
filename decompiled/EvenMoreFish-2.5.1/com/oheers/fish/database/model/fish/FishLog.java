package com.oheers.fish.database.model.fish;

import com.oheers.fish.api.fishing.items.IFish;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import javax.annotation.Nullable;

public class FishLog {
   private final int userId;
   private final String fishName;
   private final String fishRarity;
   private final LocalDateTime catchTime;
   private final float length;
   @Nullable
   private final String competitionId;

   public FishLog(int userId, String fishName, String fishRarity, LocalDateTime catchTime, float length, @Nullable String competitionId) {
      this.userId = userId;
      this.fishName = fishName;
      this.fishRarity = fishRarity;
      this.catchTime = catchTime;
      this.length = length;
      this.competitionId = competitionId;
   }

   public FishLog(int userId, IFish fish, LocalDateTime catchTime, @Nullable String competitionId) {
      this.userId = userId;
      this.fishName = fish.getId();
      this.fishRarity = fish.getRarity().getId();
      this.catchTime = catchTime;
      this.length = fish.getLength();
      this.competitionId = competitionId;
   }

   public int getUserId() {
      return this.userId;
   }

   public String getFishName() {
      return this.fishName;
   }

   public String getFishRarity() {
      return this.fishRarity;
   }

   public LocalDateTime getCatchTime() {
      return this.catchTime;
   }

   public Timestamp getCatchTimestamp() {
      return Timestamp.valueOf(this.catchTime);
   }

   public float getLength() {
      return this.length;
   }

   @Nullable
   public String getCompetitionId() {
      return this.competitionId;
   }

   @Override
   public String toString() {
      return "FishLog{userId="
         + this.userId
         + ", fishName='"
         + this.fishName
         + "', rarity='"
         + this.fishRarity
         + "', catchTime="
         + this.catchTime
         + ", length="
         + this.length
         + ", competitionId='"
         + this.competitionId
         + "'}";
   }
}
