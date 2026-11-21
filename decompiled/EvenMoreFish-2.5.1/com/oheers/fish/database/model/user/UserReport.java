package com.oheers.fish.database.model.user;

import com.oheers.fish.api.fishing.items.IFish;
import com.oheers.fish.database.data.FishRarityKey;
import java.util.UUID;
import org.jspecify.annotations.NonNull;

public class UserReport {
   private int id;
   private final UUID uuid;
   private FishRarityKey firstFish;
   private FishRarityKey recentFish;
   private FishRarityKey largestFish;
   private FishRarityKey shortestFish;
   private int numFishCaught;
   private int competitionsWon;
   private int competitionsJoined;
   private float largestLength;
   private float shortestLength;
   private float totalFishLength;
   private int fishSold;
   private double moneyEarned;

   public UserReport(
      int id,
      UUID uuid,
      FishRarityKey firstFish,
      FishRarityKey recentFish,
      FishRarityKey largestFish,
      FishRarityKey shortestFish,
      int numFishCaught,
      int competitionsWon,
      int competitionsJoined,
      float largestLength,
      float shortestLength,
      float totalFishLength,
      int fishSold,
      double moneyEarned
   ) {
      this.id = id;
      this.uuid = uuid;
      this.firstFish = firstFish;
      this.recentFish = recentFish;
      this.largestFish = largestFish;
      this.shortestFish = shortestFish;
      this.numFishCaught = numFishCaught;
      this.competitionsWon = competitionsWon;
      this.competitionsJoined = competitionsJoined;
      this.largestLength = largestLength;
      this.shortestLength = shortestLength;
      this.totalFishLength = totalFishLength;
      this.fishSold = fishSold;
      this.moneyEarned = moneyEarned;
   }

   public UserReport(
      UUID uuid,
      FishRarityKey firstFish,
      FishRarityKey recentFish,
      FishRarityKey largestFish,
      FishRarityKey shortestFish,
      int numFishCaught,
      int competitionsWon,
      int competitionsJoined,
      float largestLength,
      float shortestLength,
      float totalFishLength,
      int fishSold,
      double moneyEarned
   ) {
      this.uuid = uuid;
      this.firstFish = firstFish;
      this.recentFish = recentFish;
      this.largestFish = largestFish;
      this.shortestFish = shortestFish;
      this.numFishCaught = numFishCaught;
      this.competitionsWon = competitionsWon;
      this.competitionsJoined = competitionsJoined;
      this.largestLength = largestLength;
      this.shortestLength = shortestLength;
      this.totalFishLength = totalFishLength;
      this.fishSold = fishSold;
      this.moneyEarned = moneyEarned;
   }

   public FishRarityKey getFirstFish() {
      return this.firstFish;
   }

   public void setFirstFish(FishRarityKey firstFish) {
      this.firstFish = firstFish;
   }

   public FishRarityKey getRecentFish() {
      return this.recentFish;
   }

   public void setRecentFish(FishRarityKey recentFish) {
      this.recentFish = recentFish;
   }

   public FishRarityKey getLargestFish() {
      return this.largestFish;
   }

   public void setLargestFish(FishRarityKey largestFish) {
      this.largestFish = largestFish;
   }

   public int getNumFishCaught() {
      return this.numFishCaught;
   }

   public void setNumFishCaught(int numFishCaught) {
      this.numFishCaught = numFishCaught;
   }

   public int getCompetitionsWon() {
      return this.competitionsWon;
   }

   public void setCompetitionsWon(int competitionsWon) {
      this.competitionsWon = competitionsWon;
   }

   public int getCompetitionsJoined() {
      return this.competitionsJoined;
   }

   public void setCompetitionsJoined(int competitionsJoined) {
      this.competitionsJoined = competitionsJoined;
   }

   public float getTotalFishLength() {
      return this.totalFishLength;
   }

   public void setTotalFishLength(float totalFishLength) {
      this.totalFishLength = totalFishLength;
   }

   public float getLargestLength() {
      return this.largestLength;
   }

   public void setLargestLength(float largestLength) {
      this.largestLength = largestLength;
   }

   public void incrementFishCaught(final int magnitude) {
      this.numFishCaught += magnitude;
   }

   public void incrementTotalLength(final float magnitude) {
      this.totalFishLength += magnitude;
   }

   public void incrementCompetitionsJoined(final int magnitude) {
      this.competitionsJoined += magnitude;
   }

   public void incrementCompetitionsWon(final int magnitude) {
      this.competitionsWon += magnitude;
   }

   public int getId() {
      return this.id;
   }

   public UUID getUuid() {
      return this.uuid;
   }

   public void incrementFishSold(final int fishSold) {
      this.fishSold += fishSold;
   }

   public void incrementMoneyEarned(final double moneyEarned) {
      this.moneyEarned += moneyEarned;
   }

   public int getFishSold() {
      return this.fishSold;
   }

   public double getMoneyEarned() {
      return this.moneyEarned;
   }

   public FishRarityKey getShortestFish() {
      return this.shortestFish;
   }

   public float getShortestLength() {
      return this.shortestLength;
   }

   public void setShortestFish(FishRarityKey shortestFish) {
      this.shortestFish = shortestFish;
   }

   public void setShortestLength(float shortestLength) {
      this.shortestLength = shortestLength;
   }

   public void setShortestLengthAndFish(@NonNull IFish fish) {
      this.shortestLength = fish.getLength();
      this.shortestFish = FishRarityKey.of(fish);
   }

   public void setLongestLengthAndFish(@NonNull IFish fish) {
      this.largestLength = fish.getLength();
      this.largestFish = FishRarityKey.of(fish);
   }

   @Override
   public String toString() {
      return "UserReport{id="
         + this.id
         + ", uuid="
         + this.uuid
         + ", firstFish="
         + this.firstFish
         + ", recentFish="
         + this.recentFish
         + ", largestFish="
         + this.largestFish
         + ", shortestFish="
         + this.shortestFish
         + ", numFishCaught="
         + this.numFishCaught
         + ", competitionsWon="
         + this.competitionsWon
         + ", competitionsJoined="
         + this.competitionsJoined
         + ", largestLength="
         + this.largestLength
         + ", shortestLength="
         + this.shortestLength
         + ", totalFishLength="
         + this.totalFishLength
         + ", fishSold="
         + this.fishSold
         + ", moneyEarned="
         + this.moneyEarned
         + "}";
   }
}
