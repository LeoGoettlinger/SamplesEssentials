package com.magmaguy.elitemobs.magmacore.visuals.terrain;

import org.bukkit.Location;

public final class TerrainImpacts {
   private TerrainImpacts() {
   }

   public static TerrainImpactHandle crack(Location center) {
      return center == null ? TerrainImpactHandle.inactive() : show(new TerrainImpactRequest(center));
   }

   public static TerrainImpactHandle show(TerrainImpactRequest request) {
      return TerrainImpactService.show(request);
   }
}
