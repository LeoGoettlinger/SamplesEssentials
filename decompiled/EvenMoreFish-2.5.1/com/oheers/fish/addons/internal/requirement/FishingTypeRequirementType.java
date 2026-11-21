package com.oheers.fish.addons.internal.requirement;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.fishing.FishingType;
import com.oheers.fish.api.requirement.RequirementContext;
import com.oheers.fish.api.requirement.RequirementType;
import java.util.List;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;

public class FishingTypeRequirementType extends RequirementType {
   @Override
   public boolean checkRequirement(@NonNull RequirementContext context, @NonNull List<String> values) {
      FishingType contextType = context.getFishingType();

      for (String value : values) {
         FishingType type = FishUtils.getEnumValue(FishingType.class, value);
         if (type != null && contextType == type) {
            this.debugLogStatus(true, contextType.toString());
            return true;
         }
      }

      this.debugLogStatus(false, contextType.toString());
      return false;
   }

   @NonNull
   @Override
   public String getIdentifier() {
      return "FISHING-TYPE";
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
