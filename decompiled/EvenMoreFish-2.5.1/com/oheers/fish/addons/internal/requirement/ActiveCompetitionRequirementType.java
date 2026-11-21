package com.oheers.fish.addons.internal.requirement;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.requirement.RequirementContext;
import com.oheers.fish.api.requirement.RequirementType;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.CompetitionManager;
import java.util.List;
import java.util.Optional;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;

public class ActiveCompetitionRequirementType extends RequirementType {
   private static final String NONE = "none";

   @Override
   public boolean checkRequirement(@NonNull RequirementContext context, @NonNull List<String> values) {
      String id = Optional.ofNullable(CompetitionManager.getInstance().getActiveCompetition()).map(Competition::getCompetitionName).orElse("none");
      boolean match = values.stream().anyMatch(id::equalsIgnoreCase);
      this.debugLogStatus(match, id);
      return match;
   }

   @NonNull
   @Override
   public String getIdentifier() {
      return "ACTIVE-COMPETITION";
   }

   @NonNull
   @Override
   public String getAuthor() {
      return "FireML";
   }

   @NonNull
   @Override
   public Plugin getPlugin() {
      return EvenMoreFish.getInstance();
   }
}
