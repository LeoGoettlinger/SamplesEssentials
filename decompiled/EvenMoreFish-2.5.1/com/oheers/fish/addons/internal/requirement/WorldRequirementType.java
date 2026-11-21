package com.oheers.fish.addons.internal.requirement;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.requirement.RequirementContext;
import com.oheers.fish.api.requirement.RequirementType;
import java.util.List;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;

public class WorldRequirementType extends RequirementType {
   @Override
   public boolean checkRequirement(@NonNull RequirementContext context, @NonNull List<String> values) {
      World world = context.getWorld();
      if (world == null) {
         String configLocation = context.getConfigPath();
         if (configLocation == null) {
            configLocation = "N/A";
         }

         EvenMoreFish.getInstance()
            .getLogger()
            .severe(
               "Could not get world for "
                  + configLocation
                  + ", returning false by default. The player may not have been given a fish if you see this message multiple times."
            );
         return false;
      } else {
         String worldName = world.getName();
         String worldKey = world.getKey().asString();

         for (String value : values) {
            if (worldName.equalsIgnoreCase(value) || worldKey.equals(value)) {
               this.debugLogStatus(true, worldName);
               return true;
            }
         }

         this.debugLogStatus(false, worldName);
         return false;
      }
   }

   @NonNull
   @Override
   public String getIdentifier() {
      return "WORLD";
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
