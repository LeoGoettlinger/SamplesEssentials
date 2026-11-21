package com.magmaguy.elitemobs.config.customspawns.premade;

import com.magmaguy.elitemobs.config.customspawns.CustomSpawnConfigFields;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.World.Environment;

public class NighttimeSurfaceSpawn extends CustomSpawnConfigFields {
   public NighttimeSurfaceSpawn() {
      super("nighttime_surface_spawn", true);
      this.setSurfaceSpawn(true);

      try {
         this.setValidWorldEnvironments(new ArrayList<>(new ArrayList<>(List.of(Environment.NORMAL, Environment.CUSTOM))));
      } catch (NoSuchFieldError var2) {
         this.setValidWorldEnvironments(new ArrayList<>(List.of(Environment.NORMAL)));
      }

      this.setEarliestTime(12000L);
      this.setBypassWorldGuard(false);
   }
}
