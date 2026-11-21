package com.sk89q.worldedit.world.fluid;

import com.sk89q.worldedit.registry.Category;
import com.sk89q.worldedit.registry.Keyed;
import com.sk89q.worldedit.registry.NamespacedRegistry;
import java.util.Collections;
import java.util.Set;

public class FluidCategory extends Category<FluidType> implements Keyed {
   public static final NamespacedRegistry<FluidCategory> REGISTRY = new NamespacedRegistry<>("fluid tag", "fluid_tag", "minecraft");

   public FluidCategory(final String id) {
      super(id);
   }

   @Override
   protected Set<FluidType> load() {
      return Collections.emptySet();
   }
}
