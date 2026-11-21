package com.oheers.fish.placeholders.impl.database.player;

import com.oheers.fish.placeholders.abstracted.UniqueFishCaughtProgressPlaceholder;
import org.bukkit.OfflinePlayer;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class RemainingFishInRarityPlaceholder extends UniqueFishCaughtProgressPlaceholder {
   public RemainingFishInRarityPlaceholder() {
      super("remaining_fish_in_rarity_");
   }

   @Nullable
   @Override
   public String parsePAPI(@Nullable OfflinePlayer player, @NonNull String identifier) {
      UniqueFishCaughtProgressPlaceholder.ParsedRarityTarget target = this.parseRarityTarget(player, identifier);
      return target == null ? null : String.valueOf(this.remainingFish(target.uuid(), target.fishList()));
   }
}
