package com.oheers.fish.database.data;

import com.oheers.fish.api.fishing.items.IFish;
import java.time.LocalDateTime;
import org.jspecify.annotations.NonNull;

public class FishLogKey {
   private final int userId;
   private final String fishName;
   private final String fishRarity;
   private final LocalDateTime dateTime;

   public FishLogKey(int userId, String fishName, String fishRarity, LocalDateTime dateTime) {
      this.userId = userId;
      this.fishName = fishName;
      this.fishRarity = fishRarity;
      this.dateTime = dateTime;
   }

   public FishLogKey(int userId, IFish fish, LocalDateTime dateTime) {
      this.userId = userId;
      this.fishName = fish.getId();
      this.fishRarity = fish.getRarity().getId();
      this.dateTime = dateTime;
   }

   @NonNull
   public static FishLogKey of(int userId, final String fishName, final String fishRarity, LocalDateTime dateTime) {
      return new FishLogKey(userId, fishName, fishRarity, dateTime);
   }

   @NonNull
   public static FishLogKey from(final String pattern) {
      String[] split = pattern.split("\\.");
      return new FishLogKey(Integer.parseInt(split[0]), split[1], split[2], LocalDateTime.parse(split[3]));
   }

   @NonNull
   public static FishLogKey of(int userId, @NonNull final IFish fish, LocalDateTime dateTime) {
      return new FishLogKey(userId, fish, dateTime);
   }

   @Override
   public String toString() {
      return this.userId + "." + this.fishName + "." + this.fishRarity + "." + this.dateTime.toString();
   }

   public String toStringDefault() {
      return "FishLogKey{userId=" + this.userId + ", fishName='" + this.fishName + "', fishRarity='" + this.fishRarity + "', dateTime=" + this.dateTime + "}";
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

   public LocalDateTime getDateTime() {
      return this.dateTime;
   }
}
