package com.magmaguy.elitemobs.config.customspawns.premade;

import com.magmaguy.elitemobs.config.customspawns.CustomSpawnConfigFields;
import com.magmaguy.elitemobs.events.MoonPhaseDetector;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.World.Environment;

public class FullMoonSpawn extends CustomSpawnConfigFields {
   public FullMoonSpawn() {
      super("full_moon_spawn", true);
      this.setSurfaceSpawn(true);

      try {
         this.setValidWorldEnvironments(new ArrayList<>(new ArrayList<>(List.of(Environment.NORMAL, Environment.CUSTOM))));
      } catch (NoSuchFieldError var2) {
         this.setValidWorldEnvironments(new ArrayList<>(List.of(Environment.NORMAL)));
      }

      this.setEarliestTime(12000L);
      this.setMoonPhase(MoonPhaseDetector.MoonPhase.FULL_MOON);
   }
}
