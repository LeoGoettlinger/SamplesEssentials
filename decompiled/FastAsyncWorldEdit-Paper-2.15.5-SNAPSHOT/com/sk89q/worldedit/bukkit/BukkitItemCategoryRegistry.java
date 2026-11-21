package com.sk89q.worldedit.bukkit;

import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.registry.ItemCategoryRegistry;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;

public class BukkitItemCategoryRegistry implements ItemCategoryRegistry {
   private Set<ItemType> getFromBukkitTag(Tag<Material> tag) {
      return tag.getValues().stream().map(BukkitAdapter::asItemType).collect(Collectors.toSet());
   }

   @Override
   public Set<ItemType> getCategorisedByName(String category) {
      String[] split = category.split(":");
      String namespace = split.length > 1 ? split[0] : "minecraft";
      String key = split.length > 1 ? split[1] : category;
      Tag<Material> tag = Bukkit.getTag("items", new NamespacedKey(namespace, key), Material.class);
      return this.getFromBukkitTag(tag);
   }
}
