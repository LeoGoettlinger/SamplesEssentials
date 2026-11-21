package com.skyblockexp.ezrtp.util.compat;

import java.util.Locale;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.Biome;

public final class BiomeCompat {
   private BiomeCompat() {
   }

   public static String safeName(Biome biome) {
      try {
         if (biome.getClass().getMethod("name").invoke(biome) instanceof String s) {
            return s;
         }
      } catch (SecurityException | ReflectiveOperationException var3) {
      }

      return biome.toString();
   }

   public static Biome safeValueOf(String name) {
      return name != null && !name.isBlank() ? (Biome)Registry.BIOME.get(NamespacedKey.minecraft(name.toLowerCase(Locale.ROOT))) : null;
   }
}
