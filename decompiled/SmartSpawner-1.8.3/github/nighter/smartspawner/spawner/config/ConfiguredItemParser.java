package github.nighter.smartspawner.spawner.config;

import java.util.Base64;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public final class ConfiguredItemParser {
   private static final String NBT_PREFIX = "nbt:";

   private ConfiguredItemParser() {
   }

   public static ItemStack parse(String raw) {
      if (raw != null && !raw.isBlank()) {
         String value = raw.trim();
         if (value.regionMatches(true, 0, "nbt:", 0, "nbt:".length())) {
            return fromNbt(value.substring("nbt:".length()));
         } else if (value.indexOf(91) >= 0) {
            return fromComponents(value);
         } else {
            Material material = Material.matchMaterial(value);
            if (material != null) {
               if (!material.isItem()) {
                  throw new IllegalArgumentException("'" + value + "' is a block, not an item");
               } else {
                  return new ItemStack(material, 1);
               }
            } else if (value.indexOf(58) >= 0) {
               throw new IllegalArgumentException(
                  "'"
                     + value
                     + "' looks like an item from another plugin, which is not supported yet. Use the item's material name, the /give syntax, or an nbt: value instead"
               );
            } else {
               throw new IllegalArgumentException("'" + value + "' is not a material available on this server version");
            }
         }
      } else {
         throw new IllegalArgumentException("item is empty");
      }
   }

   private static ItemStack fromNbt(String base64) {
      byte[] bytes;
      try {
         bytes = Base64.getDecoder().decode(base64.trim());
      } catch (IllegalArgumentException var5) {
         throw new IllegalArgumentException("nbt value is not valid Base64", var5);
      }

      try {
         ItemStack item = ItemStack.deserializeBytes(bytes);
         if (item != null && item.getType() != Material.AIR) {
            return item.asQuantity(1);
         } else {
            throw new IllegalArgumentException("nbt value decoded to an empty item");
         }
      } catch (IllegalArgumentException var3) {
         throw var3;
      } catch (RuntimeException var4) {
         throw new IllegalArgumentException("nbt value could not be read as an item: " + var4.getMessage(), var4);
      }
   }

   private static ItemStack fromComponents(String value) {
      try {
         ItemStack item = Bukkit.getItemFactory().createItemStack(value);
         return item.asQuantity(1);
      } catch (IllegalArgumentException var2) {
         throw new IllegalArgumentException("invalid item syntax: " + var2.getMessage(), var2);
      }
   }

   public static String toNbtValue(ItemStack item) {
      return "nbt:" + Base64.getEncoder().encodeToString(item.asQuantity(1).serializeAsBytes());
   }
}
