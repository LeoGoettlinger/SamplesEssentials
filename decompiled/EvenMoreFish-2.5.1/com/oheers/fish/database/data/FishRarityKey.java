package com.oheers.fish.database.data;

import com.oheers.fish.api.fishing.items.IFish;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

public record FishRarityKey(@NonNull String fishName, @NonNull String fishRarity) {
   @Contract(
      value = "_, _ -> new",
      pure = true
   )
   @NonNull
   public static FishRarityKey of(final String fishName, final String fishRarity) {
      return new FishRarityKey(fishName, fishRarity);
   }

   @NonNull
   public static FishRarityKey from(final String pattern) {
      if (pattern != null && !pattern.isEmpty()) {
         int separatorIndex = pattern.lastIndexOf(46);
         return separatorIndex > 0 && separatorIndex != pattern.length() - 1
            ? new FishRarityKey(pattern.substring(0, separatorIndex), pattern.substring(separatorIndex + 1))
            : empty();
      } else {
         return empty();
      }
   }

   @NonNull
   public static FishRarityKey of(@NonNull final IFish fish) {
      return new FishRarityKey(fish.getId(), fish.getRarity().getId());
   }

   @NonNull
   public static FishRarityKey empty() {
      return new FishRarityKey("", "");
   }

   @NonNull
   @Override
   public String toString() {
      return this.fishName + "." + this.fishRarity;
   }

   public String toStringDefault() {
      return "FishRarityKey{fishName='" + this.fishName + "', fishRarity='" + this.fishRarity + "'}";
   }
}
