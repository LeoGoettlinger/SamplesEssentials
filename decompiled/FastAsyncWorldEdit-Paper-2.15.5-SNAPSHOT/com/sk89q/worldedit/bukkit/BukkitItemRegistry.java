package com.sk89q.worldedit.bukkit;

import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.registry.BundledItemRegistry;
import java.util.ArrayList;
import java.util.Collection;
import org.bukkit.Material;

class BukkitItemRegistry extends BundledItemRegistry {
   @Override
   public Component getRichName(ItemType itemType) {
      return WorldEditPlugin.getInstance().getBukkitImplAdapter() != null
         ? WorldEditPlugin.getInstance().getBukkitImplAdapter().getRichItemName(itemType)
         : super.getRichName(itemType);
   }

   @Override
   public Component getRichName(BaseItemStack itemStack) {
      return WorldEditPlugin.getInstance().getBukkitImplAdapter() != null
         ? WorldEditPlugin.getInstance().getBukkitImplAdapter().getRichItemName(itemStack)
         : super.getRichName(itemStack);
   }

   @Override
   public Collection<String> values() {
      ArrayList<String> values = new ArrayList<>();

      for (Material m : Material.values()) {
         if (!m.isLegacy() && (m.isBlock() || m.isItem())) {
            String id = m.getKey().toString();
            values.add(id);
         }
      }

      return values;
   }
}
