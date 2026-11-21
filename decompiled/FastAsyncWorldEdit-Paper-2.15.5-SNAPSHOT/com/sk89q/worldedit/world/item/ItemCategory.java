package com.sk89q.worldedit.world.item;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.blocks.BaseItem;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.registry.Category;
import com.sk89q.worldedit.registry.Keyed;
import com.sk89q.worldedit.registry.NamespacedRegistry;
import java.util.Set;

public class ItemCategory extends Category<ItemType> implements Keyed {
   public static final NamespacedRegistry<ItemCategory> REGISTRY = new NamespacedRegistry<>("item tag", "item_tag", "minecraft");

   public ItemCategory(final String id) {
      super(id);
   }

   @Override
   protected Set<ItemType> load() {
      return WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.GAME_HOOKS).getRegistries().getItemCategoryRegistry().getAll(this);
   }

   public boolean contains(BaseItem baseItem) {
      return this.getAll().contains(baseItem.getType());
   }
}
