package com.oheers.fish.utils;

import com.oheers.fish.FishUtils;
import org.bukkit.Material;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class ItemUtils {
   @NonNull
   public static Material getMaterial(@Nullable String materialName, @NonNull Material defaultMaterial) {
      Material material = getMaterial(materialName);
      return material == null ? defaultMaterial : material;
   }

   @Nullable
   public static Material getMaterial(@Nullable String materialName) {
      return materialName == null ? null : FishUtils.getEnumValue(Material.class, materialName);
   }

   public static boolean isValidMaterial(@Nullable String materialName) {
      return materialName != null && !materialName.isEmpty() ? getMaterial(materialName) != null : false;
   }
}
