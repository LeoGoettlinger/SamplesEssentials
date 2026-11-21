package me.chancesd.pvpmanager.setting;

import me.chancesd.pvpmanager.sdutils.utils.MCVersion;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public record ItemKey(Material material, int data) {
   public static final int WILDCARD_DATA = -1;

   public static ItemKey of(Material material, int data) {
      return new ItemKey(material, data);
   }

   public static ItemKey wildcard(Material material) {
      return new ItemKey(material, -1);
   }

   public static ItemKey fromString(String raw) {
      if (raw == null) {
         return null;
      } else {
         String s = raw.trim().toUpperCase();
         if ("ENCHANTED_GOLDEN_APPLE".equals(s) && Material.getMaterial("ENCHANTED_GOLDEN_APPLE") == null) {
            Material legacy = Material.getMaterial("GOLDEN_APPLE");
            return legacy == null ? null : new ItemKey(legacy, 1);
         } else if (s.contains("-")) {
            String[] parts = s.split("-", 2);
            String name = parts[0];
            int data = -1;

            try {
               data = Integer.parseInt(parts[1]);
            } catch (Exception var6) {
               data = -1;
            }

            Material m = Material.getMaterial(name);
            return m == null ? null : new ItemKey(m, data);
         } else {
            Material m = Material.getMaterial(s);
            return m == null ? null : new ItemKey(m, -1);
         }
      }
   }

   public static ItemKey fromItemStack(ItemStack item) {
      if (item == null) {
         return null;
      } else {
         Material m = item.getType();
         if (MCVersion.isLowerThan(MCVersion.V1_13)) {
            short dmg = item.getDurability();
            return of(m, dmg);
         } else {
            return wildcard(m);
         }
      }
   }
}
