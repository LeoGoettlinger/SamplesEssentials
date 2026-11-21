package com.magmaguy.elitemobs.config.customspawns.premade;

import com.magmaguy.elitemobs.config.customspawns.CustomSpawnConfigFields;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.World.Environment;

public class NormalSurfaceSpawn extends CustomSpawnConfigFields {
   public NormalSurfaceSpawn() {
      super("normal_surface_spawn", true);
      this.setSurfaceSpawn(true);
      this.setCanSpawnInLight(true);

      try {
         this.setValidWorldEnvironments(new ArrayList<>(new ArrayList<>(List.of(Environment.NORMAL, Environment.CUSTOM))));
      } catch (NoSuchFieldError var2) {
         this.setValidWorldEnvironments(new ArrayList<>(List.of(Environment.NORMAL)));
      }

      this.setBypassWorldGuard(false);
   }
}
