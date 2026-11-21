package com.magmaguy.elitemobs.items.itemconstructor;

import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.config.ProceduralItemGenerationSettingsConfig;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.versionnotifier.VersionChecker;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Material;

public class MaterialGenerator {
   private static final ArrayList<Material> validProceduralMaterials = new ArrayList<>();

   public static Material generateMaterial(Material material) {
      return material;
   }

   public static Material generateRandomMaterial() {
      if (validProceduralMaterials.isEmpty()) {
         initializeValidProceduralMaterials();
      }

      return validProceduralMaterials.isEmpty() ? null : validProceduralMaterials.get(ThreadLocalRandom.current().nextInt(validProceduralMaterials.size()));
   }

   public static Material generateMaterial(double itemTier) {
      List<Material> materials = materialsForTier(itemTier);
      return materials.isEmpty() ? null : materials.get(ThreadLocalRandom.current().nextInt(materials.size()));
   }

   public static ProceduralItemType generateItemType(double itemTier) {
      return chooseItemType(materialsForTier(itemTier));
   }

   public static ProceduralItemType generateRandomItemType() {
      if (validProceduralMaterials.isEmpty()) {
         initializeValidProceduralMaterials();
      }

      return chooseItemType(validProceduralMaterials);
   }

   private static ProceduralItemType chooseItemType(List<Material> materials) {
      List<ProceduralItemType> types = new ArrayList<>(materials.size() + 2);

      for (Material material : materials) {
         types.add(ProceduralItemType.vanilla(material));
      }

      if (ProceduralItemType.STAFF.isAvailable()) {
         types.add(ProceduralItemType.STAFF);
      }

      if (ProceduralItemType.WAND.isAvailable()) {
         types.add(ProceduralItemType.WAND);
      }

      return types.isEmpty() ? null : types.get(ThreadLocalRandom.current().nextInt(types.size()));
   }

