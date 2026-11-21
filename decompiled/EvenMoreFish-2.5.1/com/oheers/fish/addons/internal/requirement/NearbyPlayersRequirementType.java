package com.oheers.fish.addons.internal.requirement;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.requirement.RequirementContext;
import com.oheers.fish.api.requirement.RequirementType;
import com.oheers.fish.config.MainConfig;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;

public class NearbyPlayersRequirementType extends RequirementType {
   @Override
   public boolean checkRequirement(@NonNull RequirementContext context, @NonNull List<String> values) {
      Player player = context.getPlayer();
      if (player == null) {
         String configLocation = context.getConfigPath();
         if (configLocation == null) {
            configLocation = "N/A";
         }

         EvenMoreFish.getInstance()
            .getLogger()
            .warning(
               "Could not find a valid player for "
                  + configLocation
                  + ", returning false by default. The player may not have been given a fish if you see this message multiple times."
            );
         return false;
      } else {
         int range = MainConfig.getInstance().getNearbyPlayersRequirementRange();
         long nearbyPlayers = context.getPlayer().getNearbyEntities(range, range, range).stream().filter(entity -> entity instanceof Player).count();

         for (String value : values) {
            int nearbyRequirement;
            try {
               nearbyRequirement = Integer.parseInt(value);
            } catch (NumberFormatException var11) {
               EvenMoreFish.getInstance().getLogger().severe(value + " is not a valid integer");
               return false;
            }

            if (nearbyPlayers >= nearbyRequirement) {
               this.debugLogStatus(true, String.valueOf(nearbyPlayers));
               return true;
            }
         }

         this.debugLogStatus(false, String.valueOf(nearbyPlayers));
         return false;
      }
   }

   @NonNull
   @Override
   public String getIdentifier() {
      return "NEARBY-PLAYERS";
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
