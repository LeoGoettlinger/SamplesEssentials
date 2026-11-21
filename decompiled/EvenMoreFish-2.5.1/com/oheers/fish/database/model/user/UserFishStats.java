package com.oheers.fish.database.model.user;

import com.oheers.fish.api.fishing.items.IFish;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class UserFishStats {
   private final int userId;
   private String fishName;
   private String fishRarity;
   private final LocalDateTime firstCatchTime;
   private float shortestLength;
   private float longestLength;
   private int quantity;

   public UserFishStats(int userId, String fishName, String fishRarity, LocalDateTime firstCatchTime, float shortestLength, float longestLength, int quantity) {
      this.userId = userId;
      this.fishName = fishName;
      this.fishRarity = fishRarity;
      this.firstCatchTime = firstCatchTime;
      this.shortestLength = shortestLength;
      this.longestLength = longestLength;
      this.quantity = quantity;
   }

   public UserFishStats(int userId, IFish fish, LocalDateTime firstCatchTime) {
      this.userId = userId;
      this.fishName = fish.getId();
      this.fishRarity = fish.getRarity().getId();
      this.firstCatchTime = firstCatchTime;
      this.shortestLength = fish.getLength();
      this.longestLength = fish.getLength();
      this.quantity = 0;
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

   public LocalDateTime getFirstCatchTime() {
      return this.firstCatchTime;
   }

   public Timestamp getFirstCatchTimestamp() {
      return Timestamp.valueOf(this.firstCatchTime);
   }

   public float getShortestLength() {
      return this.shortestLength;
   }

   public float getLongestLength() {
      return this.longestLength;
   }

   public int getQuantity() {
      return this.quantity;
   }

   public void setFishName(String fishName) {
      this.fishName = fishName;
   }

   public void setFishRarity(String fishRarity) {
      this.fishRarity = fishRarity;
   }

   public void setShortestLength(float shortestLength) {
      this.shortestLength = shortestLength;
   }

   public void setLongestLength(float longestLength) {
      this.longestLength = longestLength;
   }

   public void incrementQuantity() {
      this.quantity++;
   }

   public void incrementQuantity(int quantity) {
      this.quantity += quantity;
   }

   public void setQuantity(int quantity) {
      this.quantity = quantity;
   }

   @Override
   public String toString() {
      return "FishUserStats{user="
         + this.userId
         + ", fishName='"
         + this.fishName
         + "', fishRarity='"
         + this.fishRarity
         + "', firstCatchTime="
         + this.firstCatchTime
         + ", shortestLength="
         + this.shortestLength
         + ", longestLength="
         + this.longestLength
         + ", quantity="
         + this.quantity
         + "}";
   }
}
