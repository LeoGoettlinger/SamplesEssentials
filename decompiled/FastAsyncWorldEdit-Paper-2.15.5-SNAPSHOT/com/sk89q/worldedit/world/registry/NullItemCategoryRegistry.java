package com.sk89q.worldedit.world.registry;

import com.sk89q.worldedit.world.item.ItemType;
import java.util.Collections;
import java.util.Set;

public class NullItemCategoryRegistry implements ItemCategoryRegistry {
   @Override
   public Set<ItemType> getCategorisedByName(String category) {
      return Collections.emptySet();
   }
}
