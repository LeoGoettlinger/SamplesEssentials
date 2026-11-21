package com.oheers.fish.placeholders.impl.database.player;

import com.oheers.fish.placeholders.abstracted.UniqueFishCaughtProgressPlaceholder;
import org.bukkit.OfflinePlayer;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class PercentCaughtInRarityPlaceholder extends UniqueFishCaughtProgressPlaceholder {
   public PercentCaughtInRarityPlaceholder() {
      super("percent_caught_in_rarity_");
   }

   @Nullable
   @Override
   public String parsePAPI(@Nullable OfflinePlayer player, @NonNull String identifier) {
      UniqueFishCaughtProgressPlaceholder.ParsedRarityTarget target = this.parseRarityTarget(player, identifier);
      if (target == null) {
         return null;
      } else {
         int caught = this.countCaughtFish(target.uuid(), target.fishList());
         return this.formatPercent(caught, target.fishList().size());
      }
   }
}
