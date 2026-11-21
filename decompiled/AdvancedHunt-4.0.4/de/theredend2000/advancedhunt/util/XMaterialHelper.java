package de.theredend2000.advancedhunt.util;

import de.theredend2000.advancedhunt.lib.xseries.XMaterial;
import java.util.Optional;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class XMaterialHelper {
   public static ItemStack getItemStack(XMaterial material) {
      if (material == null) {
         return null;
      } else {
         if (material.name().endsWith("_WALL")) {
            String baseName = material.name().replaceFirst("_WALL$", "");

            try {
               XMaterial baseMaterial = XMaterial.valueOf(baseName);
               ItemStack item = baseMaterial.parseItem();
               if (item != null) {
                  return item;
               }
            } catch (IllegalArgumentException var4) {
            }
         }

         return material.parseItem();
      }
   }

   public static ItemStack getItemStack(String materialName) {
      return getItemStack(materialName, null);
   }

   public static ItemStack getItemStack(String materialName, String blockState) {
      if (materialName != null && !materialName.trim().isEmpty()) {
         String headMaterialName = HeadHelper.resolveHeadDisplayMaterialName(materialName, blockState);
         if (headMaterialName != null) {
            Optional<XMaterial> resolvedHeadMaterial = XMaterial.matchXMaterial(headMaterialName);
            if (resolvedHeadMaterial.isPresent()) {
               ItemStack item = getItemStack(resolvedHeadMaterial.get());
               if (item != null) {
                  return item;
               }
            }
         }

         Material material = Material.matchMaterial(materialName);
         if (material != null) {
            return getItemStack(XMaterial.matchXMaterial(material));
         } else {
            Optional<XMaterial> xMaterial = XMaterial.matchXMaterial(materialName);
            return xMaterial.isPresent() ? getItemStack(xMaterial.get()) : null;
         }
      } else {
         return null;
      }
   }
}
