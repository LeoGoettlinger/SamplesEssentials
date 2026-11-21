package com.oheers.fish.database.data;

import com.oheers.fish.api.fishing.items.IFish;
import org.jspecify.annotations.NonNull;

public record UserFishRarityKey(int userId, @NonNull String fishName, @NonNull String fishRarity) {
   @NonNull
   public static UserFishRarityKey of(final int userId, final String fishName, final String fishRarity) {
      return new UserFishRarityKey(userId, fishName, fishRarity);
   }

   @NonNull
   public static UserFishRarityKey of(final int userId, @NonNull final IFish fish) {
      return new UserFishRarityKey(userId, fish.getId(), fish.getRarity().getId());
   }

   @NonNull
   public static UserFishRarityKey from(@NonNull final String pattern) {
      int firstSeparator = pattern.indexOf(46);
      int lastSeparator = pattern.lastIndexOf(46);
      if (firstSeparator > 0 && lastSeparator > firstSeparator && lastSeparator != pattern.length() - 1) {
         try {
            int userId = Integer.parseInt(pattern.substring(0, firstSeparator));
            String fishName = pattern.substring(firstSeparator + 1, lastSeparator);
            String fishRarity = pattern.substring(lastSeparator + 1);
            return new UserFishRarityKey(userId, fishName, fishRarity);
         } catch (NumberFormatException var6) {
            return empty();
         }
      } else {
         return empty();
      }
   }

   @NonNull
   public static UserFishRarityKey empty() {
      return new UserFishRarityKey(-1, "", "");
   }

   @NonNull
   @Override
   public String toString() {
      return this.userId + "." + this.fishName + "." + this.fishRarity;
   }

   public String toStringDefault() {
      return "UserFishRarityKey{userId='" + this.userId + "'fishName='" + this.fishName + "', fishRarity='" + this.fishRarity + "'}";
   }
}
