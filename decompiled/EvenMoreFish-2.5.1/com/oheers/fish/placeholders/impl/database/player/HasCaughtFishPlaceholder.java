package com.oheers.fish.placeholders.impl.database.player;

import com.oheers.fish.placeholders.abstracted.UniqueFishCaughtProgressPlaceholder;
import org.bukkit.OfflinePlayer;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class HasCaughtFishPlaceholder extends UniqueFishCaughtProgressPlaceholder {
   public HasCaughtFishPlaceholder() {
      super("has_caught_");
   }

   @Nullable
   @Override
   public String parsePAPI(@Nullable OfflinePlayer player, @NonNull String identifier) {
      UniqueFishCaughtProgressPlaceholder.ParsedFishTarget target = this.parseFishTarget(player, identifier);
      return target == null ? null : String.valueOf(this.hasCaughtFish(target.uuid(), target.fish()));
   }
}
