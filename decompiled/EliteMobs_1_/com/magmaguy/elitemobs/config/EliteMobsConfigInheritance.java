package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.magmacore.config.CustomConfigInheritancePolicy;
import java.util.Set;

public final class EliteMobsConfigInheritance {
   public static final CustomConfigInheritancePolicy POLICY = CustomConfigInheritancePolicy.excludingRoots(
      Set.of("spawnLocation", "spawnLocations", "isEnabled", "leashRadius", "powers", "eliteScript")
   );

   private EliteMobsConfigInheritance() {
   }
}
