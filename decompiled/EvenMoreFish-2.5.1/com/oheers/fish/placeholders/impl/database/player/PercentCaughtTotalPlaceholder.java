package com.oheers.fish.placeholders.impl.database.player;

import com.oheers.fish.api.fishing.items.IFish;
import com.oheers.fish.placeholders.abstracted.UniqueFishCaughtProgressPlaceholder;
import java.util.List;
import java.util.UUID;
import org.bukkit.OfflinePlayer;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class PercentCaughtTotalPlaceholder extends UniqueFishCaughtProgressPlaceholder {
   public PercentCaughtTotalPlaceholder() {
      super("percent_caught_total_");
   }

   @Nullable
   @Override
   public String parsePAPI(@Nullable OfflinePlayer player, @NonNull String identifier) {
      String target = identifier.substring(this.getPrefixLength());
      UUID uuid = this.resolveTarget(player, target, identifier);
      if (uuid == null) {
         return null;
      } else {
         List<IFish> fishList = this.getAllFish();
         return this.formatPercent(this.countCaughtFish(uuid, fishList), fishList.size());
      }
   }
}
