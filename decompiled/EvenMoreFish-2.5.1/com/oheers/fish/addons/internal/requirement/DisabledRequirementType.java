package com.oheers.fish.addons.internal.requirement;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.requirement.RequirementContext;
import com.oheers.fish.api.requirement.RequirementType;
import java.util.List;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;

public class DisabledRequirementType extends RequirementType {
   @Override
   public boolean checkRequirement(@NonNull RequirementContext context, @NonNull List<String> values) {
      for (String value : values) {
         if (!Boolean.parseBoolean(value)) {
            this.debugLogStatus(true, null);
            return true;
         }
      }

      this.debugLogStatus(false, null);
      return false;
   }

   @NonNull
   @Override
   public String getIdentifier() {
      return "DISABLED";
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
