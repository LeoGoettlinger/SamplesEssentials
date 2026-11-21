package de.theredend2000.advancedhunt.model;

import java.util.Objects;

public class Reward {
   private final RewardType type;
   private final double chance;
   private final String message;
   private final String broadcast;
   private final String value;

   public Reward(RewardType type, double chance, String message, String broadcast, String value) {
      this.type = Objects.requireNonNull(type, "Reward type cannot be null");
      if (!(chance < 0.0) && !(chance > 100.0)) {
         this.chance = chance;
         this.message = message;
         this.broadcast = broadcast;
         this.value = value;
      } else {
         throw new IllegalArgumentException("Chance must be between 0.0 and 100.0, got: " + chance);
      }
   }

   public RewardType getType() {
      return this.type;
   }

   public double getChance() {
      return this.chance;
   }

   public String getMessage() {
      return this.message;
   }

   public String getBroadcast() {
      return this.broadcast;
   }

   public String getValue() {
      return this.value;
   }

   @Override
   public String toString() {
      String valuePreview = this.value == null ? "null" : (this.value.length() > 30 ? this.value.substring(0, 30) + "..." : this.value);
      return "Reward{type=" + this.type + ", chance=" + this.chance + ", value='" + valuePreview + "'}";
   }
}
