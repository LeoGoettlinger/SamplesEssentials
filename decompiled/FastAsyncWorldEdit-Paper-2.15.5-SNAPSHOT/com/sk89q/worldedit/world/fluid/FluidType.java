package com.sk89q.worldedit.world.fluid;

import com.fastasyncworldedit.core.registry.RegistryItem;
import com.sk89q.worldedit.registry.Keyed;
import com.sk89q.worldedit.registry.NamespacedRegistry;

public class FluidType implements RegistryItem, Keyed {
   public static final NamespacedRegistry<FluidType> REGISTRY = new NamespacedRegistry<>("fluid type", "fluid_type", "minecraft");
   private final String id;
   private int internalId;

   public FluidType(String id) {
      this.id = id;
   }

   @Override
   public String id() {
      return this.id;
   }

   @Deprecated(
      forRemoval = true,
      since = "2.11.0"
   )
   @Override
   public String getId() {
      return this.id;
   }

   @Override
   public void setInternalId(int internalId) {
      this.internalId = internalId;
   }

   @Override
   public int getInternalId() {
      return this.internalId;
   }

   @Override
   public String toString() {
      return this.id();
   }

   @Override
   public int hashCode() {
      return this.id.hashCode();
   }

   @Override
   public boolean equals(Object obj) {
      return obj instanceof FluidType && this.id.equals(((FluidType)obj).id);
   }
}
