package net.ess3.provider;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public interface SpawnerItemProvider extends Provider {
   Map<EntityType, String> entityToDisplayName = Stream.of(
         "CAVE_SPIDER:Cave Spider",
         "PIG_ZOMBIE:Zombie Pigman",
         "ZOMBIFIED_PIGLIN:Zombie Piglin",
         "MAGMA_CUBE:Magma Cube",
         "ENDER_DRAGON:Ender Dragon",
         "MUSHROOM_COW:Mooshroom",
         "SNOWMAN:Snow Golem",
         "OCELOT:Ocelot",
         "IRON_GOLEM:Iron Golem",
         "WITHER:Wither",
         "HORSE:Horse"
      )
      .filter(s -> {
         try {
            EntityType.valueOf(s);
            return true;
         } catch (IllegalArgumentException var2) {
            return false;
         }
      })
      .collect(Collectors.toMap(s -> EntityType.valueOf(s.split(":")[0]), s -> s.split(":")[1]));

   ItemStack setEntityType(ItemStack is, EntityType type) throws IllegalArgumentException;

   EntityType getEntityType(ItemStack is) throws IllegalArgumentException;

   default ItemStack setDisplayName(final ItemStack is, final EntityType type) {
      ItemMeta meta = is.getItemMeta();
      String displayName;
      if (entityToDisplayName.containsKey(type)) {
         displayName = entityToDisplayName.get(type);
      } else {
         displayName = type.getName();
      }

      meta.setDisplayName(ChatColor.RESET + displayName + " Spawner");
      is.setItemMeta(meta);
      return is;
   }
}
