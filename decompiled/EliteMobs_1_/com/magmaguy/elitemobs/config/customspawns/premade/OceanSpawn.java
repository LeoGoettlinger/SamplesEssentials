package com.magmaguy.elitemobs.config.customspawns.premade;

import com.magmaguy.elitemobs.config.customspawns.CustomSpawnConfigFields;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.World.Environment;

public class OceanSpawn extends CustomSpawnConfigFields {
   public OceanSpawn() {
      super("ocean_spawn", true);
      this.setValidWorldEnvironments(List.of(Environment.NORMAL, Environment.CUSTOM));
      this.setValidBiomesStrings(
         new ArrayList<>(
            List.of("OCEAN", "COLD_OCEAN", "DEEP_OCEAN", "FROZEN_OCEAN", "LUKEWARM_OCEAN", "DEEP_FROZEN_OCEAN", "DEEP_LUKEWARM_OCEAN", "DEEP_OCEAN")
         )
      );
      this.setSurfaceSpawn(true);
   }
}
