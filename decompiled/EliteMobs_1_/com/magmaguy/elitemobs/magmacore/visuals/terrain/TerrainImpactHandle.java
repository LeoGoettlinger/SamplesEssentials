package com.magmaguy.elitemobs.magmacore.visuals.terrain;

import java.util.UUID;

public interface TerrainImpactHandle extends AutoCloseable {
   UUID id();

   boolean active();

   @Override
   void close();

   static TerrainImpactHandle inactive() {
      return InactiveTerrainImpactHandle.INSTANCE;
   }
}
