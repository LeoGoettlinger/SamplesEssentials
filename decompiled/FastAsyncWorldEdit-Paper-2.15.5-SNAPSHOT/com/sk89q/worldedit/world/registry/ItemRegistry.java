package com.sk89q.worldedit.world.registry;

import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.world.item.ItemType;
import java.util.Collection;
import java.util.Collections;
import javax.annotation.Nullable;

public interface ItemRegistry {
   Component getRichName(ItemType itemType);

   default Component getRichName(BaseItemStack itemStack) {
      return this.getRichName(itemStack.getType());
   }

   @Deprecated
   @Nullable
   default String getName(ItemType itemType) {
      return this.getRichName(itemType).toString();
   }

   @Nullable
   ItemMaterial getMaterial(ItemType itemType);

   default Collection<String> values() {
      return Collections.emptyList();
   }
}
