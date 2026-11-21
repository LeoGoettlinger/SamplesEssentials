package com.magmaguy.elitemobs.magmacore.visuals.terrain;

import java.util.UUID;

final class InactiveTerrainImpactHandle implements TerrainImpactHandle {
   static final InactiveTerrainImpactHandle INSTANCE = new InactiveTerrainImpactHandle();
   private static final UUID ID = new UUID(0L, 0L);

   private InactiveTerrainImpactHandle() {
   }

   @Override
   public UUID id() {
      return ID;
   }

   @Override
   public boolean active() {
      return false;
   }

   @Override
   public void close() {
   }
}
