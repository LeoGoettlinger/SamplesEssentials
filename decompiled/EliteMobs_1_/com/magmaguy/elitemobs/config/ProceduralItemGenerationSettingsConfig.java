package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.items.itemconstructor.MaterialGenerator;
import com.magmaguy.elitemobs.magmacore.config.ConfigurationFile;
import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

public class ProceduralItemGenerationSettingsConfig extends ConfigurationFile {
   private static final List<String> validMaterials = new ArrayList<>();
   private static boolean doProceduralItemDrops;
   private static double customEnchantmentChance;
   private static boolean stavesEnabled;
   private static boolean wandsEnabled;
   private static ProceduralItemGenerationSettingsConfig instance;

   public ProceduralItemGenerationSettingsConfig() {
      super("ProceduralItemGenerationSettings.yml");
   }

   private void addMaterial(Material material) {
      String key = "validMaterials." + material.name();
      ConfigurationEngine.setBoolean(this.fileConfiguration, key, true);
   }

   public void cacheMaterials() {
      validMaterials.clear();
      ConfigurationSection section = this.fileConfiguration.getConfigurationSection("validMaterials");
      if (section != null) {
         for (String material : section.getKeys(false)) {
            if (section.getBoolean(material)) {
               validMaterials.add(material);
            }
         }
      }
   }

   @Override
   public void initializeValues() {
      instance = this;
      doProceduralItemDrops = ConfigurationEngine.setBoolean(this.fileConfiguration, "dropProcedurallyGeneratedItems", true);
      customEnchantmentChance = ConfigurationEngine.setDouble(this.fileConfiguration, "customEnchantmentsChance", 0.5);
      stavesEnabled = ConfigurationEngine.setBoolean(this.fileConfiguration, "validWeapons.STAVES", true);
      wandsEnabled = ConfigurationEngine.setBoolean(this.fileConfiguration, "validWeapons.WANDS", true);
      this.addMaterial(Material.DIAMOND_HELMET);
      this.addMaterial(Material.DIAMOND_CHESTPLATE);
      this.addMaterial(Material.DIAMOND_LEGGINGS);
      this.addMaterial(Material.DIAMOND_BOOTS);
      this.addMaterial(Material.DIAMOND_SWORD);
      this.addMaterial(Material.DIAMOND_AXE);
      this.addMaterial(Material.DIAMOND_HOE);
      this.addMaterial(Material.IRON_HELMET);
      this.addMaterial(Material.IRON_CHESTPLATE);
      this.addMaterial(Material.IRON_LEGGINGS);
      this.addMaterial(Material.IRON_BOOTS);
      this.addMaterial(Material.IRON_SWORD);
      this.addMaterial(Material.IRON_AXE);
      this.addMaterial(Material.IRON_HOE);
      this.addMaterial(Material.GOLDEN_HELMET);
      this.addMaterial(Material.GOLDEN_CHESTPLATE);
      this.addMaterial(Material.GOLDEN_LEGGINGS);
      this.addMaterial(Material.GOLDEN_BOOTS);
      this.addMaterial(Material.GOLDEN_SWORD);
      this.addMaterial(Material.GOLDEN_AXE);
      this.addMaterial(Material.GOLDEN_HOE);
      this.addMaterial(Material.CHAINMAIL_HELMET);
      this.addMaterial(Material.CHAINMAIL_CHESTPLATE);
      this.addMaterial(Material.CHAINMAIL_LEGGINGS);
      this.addMaterial(Material.CHAINMAIL_BOOTS);
      this.addMaterial(Material.LEATHER_HELMET);
      this.addMaterial(Material.LEATHER_CHESTPLATE);
      this.addMaterial(Material.LEATHER_LEGGINGS);
      this.addMaterial(Material.LEATHER_BOOTS);
      this.addMaterial(Material.STONE_SWORD);
      this.addMaterial(Material.STONE_AXE);
      this.addMaterial(Material.STONE_HOE);
      this.addMaterial(Material.WOODEN_SWORD);
      this.addMaterial(Material.WOODEN_AXE);
      this.addMaterial(Material.WOODEN_HOE);
      this.addMaterial(Material.SHIELD);
      this.addMaterial(Material.TURTLE_HELMET);
      this.addMaterial(Material.TRIDENT);
      this.addMaterial(Material.BOW);
      this.addMaterial(Material.CROSSBOW);

      try {
         this.addMaterial(Material.MACE);
      } catch (NoSuchFieldError var3) {
      }

      try {
         this.addMaterial(Material.DIAMOND_SPEAR);
         this.addMaterial(Material.IRON_SPEAR);
         this.addMaterial(Material.GOLDEN_SPEAR);
         this.addMaterial(Material.STONE_SPEAR);
         this.addMaterial(Material.WOODEN_SPEAR);
         this.addMaterial(Material.COPPER_SPEAR);
         this.addMaterial(Material.NETHERITE_SPEAR);
      } catch (NoSuchFieldError var2) {
      }

      this.fileConfiguration.options().copyDefaults(true);
      this.cacheMaterials();
      MaterialGenerator.initializeValidProceduralMaterials();
   }

   @Generated
   public static List<String> getValidMaterials() {
      return validMaterials;
   }

   @Generated
   public static boolean isDoProceduralItemDrops() {
      return doProceduralItemDrops;
   }

   @Generated
   public static double getCustomEnchantmentChance() {
      return customEnchantmentChance;
   }

   @Generated
   public static boolean isStavesEnabled() {
      return stavesEnabled;
   }

   @Generated
   public static boolean isWandsEnabled() {
      return wandsEnabled;
   }

   @Generated
   public static ProceduralItemGenerationSettingsConfig getInstance() {
      return instance;
   }
}
