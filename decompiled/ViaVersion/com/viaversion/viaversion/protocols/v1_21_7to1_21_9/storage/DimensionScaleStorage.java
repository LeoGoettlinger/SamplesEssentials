package com.viaversion.viaversion.protocols.v1_21_7to1_21_9.storage;

import java.util.HashMap;
import java.util.Map;

public final class DimensionScaleStorage {
   private final Map<Integer, Double> dimensionScales = new HashMap<>(4);

   public double getScale(int dimensionId) {
      return this.dimensionScales.getOrDefault(dimensionId, 1.0);
   }

   public void setScale(int dimensionId, double scale) {
      this.dimensionScales.put(dimensionId, scale);
   }
}
