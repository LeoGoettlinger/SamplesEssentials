package org.lushplugins.lushrewards.libraries.lushlib.registry;

import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.Fluid;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Tag;
import org.bukkit.entity.EntityType;

public record TagType<T extends Keyed>(Registry<T> registry, String[] registries, Class<T> dataType) {
   private static final HashMap<Registry<?>, TagType<?>> TAG_TYPES = new HashMap<>();
   public static final TagType<EntityType> ENTITY_TYPES = new TagType(Registry.ENTITY_TYPE, new String[]{"entity_types"}, (Class<T>)EntityType.class);
   public static final TagType<Fluid> FLUIDS = new TagType(Registry.FLUID, new String[]{"fluids"}, (Class<T>)Fluid.class);
   public static final TagType<Material> MATERIALS = new TagType(Registry.MATERIAL, new String[]{"blocks", "items"}, (Class<T>)Material.class);

   public static <T extends Keyed> TagType<T> get(Registry<T> registry) {
      TagType<?> tagType = TAG_TYPES.get(registry);
      return (TagType<T>)(tagType != null ? tagType : null);
   }

   public static <T extends Keyed> Tag<T> getTag(NamespacedKey namespacedKey, Registry<T> registry) {
      TagType<T> tagType = get(registry);
      return tagType != null ? tagType.getTag(namespacedKey) : null;
   }

   public Tag<T> getTag(NamespacedKey namespacedKey) {
      for (String tagRegistry : this.registries()) {
         Tag<T> tag = Bukkit.getTag(tagRegistry, namespacedKey, this.dataType());
         if (tag != null) {
            return tag;
         }
      }

      return null;
   }

   static {
      TAG_TYPES.put(Registry.ENTITY_TYPE, ENTITY_TYPES);
      TAG_TYPES.put(Registry.FLUID, FLUIDS);
      TAG_TYPES.put(Registry.MATERIAL, MATERIALS);
   }
}
