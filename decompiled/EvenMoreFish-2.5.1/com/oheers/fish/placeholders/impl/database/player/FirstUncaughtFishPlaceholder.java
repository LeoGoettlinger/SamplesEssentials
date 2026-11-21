package com.oheers.fish.placeholders.impl.database.player;

import com.oheers.fish.api.fishing.items.IFish;
import com.oheers.fish.placeholders.abstracted.UniqueFishCaughtProgressPlaceholder;
import org.bukkit.OfflinePlayer;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class FirstUncaughtFishPlaceholder extends UniqueFishCaughtProgressPlaceholder {
   public FirstUncaughtFishPlaceholder() {
      super("first_uncaught_fish_");
   }

   @Nullable
   @Override
   public String parsePAPI(@Nullable OfflinePlayer player, @NonNull String identifier) {
      UniqueFishCaughtProgressPlaceholder.ParsedRarityTarget target = this.parseRarityTarget(player, identifier);
      if (target == null) {
         return null;
      } else {
         IFish fish = this.firstUncaughtFish(target.uuid(), target.fishList());
         return fish == null ? "" : fish.getId();
      }
   }
}
