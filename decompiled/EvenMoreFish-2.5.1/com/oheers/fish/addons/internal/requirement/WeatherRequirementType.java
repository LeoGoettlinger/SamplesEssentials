package com.oheers.fish.addons.internal.requirement;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.requirement.RequirementContext;
import com.oheers.fish.api.requirement.RequirementType;
import java.util.List;
import org.bukkit.WeatherType;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;

public class WeatherRequirementType extends RequirementType {
   @Override
   public boolean checkRequirement(@NonNull RequirementContext context, @NonNull List<String> values) {
      if (context.getWorld() == null) {
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
         boolean isClear = context.getWorld().isClearWeather();

         for (String value : values) {
            WeatherType weatherType = FishUtils.getEnumValue(WeatherType.class, value);
            if (weatherType == null) {
               EvenMoreFish.getInstance().getLogger().severe(value + " is not a valid weather type.");
               return false;
            }

            if (isClear ? weatherType.equals(WeatherType.CLEAR) : weatherType.equals(WeatherType.DOWNFALL)) {
               this.debugLogStatus(true, weatherType.toString());
               return true;
            }
         }

         this.debugLogStatus(false, null);
         return false;
      }
   }

   @NonNull
   @Override
   public String getIdentifier() {
      return "WEATHER";
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
