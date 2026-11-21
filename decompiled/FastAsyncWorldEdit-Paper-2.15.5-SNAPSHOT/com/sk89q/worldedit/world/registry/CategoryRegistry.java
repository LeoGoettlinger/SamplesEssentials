package com.sk89q.worldedit.world.registry;

import com.sk89q.worldedit.registry.Category;
import com.sk89q.worldedit.registry.Keyed;
import java.util.Set;

public interface CategoryRegistry<T extends Keyed> {
   Set<T> getCategorisedByName(String category);

   default Set<T> getAll(final Category<T> category) {
      return this.getCategorisedByName(category.id());
   }
}
