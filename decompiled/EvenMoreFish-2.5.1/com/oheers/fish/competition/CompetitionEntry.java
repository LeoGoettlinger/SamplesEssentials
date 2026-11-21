package com.oheers.fish.competition;

import com.oheers.fish.api.fishing.items.IFish;
import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.NonNull;

public class CompetitionEntry {
   private final UUID player;
   private final IFish fish;
   protected long time;
   protected float value = 0.0F;
   private final CompetitionType type;

   public CompetitionEntry(UUID player, IFish fish, CompetitionType type) {
      this.player = player;
      this.fish = fish;
      this.time = Instant.now().toEpochMilli();
      this.type = type;
      this.trackFish(fish);
   }

   public CompetitionEntry(@NonNull CompetitionEntry entry) {
      this.player = entry.player;
      this.fish = entry.fish;
      this.time = Instant.now().toEpochMilli();
      this.value = entry.value;
      this.type = entry.type;
   }

   public void incrementValue(float increaseAmount) {
      this.value = this.value + Math.abs(increaseAmount);
      this.time = Instant.now().toEpochMilli();
   }

   public void trackFish(@NonNull IFish fish) {
      if (this.type.useFishLength()) {
         if (!fish.isLengthless()) {
            this.value = this.value + fish.getLength();
         }
      } else {
         this.value++;
      }
   }

   public IFish getFish() {
      return this.fish;
   }

   public long getTime() {
      return this.time;
   }

   public int getHash() {
      return this.hashCode();
   }

   public float getValue() {
      return this.value;
   }

   public void setValue(float value) {
      this.value = value;
   }

   public UUID getPlayer() {
      return this.player;
   }

   @Override
   public String toString() {
      return "CompetitionEntry[" + this.player + ", " + this.value + ", " + this.time + "]";
   }
}
