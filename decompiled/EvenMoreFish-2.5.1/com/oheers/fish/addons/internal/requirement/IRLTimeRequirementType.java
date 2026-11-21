package com.oheers.fish.addons.internal.requirement;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.requirement.RequirementContext;
import com.oheers.fish.api.requirement.RequirementType;
import java.time.Instant;
import java.util.List;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;

public class IRLTimeRequirementType extends RequirementType {
   @Override
   public boolean checkRequirement(@NonNull RequirementContext context, @NonNull List<String> values) {
      long currentTime = Instant.now().getEpochSecond() / 60L % 1440L + 60L;

      for (String value : values) {
         String[] split = value.split("-");

         int minTime;
         int maxTime;
         try {
            minTime = this.getDayMinute(split[0], 0);
            maxTime = this.getDayMinute(split[1], 1440);
         } catch (ArrayIndexOutOfBoundsException var11) {
            EvenMoreFish.getInstance().getLogger().severe(value + " is not a valid real time format. Using the defaults.");
            minTime = 0;
            maxTime = 1440;
         }

         if (currentTime >= minTime && currentTime < maxTime) {
            this.debugLogStatus(true, String.valueOf(currentTime));
            return true;
         }
      }

      this.debugLogStatus(false, String.valueOf(currentTime));
      return false;
   }

   @NonNull
   @Override
   public String getIdentifier() {
      return "IRL-TIME";
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

   private int getDayMinute(@NonNull final String HHMMFormat, final int fallback) {
      String[] time = HHMMFormat.split(":");

      try {
         return Integer.parseInt(time[0]) * 60 + Integer.parseInt(time[1]);
      } catch (NumberFormatException | IndexOutOfBoundsException var5) {
         EvenMoreFish.getInstance().getLogger().severe("FATAL error reading " + HHMMFormat + ", resorting to default value of " + fallback);
         return fallback;
      }
   }
}
