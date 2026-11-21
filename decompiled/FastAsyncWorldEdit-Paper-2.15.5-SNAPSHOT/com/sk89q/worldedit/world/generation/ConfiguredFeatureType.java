package com.sk89q.worldedit.world.generation;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.registry.Keyed;
import com.sk89q.worldedit.registry.NamespacedRegistry;

public record ConfiguredFeatureType(String id, boolean place_on_face) implements Keyed {
   public static final NamespacedRegistry<ConfiguredFeatureType> REGISTRY = new NamespacedRegistry<>(
      "configured feature type", "configured_feature_type", "minecraft"
   );

   public ConfiguredFeatureType(String id) {
      this(id, false);
   }

   @Override
   public String toString() {
      return this.id;
   }

   public boolean place(EditSession extent, BlockVector3 position) {
      return extent.getWorld().generateFeature(this, extent, position);
   }
}
