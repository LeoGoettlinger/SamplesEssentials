package com.magmaguy.elitemobs.magmacore.visuals.terrain;

public interface TerrainImpactRenderer extends AutoCloseable {
   TerrainImpactHandle show(TerrainImpactRequest var1);

   @Override
   default void close() {
   }
}
