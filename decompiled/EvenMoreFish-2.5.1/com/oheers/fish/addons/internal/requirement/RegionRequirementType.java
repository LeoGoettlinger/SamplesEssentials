package com.oheers.fish.addons.internal.requirement;

import com.oheers.fish.Checks;
import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.requirement.RequirementContext;
import com.oheers.fish.api.requirement.RequirementType;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;

public class RegionRequirementType extends RequirementType {
   @Override
   public boolean checkRequirement(@NonNull RequirementContext context, @NonNull List<String> values) {
      Location location = context.getHookOrPlayerLocation();
      if (location == null) {
         EvenMoreFish.getInstance().getLogger().severe("There is no valid location. Failing region Requirement.");
         return false;
      } else {
         return Checks.canUseRegion(location, values);
      }
   }

   @NonNull
   @Override
   public String getIdentifier() {
      return "REGION";
   }

   @NonNull
   @Override
   public String getAuthor() {
      return "Oheers";
   }

   @NonNull
   @Override
   public Plugin getPlugin() {
      return EvenMoreFish.getInstance();
   }
}
