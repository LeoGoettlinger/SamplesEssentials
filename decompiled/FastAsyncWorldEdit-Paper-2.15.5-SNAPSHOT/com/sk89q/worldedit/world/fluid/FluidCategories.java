package com.sk89q.worldedit.world.fluid;

import javax.annotation.Nullable;

public final class FluidCategories {
   public static final FluidCategory LAVA = register("minecraft:lava");
   public static final FluidCategory WATER = register("minecraft:water");

   private FluidCategories() {
   }

   private static FluidCategory register(final String id) {
      return register(new FluidCategory(id));
   }

   public static FluidCategory register(final FluidCategory tag) {
      return FluidCategory.REGISTRY.register(tag.id(), tag);
   }

   @Nullable
   public static FluidCategory get(final String id) {
      return FluidCategory.REGISTRY.get(id);
   }
}
