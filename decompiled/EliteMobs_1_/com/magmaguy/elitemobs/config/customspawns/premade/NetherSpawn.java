package com.magmaguy.elitemobs.config.customspawns.premade;

import com.magmaguy.elitemobs.config.customspawns.CustomSpawnConfigFields;
import java.util.List;
import org.bukkit.World.Environment;

public class NetherSpawn extends CustomSpawnConfigFields {
   public NetherSpawn() {
      super("nether_spawn", true);
      this.setCanSpawnInLight(true);
      this.setValidWorldEnvironments(List.of(Environment.NETHER));
      this.setUndergroundSpawn(true);
   }
}
