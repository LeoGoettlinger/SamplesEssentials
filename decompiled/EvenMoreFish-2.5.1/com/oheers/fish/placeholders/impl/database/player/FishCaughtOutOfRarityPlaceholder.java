package com.oheers.fish.placeholders.impl.database.player;

import com.oheers.fish.placeholders.abstracted.UniqueFishCaughtProgressPlaceholder;
import org.bukkit.OfflinePlayer;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class FishCaughtOutOfRarityPlaceholder extends UniqueFishCaughtProgressPlaceholder {
   public FishCaughtOutOfRarityPlaceholder() {
      super("fish_caught_out_of_rarity_");
   }

   @Nullable
   @Override
   public String parsePAPI(@Nullable OfflinePlayer player, @NonNull String identifier) {
      UniqueFishCaughtProgressPlaceholder.ParsedRarityTarget target = this.parseRarityTarget(player, identifier);
      return target == null ? null : this.formatProgress(this.countCaughtFish(target.uuid(), target.fishList()), target.fishList().size());
   }
}