   private static List<Material> materialsForTier(double itemTier) {
      if (validProceduralMaterials.isEmpty()) {
         initializeValidProceduralMaterials();
      }

      List<Material> localValidMaterials = new ArrayList<>(validProceduralMaterials);
      if (itemTier < 7 + ItemSettingsConfig.getMinimumProcedurallyGeneratedDiamondLootLevelPlusSeven()) {
         localValidMaterials.remove(Material.TRIDENT);
      }

      if (VersionChecker.serverVersionOlderThan(21, 11)) {
         try {
            localValidMaterials.remove(Material.DIAMOND_SPEAR);
            localValidMaterials.remove(Material.IRON_SPEAR);
            localValidMaterials.remove(Material.GOLDEN_SPEAR);
            localValidMaterials.remove(Material.STONE_SPEAR);
            localValidMaterials.remove(Material.WOODEN_SPEAR);
            localValidMaterials.remove(Material.COPPER_SPEAR);
            localValidMaterials.remove(Material.NETHERITE_SPEAR);
         } catch (NoSuchFieldError var8) {
         }
      }

      if (itemTier < 7 + ItemSettingsConfig.getMinimumProcedurallyGeneratedDiamondLootLevelPlusSeven()) {
         localValidMaterials.remove(Material.DIAMOND_AXE);
         localValidMaterials.remove(Material.DIAMOND_HORSE_ARMOR);
         localValidMaterials.remove(Material.DIAMOND_CHESTPLATE);
         localValidMaterials.remove(Material.DIAMOND_HELMET);
         localValidMaterials.remove(Material.DIAMOND_HOE);
         localValidMaterials.remove(Material.DIAMOND_LEGGINGS);
         localValidMaterials.remove(Material.DIAMOND_PICKAXE);
         localValidMaterials.remove(Material.DIAMOND_SHOVEL);
         localValidMaterials.remove(Material.DIAMOND_SWORD);
         localValidMaterials.remove(Material.DIAMOND_BOOTS);

         try {
            localValidMaterials.remove(Material.MACE);
            localValidMaterials.remove(Material.DIAMOND_SPEAR);
            localValidMaterials.remove(Material.NETHERITE_SPEAR);
         } catch (NoSuchFieldError var7) {
         }
      }

      if (itemTier < 6.0) {
         localValidMaterials.remove(Material.IRON_AXE);
         localValidMaterials.remove(Material.IRON_HORSE_ARMOR);
         localValidMaterials.remove(Material.IRON_BOOTS);
         localValidMaterials.remove(Material.IRON_CHESTPLATE);
         localValidMaterials.remove(Material.IRON_HELMET);
         localValidMaterials.remove(Material.IRON_HOE);
         localValidMaterials.remove(Material.IRON_LEGGINGS);
         localValidMaterials.remove(Material.IRON_PICKAXE);
         localValidMaterials.remove(Material.IRON_SHOVEL);
         localValidMaterials.remove(Material.IRON_SWORD);
         localValidMaterials.remove(Material.BOW);
         localValidMaterials.remove(Material.CROSSBOW);
         localValidMaterials.remove(Material.TURTLE_HELMET);

         try {
            localValidMaterials.remove(Material.IRON_SPEAR);
            localValidMaterials.remove(Material.COPPER_SPEAR);
         } catch (NoSuchFieldError var6) {
         }
      }

      if (itemTier < 5.0) {
         localValidMaterials.remove(Material.CHAINMAIL_BOOTS);
         localValidMaterials.remove(Material.CHAINMAIL_CHESTPLATE);
         localValidMaterials.remove(Material.CHAINMAIL_HELMET);
         localValidMaterials.remove(Material.CHAINMAIL_LEGGINGS);
         localValidMaterials.remove(Material.STONE_SWORD);
         localValidMaterials.remove(Material.STONE_HOE);
         localValidMaterials.remove(Material.STONE_SHOVEL);
         localValidMaterials.remove(Material.STONE_PICKAXE);
         localValidMaterials.remove(Material.STONE_AXE);

         try {
            localValidMaterials.remove(Material.STONE_SPEAR);
         } catch (NoSuchFieldError var5) {
         }
      }

      if (itemTier < 3.0) {
         localValidMaterials.remove(Material.GOLDEN_BOOTS);
         localValidMaterials.remove(Material.GOLDEN_CHESTPLATE);
         localValidMaterials.remove(Material.GOLDEN_HELMET);
         localValidMaterials.remove(Material.GOLDEN_LEGGINGS);
         localValidMaterials.remove(Material.GOLDEN_SWORD);
         localValidMaterials.remove(Material.GOLDEN_HOE);
         localValidMaterials.remove(Material.GOLDEN_SHOVEL);
         localValidMaterials.remove(Material.GOLDEN_PICKAXE);
         localValidMaterials.remove(Material.GOLDEN_AXE);
         localValidMaterials.remove(Material.WOODEN_SWORD);
         localValidMaterials.remove(Material.WOODEN_AXE);
         localValidMaterials.remove(Material.WOODEN_HOE);

         try {
            localValidMaterials.remove(Material.GOLDEN_SPEAR);
            localValidMaterials.remove(Material.WOODEN_SPEAR);
         } catch (NoSuchFieldError var4) {
         }
      }

      return localValidMaterials;
   }

   public static void initializeValidProceduralMaterials() {
      validProceduralMaterials.clear();
      if (ProceduralItemGenerationSettingsConfig.getValidMaterials().isEmpty()) {
         ProceduralItemGenerationSettingsConfig.getInstance().cacheMaterials();
         if (ProceduralItemGenerationSettingsConfig.getValidMaterials().isEmpty()) {
            if (!ProceduralItemGenerationSettingsConfig.isStavesEnabled() && !ProceduralItemGenerationSettingsConfig.isWandsEnabled()) {
               Logger.warn(
                  "No valid materials detected for the procedural item settings. If you are trying to disable them, use the 'dropProcedurallyGeneratedItems' option instead. Warn the developer."
               );
            }

            return;
         }
      }

      for (String string : ProceduralItemGenerationSettingsConfig.getValidMaterials()) {
         Material material = Material.getMaterial(string);
         if (material != null && material.isItem() && !material.isAir()) {
            validProceduralMaterials.add(material);
         } else {
            Logger.info("Invalid material type detected: " + string);
         }
      }
   }
}
