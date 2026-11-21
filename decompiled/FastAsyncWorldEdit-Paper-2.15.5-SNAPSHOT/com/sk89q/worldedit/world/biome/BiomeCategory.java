package com.sk89q.worldedit.world.biome;

import com.sk89q.worldedit.registry.Category;
import com.sk89q.worldedit.registry.Keyed;
import com.sk89q.worldedit.registry.NamespacedRegistry;
import java.util.Set;
import java.util.function.Supplier;

public class BiomeCategory extends Category<BiomeType> implements Keyed {
   public static final NamespacedRegistry<BiomeCategory> REGISTRY = new NamespacedRegistry<>("biome tag", "biome_tag", "minecraft", true);

   public BiomeCategory(final String id) {
      super(id);
   }

   public BiomeCategory(final String id, final Supplier<Set<BiomeType>> contentSupplier) {
      super(id, contentSupplier);
   }

   @Override
   protected Set<BiomeType> load() {
      return Set.of();
   }
}
