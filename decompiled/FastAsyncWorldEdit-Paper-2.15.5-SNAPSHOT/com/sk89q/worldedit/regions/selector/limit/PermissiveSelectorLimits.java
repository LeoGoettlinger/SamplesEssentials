package com.sk89q.worldedit.regions.selector.limit;

import java.util.Optional;

public class PermissiveSelectorLimits implements SelectorLimits {
   private static final PermissiveSelectorLimits INSTANCE = new PermissiveSelectorLimits();

   private PermissiveSelectorLimits() {
   }

   @Override
   public Optional<Integer> getPolygonVertexLimit() {
      return Optional.empty();
   }

   @Override
   public Optional<Integer> getPolyhedronVertexLimit() {
      return Optional.empty();
   }

   public static PermissiveSelectorLimits getInstance() {
      return INSTANCE;
   }
}
