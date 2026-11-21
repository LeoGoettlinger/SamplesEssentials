package com.oheers.fish.api.fishing.items;

import com.oheers.fish.api.Logging;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class RarityKey {
   private final IRarity rarity;
   private final IFish fish;

   private RarityKey(IRarity rarity, IFish fish) {
      this.rarity = rarity;
      this.fish = fish;
   }

   @NonNull
   public static RarityKey of(@NonNull IFish fish) {
      return new RarityKey(fish.getRarity(), fish);
   }

   @Nullable
   public static RarityKey of(@NonNull String rarityStr, @NonNull String fishStr) {
      IRarity rarity = AbstractFishManager.getInstance().getRarity(rarityStr);
      if (rarity == null) {
         Logging.warn("There is no rarity named " + rarityStr, new IllegalArgumentException());
         return null;
      } else {
         IFish fish = rarity.getFish(fishStr);
         if (fish == null) {
            fish = rarity.getFish(fishStr.replace("_", " "));
         }

         if (fish == null) {
            Logging.warn("Rarity " + rarityStr + " has no fish named " + fishStr, new IllegalArgumentException());
            return null;
         } else {
            return new RarityKey(rarity, fish);
         }
      }
   }

   @Nullable
   public static RarityKey of(@NonNull String keyString) {
      String[] split = keyString.split(":");
      if (split.length < 2) {
         return null;
      } else {
         String rarity = split[0];
         String fish = split[1];
         return of(rarity, fish);
      }
   }

   @NonNull
   public IFish getFish() {
      return this.fish.createCopy();
   }

   @NonNull
   public IRarity getRarity() {
      return this.rarity;
   }

   @Override
   public String toString() {
      return String.join(":", this.rarity.getId().toLowerCase(), this.fish.getId().toLowerCase().replace(" ", "_"));
   }
}
