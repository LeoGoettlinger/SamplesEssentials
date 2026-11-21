package com.oheers.fish.addons.external.requirement;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.requirement.RequirementContext;
import com.oheers.fish.api.requirement.RequirementType;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;

public class PermissionRequirementType extends RequirementType {
   @Override
   public boolean checkRequirement(@NonNull RequirementContext context, @NonNull List<String> values) {
      Player player = context.getPlayer();
      if (player == null) {
         return false;
      } else {
         for (String value : values) {
            if (player.hasPermission(value)) {
               return true;
            }
         }

         return false;
      }
   }

   @NonNull
   @Override
   public String getIdentifier() {
      return "PERMISSION";
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
