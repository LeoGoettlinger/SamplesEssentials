package com.magmaguy.elitemobs.config.customspawns.premade;

import com.magmaguy.elitemobs.config.customspawns.CustomSpawnConfigFields;
import java.util.List;
import org.bukkit.World.Environment;

public class TheEndSurfaceSpawn extends CustomSpawnConfigFields {
   public TheEndSurfaceSpawn() {
      super("the_end_surface_spawn", true);
      this.setSurfaceSpawn(true);
      this.setCanSpawnInLight(true);
      this.setValidWorldEnvironments(List.of(Environment.THE_END));
   }
}
