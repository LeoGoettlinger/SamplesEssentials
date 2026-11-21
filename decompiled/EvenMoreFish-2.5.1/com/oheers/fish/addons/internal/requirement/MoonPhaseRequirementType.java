package com.oheers.fish.addons.internal.requirement;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.requirement.RequirementContext;
import com.oheers.fish.api.requirement.RequirementType;
import java.util.List;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;

public class MoonPhaseRequirementType extends RequirementType {
   @Override
   public boolean checkRequirement(@NonNull RequirementContext context, @NonNull List<String> values) {
      World world = context.getWorld();
      if (world == null) {
         return false;
      } else {
         int phaseId = (int)(world.getFullTime() / 24000L) % 8;

         for (String value : values) {
            MoonPhaseRequirementType.MoonPhase phase = FishUtils.getEnumValue(MoonPhaseRequirementType.MoonPhase.class, value);
            if (phase == null) {
               EvenMoreFish.getInstance().getLogger().severe(value + " is not a valid moon phase.");
               return false;
            }

            if (phase.getPhaseID() == phaseId) {
               this.debugLogStatus(true, phase.toString());
               return true;
            }
         }

         this.debugLogStatus(false, String.valueOf(phaseId));
         return false;
      }
   }

   @NonNull
   @Override
   public String getIdentifier() {
      return "MOON-PHASE";
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

   static enum MoonPhase {
      FULL_MOON(0),
      WANING_GIBBOUS(1),
      LAST_QUARTER(2),
      WANING_CRESCENT(3),
      NEW_MOON(4),
      WAXING_CRESCENT(5),
      FIRST_QUARTER(6),
      WAXING_GIBBOUS(7);

      final int phaseID;

      private MoonPhase(int phaseID) {
         this.phaseID = phaseID;
      }

      public int getPhaseID() {
         return this.phaseID;
      }
   }
}
