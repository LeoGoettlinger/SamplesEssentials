package com.sk89q.worldedit.world.generation;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.registry.Keyed;
import com.sk89q.worldedit.registry.NamespacedRegistry;

public record StructureType(String id) implements Keyed {
   public static final NamespacedRegistry<StructureType> REGISTRY = new NamespacedRegistry<>("structure type", "structure_type", "minecraft");

   @Override
   public String toString() {
      return this.id;
   }

   public boolean place(EditSession extent, BlockVector3 position) {
      return extent.getWorld().generateStructure(this, extent, position);
   }
}
