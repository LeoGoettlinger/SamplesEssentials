package com.oheers.fish.placeholders.impl.competition;

import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.CompetitionManager;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.placeholders.abstracted.EMFPlaceholder;
import org.bukkit.OfflinePlayer;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class CompetitionTypePlaceholder implements EMFPlaceholder {
   @Override
   public boolean shouldProcess(@NonNull String identifier) {
      return identifier.equalsIgnoreCase("competition_type");
   }

   @NonNull
   @Override
   public String parsePAPI(@Nullable OfflinePlayer player, @NonNull String identifier) {
      Competition activeComp = CompetitionManager.getInstance().getActiveCompetition();
      return activeComp != null && activeComp.getCompetitionType() != null
         ? activeComp.getCompetitionType().getKey()
         : ConfigMessage.PLACEHOLDER_NO_COMPETITION_RUNNING.getMessage().getLegacyMessage(null);
   }
}
