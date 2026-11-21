package com.oheers.fish.addons.internal.requirement;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.requirement.RequirementContext;
import com.oheers.fish.api.requirement.RequirementType;
import java.util.List;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;

public class InGameTimeRequirementType extends RequirementType {
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
         for (String value : values) {
            String[] split = value.split("-");

            int minTime;
            int maxTime;
            try {
               minTime = Integer.parseInt(split[0]);
               maxTime = Integer.parseInt(split[1]);
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException var10) {
               EvenMoreFish.getInstance().getLogger().severe(value + " is not a valid ingame time format. Using the defaults.");
               minTime = 0;
               maxTime = 24000;
            }

            if (world.getTime() <= maxTime && world.getTime() >= minTime) {
               this.debugLogStatus(true, String.valueOf(world.getTime()));
               return true;
            }
         }

         this.debugLogStatus(false, String.valueOf(world.getTime()));
         return false;
      }
   }

   @NonNull
   @Override
   public String getIdentifier() {
      return "INGAME-TIME";
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
