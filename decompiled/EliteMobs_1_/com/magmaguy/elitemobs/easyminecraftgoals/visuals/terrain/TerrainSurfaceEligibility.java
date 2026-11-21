package com.magmaguy.elitemobs.easyminecraftgoals.visuals.terrain;

import java.util.Objects;

final class TerrainSurfaceEligibility {
   private TerrainSurfaceEligibility() {
   }

   static boolean isEligibleSurface(TerrainSurfaceEligibility.MaterialTraits material) {
      Objects.requireNonNull(material, "material");
      return !material.air() && !material.liquid() && material.solid() && material.occluding() && !material.interactable() && !material.tileState();
   }

   static boolean hasClearance(TerrainSurfaceEligibility.MaterialTraits material) {
      Objects.requireNonNull(material, "material");
      return material.air() && material.passable();
   }

   record MaterialTraits(boolean air, boolean liquid, boolean solid, boolean occluding, boolean interactable, boolean tileState, boolean passable) {
   }
}
