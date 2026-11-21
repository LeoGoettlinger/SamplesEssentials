package com.oheers.fish.addons.internal.requirement;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.registry.EMFRegistry;
import com.oheers.fish.api.requirement.RequirementContext;
import com.oheers.fish.api.requirement.RequirementType;
import java.util.List;
import java.util.Objects;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;

public class RewardTypeRequirementType extends RequirementType {
   @Override
   public boolean checkRequirement(@NonNull RequirementContext context, @NonNull List<String> values) {
      boolean pass = values.stream().filter(Objects::nonNull).allMatch(value -> EMFRegistry.REWARD_TYPE.get(value) != null);
      this.debugLogStatus(pass, null);
      return pass;
   }

   @NonNull
   @Override
   public String getIdentifier() {
      return "REWARD-TYPE";
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
