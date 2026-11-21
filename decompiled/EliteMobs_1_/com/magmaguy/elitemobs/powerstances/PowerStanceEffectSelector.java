package com.magmaguy.elitemobs.powerstances;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.powers.meta.ElitePower;
import java.util.ArrayList;
import java.util.List;

public final class PowerStanceEffectSelector {
   private PowerStanceEffectSelector() {
   }

   public static boolean isMajorPower(ElitePower elitePower) {
      return elitePower == null ? false : PowersConfigFields.isMajorPowerType(elitePower.getPowerType());
   }

   public static List<String> selectTrails(EliteEntity eliteEntity, boolean majorPowers) {
      List<String> trails = new ArrayList<>();
      if (eliteEntity == null) {
         return trails;
      } else {
         for (ElitePower elitePower : eliteEntity.getElitePowers()) {
            if (elitePower != null && isMajorPower(elitePower) == majorPowers) {
               String trail = elitePower.getTrail();
               if (trail != null && !trail.isEmpty()) {
                  trails.add(trail);
               }
            }
         }

         return trails;
      }
   }
}
