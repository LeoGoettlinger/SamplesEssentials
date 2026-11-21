package com.oheers.fish.database.model.fish;

import com.oheers.fish.FishUtils;
import com.oheers.fish.api.fishing.items.IFish;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class FishStats {
   @NonNull
   private final String fishName;
   @NonNull
   private final String fishRarity;
   @NonNull
   private final LocalDateTime firstCatchTime;
   @NonNull
   private final UUID discoverer;
   private final String discovererName;
   private float shortestLength;
   @NonNull
   private UUID shortestFisher;
   private String shortestFisherName;
   private float longestLength;
   @NonNull
   private UUID longestFisher;
   private String longestFisherName;
   private int quantity;

   public FishStats(
      @NonNull String fishName,
      @NonNull String fishRarity,
      @NonNull LocalDateTime firstCatchTime,
      @NonNull UUID discoverer,
      float shortestLength,
      @NonNull UUID shortestFisher,
      float longestLength,
      @NonNull UUID longestFisher,
      int quantity
   ) {
      this.fishName = fishName;
      this.fishRarity = fishRarity;
      this.firstCatchTime = firstCatchTime;
      this.discoverer = discoverer;
      this.discovererName = FishUtils.getPlayerName(discoverer);
      this.shortestLength = shortestLength;
      this.shortestFisher = shortestFisher;
      this.shortestFisherName = FishUtils.getPlayerName(shortestFisher);
      this.longestLength = longestLength;
      this.longestFisher = longestFisher;
      this.longestFisherName = FishUtils.getPlayerName(longestFisher);
      this.quantity = quantity;
   }

   public FishStats(
      IFish fish,
      @NonNull LocalDateTime firstCatchTime,
      @NonNull UUID discoverer,
      float shortestLength,
      @NonNull UUID shortestFisher,
      float longestLength,
      @NonNull UUID longestFisher,
      int quantity
   ) {
      this(fish.getId(), fish.getRarity().getId(), firstCatchTime, discoverer, shortestLength, shortestFisher, longestLength, longestFisher, quantity);
   }

   public static FishStats empty(IFish fish, LocalDateTime firstCatchTime) {
      return new FishStats(
         fish, firstCatchTime, fish.getFishermanUUID(), fish.getLength(), fish.getFishermanUUID(), fish.getLength(), fish.getFishermanUUID(), 0
      );
   }

   @NonNull
   public String getFishName() {
      return this.fishName;
   }

   @NonNull
   public String getFishRarity() {
      return this.fishRarity;
   }

   @NonNull
   public LocalDateTime getFirstCatchTime() {
      return this.firstCatchTime;
   }

   @NonNull
   public Timestamp getFirstCatchTimestamp() {
      return Timestamp.valueOf(this.firstCatchTime);
   }

   public float getShortestLength() {
      return this.shortestLength;
   }

   @NonNull
   public UUID getShortestFisher() {
      return this.shortestFisher;
   }

   @Nullable
   public String getShortestFisherName() {
      return this.shortestFisherName;
   }

   public float getLongestLength() {
      return this.longestLength;
   }

   @NonNull
   public UUID getLongestFisher() {
      return this.longestFisher;
   }

   @Nullable
   public String getLongestFisherName() {
      return this.longestFisherName;
   }

   public int getQuantity() {
      return this.quantity;
   }

   @NonNull
   public UUID getDiscoverer() {
      return this.discoverer;
   }

   @Nullable
   public String getDiscovererName() {
      return this.discovererName;
   }

   public void setShortestLength(float shortestLength) {
      this.shortestLength = shortestLength;
   }

   public void setShortestFisher(@NonNull UUID shortestFisher) {
      this.shortestFisher = shortestFisher;
      this.shortestFisherName = FishUtils.getPlayerName(shortestFisher);
   }

   public void setLongestLength(float longestLength) {
      this.longestLength = longestLength;
   }

   public void setLongestFisher(@NonNull UUID longestFisher) {
      this.longestFisher = longestFisher;
      this.longestFisherName = FishUtils.getPlayerName(longestFisher);
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
      return "FishStats{fishName='"
         + this.fishName
         + "', fishRarity='"
         + this.fishRarity
         + "', firstCatchTime="
         + this.firstCatchTime
         + ", discoverer="
         + this.discoverer
         + ", shortestLength="
         + this.shortestLength
         + ", shortestFisher="
         + this.shortestFisher
         + ", longestLength="
         + this.longestLength
         + ", longestFisher="
         + this.longestFisher
         + ", quantity="
         + this.quantity
         + "}";
   }
}
