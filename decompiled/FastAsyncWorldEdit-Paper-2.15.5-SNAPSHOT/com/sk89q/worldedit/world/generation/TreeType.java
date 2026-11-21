package com.sk89q.worldedit.world.generation;

import com.sk89q.worldedit.registry.Keyed;
import com.sk89q.worldedit.registry.NamespacedRegistry;

public record TreeType(String id) implements Keyed {
   public static final NamespacedRegistry<TreeType> REGISTRY = new NamespacedRegistry<>("tree_type", "minecraft");

   @Override
   public String toString() {
      return this.id;
   }
}
