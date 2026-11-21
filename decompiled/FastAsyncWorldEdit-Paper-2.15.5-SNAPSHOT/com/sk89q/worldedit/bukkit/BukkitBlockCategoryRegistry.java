package com.sk89q.worldedit.bukkit;

import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.registry.BlockCategoryRegistry;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;

public class BukkitBlockCategoryRegistry implements BlockCategoryRegistry {
   private Set<BlockType> getFromBukkitTag(Tag<Material> tag) {
      return tag.getValues().stream().map(BukkitAdapter::asBlockType).collect(Collectors.toSet());
   }

   @Override
   public Set<BlockType> getCategorisedByName(String category) {
      String[] split = category.split(":");
      String namespace = split.length > 1 ? split[0] : "minecraft";
      String key = split.length > 1 ? split[1] : category;
      Tag<Material> tag = Bukkit.getTag("blocks", new NamespacedKey(namespace, key), Material.class);
      return (Set<BlockType>)(tag == null ? new HashSet<>() : this.getFromBukkitTag(tag));
   }
}
