package com.oheers.fish.placeholders.impl.database.player;

import com.oheers.fish.placeholders.abstracted.UniqueFishCaughtProgressPlaceholder;
import org.bukkit.OfflinePlayer;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class DistinctFishCaughtInRarityPlaceholder extends UniqueFishCaughtProgressPlaceholder {
   public DistinctFishCaughtInRarityPlaceholder() {
      super("distinct_fish_caught_in_rarity_");
   }

   @Nullable
   @Override
   public String parsePAPI(@Nullable OfflinePlayer player, @NonNull String identifier) {
      UniqueFishCaughtProgressPlaceholder.ParsedRarityTarget target = this.parseRarityTarget(player, identifier);
      return target == null ? null : String.valueOf(this.countCaughtFish(target.uuid(), target.fishList()));
   }
}
