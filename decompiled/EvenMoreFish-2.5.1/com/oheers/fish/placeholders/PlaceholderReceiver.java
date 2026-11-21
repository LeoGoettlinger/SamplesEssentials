package com.oheers.fish.placeholders;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.placeholders.abstracted.EMFPlaceholder;
import com.oheers.fish.placeholders.impl.competition.CompetitionActivePlaceholder;
import com.oheers.fish.placeholders.impl.competition.CompetitionPlaceFishPlaceholder;
import com.oheers.fish.placeholders.impl.competition.CompetitionPlacePlayerPlaceholder;
import com.oheers.fish.placeholders.impl.competition.CompetitionPlaceSizePlaceholder;
import com.oheers.fish.placeholders.impl.competition.CompetitionTimeLeftPlaceholder;
import com.oheers.fish.placeholders.impl.competition.CompetitionTypeFormatPlaceholder;
import com.oheers.fish.placeholders.impl.competition.CompetitionTypePlaceholder;
import com.oheers.fish.placeholders.impl.database.player.DistinctFishCaughtInRarityPlaceholder;
import com.oheers.fish.placeholders.impl.database.player.FirstUncaughtFishPlaceholder;
import com.oheers.fish.placeholders.impl.database.player.FishCaughtOutOfRarityPlaceholder;
import com.oheers.fish.placeholders.impl.database.player.FishCaughtOutOfTotalPlaceholder;
import com.oheers.fish.placeholders.impl.database.player.HasCaughtFishPlaceholder;
import com.oheers.fish.placeholders.impl.database.player.HasCompletedCollectionPlaceholder;
import com.oheers.fish.placeholders.impl.database.player.HasCompletedRarityPlaceholder;
import com.oheers.fish.placeholders.impl.database.player.PercentCaughtInRarityPlaceholder;
import com.oheers.fish.placeholders.impl.database.player.PercentCaughtTotalPlaceholder;
import com.oheers.fish.placeholders.impl.database.player.RemainingFishInRarityPlaceholder;
import com.oheers.fish.placeholders.impl.database.player.RemainingFishTotalPlaceholder;
import com.oheers.fish.placeholders.impl.database.player.TimesCaughtFishPlaceholder;
import com.oheers.fish.placeholders.impl.database.player.TotalCompetitionsJoinedPlaceholder;
import com.oheers.fish.placeholders.impl.database.player.TotalCompetitionsWonPlaceholder;
import com.oheers.fish.placeholders.impl.database.player.TotalFishCaughtInRarityPlaceholder;
import com.oheers.fish.placeholders.impl.database.player.TotalFishCaughtPlaceholder;
import com.oheers.fish.placeholders.impl.database.player.TotalFishSoldPlaceholder;
import com.oheers.fish.placeholders.impl.database.player.TotalMoneyEarnedPlaceholder;
import com.oheers.fish.placeholders.impl.player.CustomFishingBooleanPlaceholder;
import com.oheers.fish.placeholders.impl.player.CustomFishingStatusPlaceholder;
import java.util.List;
import java.util.stream.Stream;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class PlaceholderReceiver extends PlaceholderExpansion {
   private final EvenMoreFish plugin;
   private final List<EMFPlaceholder> handlers;

   public PlaceholderReceiver(@NonNull EvenMoreFish plugin) {
      this.plugin = plugin;
      this.handlers = this.createHandlers();
   }

   public boolean persist() {
      return true;
   }

   public boolean canRegister() {
      return true;
   }

   @NonNull
   public String getAuthor() {
      return this.plugin.getPluginMeta().getAuthors().toString();
   }

   @NonNull
   public String getIdentifier() {
      return "emf";
   }

   @NonNull
   public String getVersion() {
      return this.plugin.getPluginMeta().getVersion();
   }

   private List<EMFPlaceholder> createHandlers() {
      return Stream.of(
            new CompetitionActivePlaceholder(),
            new CompetitionPlaceFishPlaceholder(),
            new CompetitionPlacePlayerPlaceholder(),
            new CompetitionPlaceSizePlaceholder(),
            new CompetitionTimeLeftPlaceholder(),
            new CompetitionTypeFormatPlaceholder(),
            new CompetitionTypePlaceholder(),
            new DistinctFishCaughtInRarityPlaceholder(),
            new FishCaughtOutOfRarityPlaceholder(),
            new FishCaughtOutOfTotalPlaceholder(),
            new FirstUncaughtFishPlaceholder(),
            new HasCaughtFishPlaceholder(),
            new HasCompletedCollectionPlaceholder(),
            new HasCompletedRarityPlaceholder(),
            new PercentCaughtInRarityPlaceholder(),
            new PercentCaughtTotalPlaceholder(),
            new RemainingFishInRarityPlaceholder(),
            new RemainingFishTotalPlaceholder(),
            new TimesCaughtFishPlaceholder(),
            new TotalCompetitionsJoinedPlaceholder(),
            new TotalCompetitionsWonPlaceholder(),
            new TotalFishCaughtPlaceholder(),
            new TotalFishCaughtInRarityPlaceholder(),
            new TotalFishSoldPlaceholder(),
            new TotalMoneyEarnedPlaceholder(),
            new CustomFishingBooleanPlaceholder(),
            new CustomFishingStatusPlaceholder()
         )
         .toList();
   }

   @Nullable
   public String onRequest(@Nullable OfflinePlayer player, @NonNull final String identifier) {
      for (EMFPlaceholder handler : this.handlers) {
         if (handler.shouldProcess(identifier)) {
            return handler.parsePAPI(player, identifier);
         }
      }

      return null;
   }
}
