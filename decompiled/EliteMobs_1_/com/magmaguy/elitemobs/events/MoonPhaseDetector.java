package com.magmaguy.elitemobs.events;

import com.magmaguy.elitemobs.magmacore.util.Logger;
import org.bukkit.World;

public class MoonPhaseDetector {
   public static MoonPhaseDetector.MoonPhase detectMoonPhase(World eventWorld) {
      int days = (int)eventWorld.getFullTime() / 24000;
      int phase = Math.abs(days % 8);
      switch (phase) {
         case 0:
            return MoonPhaseDetector.MoonPhase.FULL_MOON;
         case 1:
            return MoonPhaseDetector.MoonPhase.WANING_GIBBOUS;
         case 2:
            return MoonPhaseDetector.MoonPhase.LAST_QUARTER;
         case 3:
            return MoonPhaseDetector.MoonPhase.WANING_CRESCENT;
         case 4:
            return MoonPhaseDetector.MoonPhase.NEW_MOON;
         case 5:
            return MoonPhaseDetector.MoonPhase.WAXING_CRESCENT;
         case 6:
            return MoonPhaseDetector.MoonPhase.FIRST_QUARTER;
         case 7:
            return MoonPhaseDetector.MoonPhase.WAXING_GIBBOUS;
         default:
            Logger.info("Unhandled moon phase. Phase " + phase + " was " + days + ". Defaulting to full moon...");
            return MoonPhaseDetector.MoonPhase.FULL_MOON;
      }
   }

   public static enum MoonPhase {
      FULL_MOON,
      WANING_GIBBOUS,
      LAST_QUARTER,
      WANING_CRESCENT,
      NEW_MOON,
      WAXING_CRESCENT,
      FIRST_QUARTER,
      WAXING_GIBBOUS;
   }
}
