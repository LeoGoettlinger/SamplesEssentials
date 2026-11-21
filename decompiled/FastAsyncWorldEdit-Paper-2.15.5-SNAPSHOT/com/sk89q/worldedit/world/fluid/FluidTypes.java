package com.sk89q.worldedit.world.fluid;

import javax.annotation.Nullable;

public final class FluidTypes {
   public static final FluidType EMPTY = register("minecraft:empty");
   public static final FluidType FLOWING_LAVA = register("minecraft:flowing_lava");
   public static final FluidType FLOWING_WATER = register("minecraft:flowing_water");
   public static final FluidType LAVA = register("minecraft:lava");
   public static final FluidType WATER = register("minecraft:water");

   private FluidTypes() {
   }

   private static FluidType register(final String id) {
      return register(new FluidType(id));
   }

   public static FluidType register(final FluidType fluid) {
      return FluidType.REGISTRY.register(fluid.id(), fluid);
   }

   @Nullable
   public static FluidType get(final String id) {
      return FluidType.REGISTRY.get(id);
   }
}
