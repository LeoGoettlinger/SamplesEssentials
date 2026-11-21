package com.magmaguy.elitemobs.config.customspawns.premade;

import com.magmaguy.elitemobs.config.customspawns.CustomSpawnConfigFields;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.World.Environment;

public class NormalUndergroundSpawn extends CustomSpawnConfigFields {
   public NormalUndergroundSpawn() {
      super("normal_underground_spawn", true);
      this.setSurfaceSpawn(false);
      this.setCanSpawnInLight(false);

      try {
         this.setValidWorldEnvironments(new ArrayList<>(new ArrayList<>(List.of(Environment.NORMAL, Environment.CUSTOM))));
      } catch (NoSuchFieldError var2) {
         this.setValidWorldEnvironments(new ArrayList<>(List.of(Environment.NORMAL)));
      }

      this.setBypassWorldGuard(false);
   }
}
