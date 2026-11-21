package com.magmaguy.elitemobs.items.itemconstructor;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.CustomModelsConfig;
import com.magmaguy.elitemobs.utils.CustomModelAdder;
import com.magmaguy.elitemobs.versionnotifier.VersionChecker;
import java.util.Locale;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.Map.Entry;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class EliteItemSkins {
   private static final NavigableMap<Integer, EliteItemSkins.SkinTier> TIER_THRESHOLDS = new TreeMap<>();
   private static final double NETHERITE_SWORD_DAMAGE = 8.0;
   private static final double NETHERITE_AXE_DAMAGE = 10.0;
   private static final double NETHERITE_HOE_DAMAGE = 1.0;
   private static final double SWORD_ATTACK_SPEED = 1.6;
   private static final double AXE_ATTACK_SPEED = 1.0;
   private static final double HOE_ATTACK_SPEED = 4.0;
   private static final double NETHERITE_HELMET_ARMOR = 3.0;
   private static final double NETHERITE_CHESTPLATE_ARMOR = 8.0;
   private static final double NETHERITE_LEGGINGS_ARMOR = 6.0;
   private static final double NETHERITE_BOOTS_ARMOR = 3.0;
   private static final double NETHERITE_ARMOR_TOUGHNESS = 3.0;
   private static final double NETHERITE_KNOCKBACK_RESISTANCE = 0.1;
   private static final double MAX_TOTAL_ARMOR = 30.0;
   private static final double MAX_TOTAL_TOUGHNESS = 20.0;
   private static final double MAX_KNOCKBACK_RESISTANCE = 1.0;
   private static final double MAX_ATTACK_SPEED_MULTIPLIER = 0.2;

   private static void registerTier(int minLevel, EliteItemSkins.SkinTier tier) {
      TIER_THRESHOLDS.put(minLevel, tier);
   }

   public static EliteItemSkins.SkinTier getSkinTier(int level) {
      Entry<Integer, EliteItemSkins.SkinTier> entry = TIER_THRESHOLDS.floorEntry(level);
      return entry != null ? entry.getValue() : EliteItemSkins.SkinTier.NONE;
   }

   public static int getTierMinLevel(EliteItemSkins.SkinTier tier) {
      if (tier == EliteItemSkins.SkinTier.NONE) {
         return -1;
      } else {
         for (Entry<Integer, EliteItemSkins.SkinTier> entry : TIER_THRESHOLDS.entrySet()) {
            if (entry.getValue() == tier) {
               return entry.getKey();
            }
         }

         return -1;
      }
   }

   public static String getItemModelId(Material material, int level) {
      EliteItemSkins.SkinTier tier = getSkinTier(level);
      if (!tier.hasCustomAssets()) {
         return null;
      } else {
         EliteItemSkins.ItemType itemType = EliteItemSkins.ItemType.fromMaterial(material);
         return itemType != null && itemType.hasItemModel() ? buildItemModelId(tier, itemType) : null;
      }
   }

   public static String getEquipmentModelId(Material material, int level) {
      EliteItemSkins.SkinTier tier = getSkinTier(level);
      if (!tier.hasCustomAssets()) {
         return null;
      } else {
         EliteItemSkins.ItemType itemType = EliteItemSkins.ItemType.fromMaterial(material);
         return itemType != null && itemType.hasEquipmentModel() ? buildEquipmentModelId(tier) : null;
      }
   }

   private static String buildItemModelId(EliteItemSkins.SkinTier tier, EliteItemSkins.ItemType itemType) {
      return "elitemobs:gear/" + tier.getId() + "_" + itemType.getModelName();
   }

   private static String buildEquipmentModelId(EliteItemSkins.SkinTier tier) {
      return "elitemobs:" + tier.getId();
   }

   public static void applyLevelBasedSkin(ItemStack itemStack, int level) {
      if (itemStack != null) {
         Material material = itemStack.getType();
         String itemModelId = getItemModelId(material, level);
         String equipmentModelId = getEquipmentModelId(material, level);
         if (itemModelId != null) {
            CustomModelAdder.addCustomModel(itemStack, itemModelId);
         }

         if (equipmentModelId != null) {
            CustomModelAdder.addEquippableModel(itemStack, equipmentModelId);
         }

         applyAttributeScaling(itemStack, level);
      }
   }

   public static void applySkinTier(ItemStack itemStack, EliteItemSkins.SkinTier tier) {
      if (itemStack != null && tier.hasCustomAssets()) {
         Material material = itemStack.getType();
         EliteItemSkins.ItemType itemType = EliteItemSkins.ItemType.fromMaterial(material);
         if (itemType != null) {
            if (itemType.hasItemModel()) {
               String itemModelId = buildItemModelId(tier, itemType);
               CustomModelAdder.addCustomModel(itemStack, itemModelId);
            }

            if (itemType.hasEquipmentModel()) {
               String equipmentModelId = buildEquipmentModelId(tier);
               CustomModelAdder.addEquippableModel(itemStack, equipmentModelId);
            }

            int tierLevel = getTierMinLevel(tier);
            if (tierLevel > 0) {
               applyAttributeScaling(itemStack, tierLevel);
            }
         }
      }
   }

   public static boolean isSupportedMaterial(Material material) {
      return EliteItemSkins.ItemType.fromMaterial(material) != null;
   }

   public static boolean wouldApplySkin(Material material, int level) {
      return getItemModelId(material, level) != null || getEquipmentModelId(material, level) != null;
   }

   public static String getTierDisplayName(int level) {
      EliteItemSkins.SkinTier tier = getSkinTier(level);
      if (tier == EliteItemSkins.SkinTier.NONE) {
         return "None";
      } else {
         String id = tier.getId();
         return id.substring(0, 1).toUpperCase() + id.substring(1);
      }
   }

   public static void applyAttributeScaling(ItemStack itemStack, int level) {
      if (itemStack != null && level >= 1) {
         if (CustomModelsConfig.useAttributeScaling) {
            if (!VersionChecker.serverVersionOlderThan(21, 2)) {
               Material material = itemStack.getType();
               EliteItemSkins.ItemType itemType = EliteItemSkins.ItemType.fromMaterial(material);
               if (itemType != null) {
                  ItemMeta meta = itemStack.getItemMeta();
                  if (meta != null) {
                     double levelMultiplier = Math.min(level, 100) / 100.0;
                     switch (itemType) {
                        case HELMET:
                           applyArmorAttributes(meta, 3.0, levelMultiplier, EquipmentSlotGroup.HEAD);
                           break;
                        case CHESTPLATE:
                           applyArmorAttributes(meta, 8.0, levelMultiplier, EquipmentSlotGroup.CHEST);
                           break;
                        case LEGGINGS:
                           applyArmorAttributes(meta, 6.0, levelMultiplier, EquipmentSlotGroup.LEGS);
                           break;
                        case BOOTS:
                           applyArmorAttributes(meta, 3.0, levelMultiplier, EquipmentSlotGroup.FEET);
                           break;
                        case SWORD:
                           applyWeaponAttributes(meta, 8.0, 1.6, levelMultiplier, EquipmentSlotGroup.MAINHAND);
                           break;
                        case AXE:
                           applyWeaponAttributes(meta, 10.0, 1.0, levelMultiplier, EquipmentSlotGroup.MAINHAND);
                           break;
                        case SCYTHE:
                           applyWeaponAttributes(meta, 1.0, 4.0, levelMultiplier, EquipmentSlotGroup.MAINHAND);
                        case BOW:
                        case CROSSBOW:
                     }

                     itemStack.setItemMeta(meta);
                  }
               }
            }
         }
      }
   }

   private static void applyWeaponAttributes(ItemMeta meta, double baseDamage, double baseSpeed, double levelMultiplier, EquipmentSlotGroup slot) {
      double scaledDamage = baseDamage * (1.0 + levelMultiplier);
      double speedMultiplier = Math.min(levelMultiplier * 0.1, 0.2);
      double scaledSpeed = baseSpeed * (1.0 + speedMultiplier);
      meta.addAttributeModifier(
         Attribute.ATTACK_DAMAGE,
         new AttributeModifier(new NamespacedKey(MetadataHandler.PLUGIN, "elite_damage"), scaledDamage - 1.0, Operation.ADD_NUMBER, slot)
      );
      meta.addAttributeModifier(
         Attribute.ATTACK_SPEED, new AttributeModifier(new NamespacedKey(MetadataHandler.PLUGIN, "elite_speed"), scaledSpeed - 4.0, Operation.ADD_NUMBER, slot)
      );
   }

   private static void applyArmorAttributes(ItemMeta meta, double baseArmor, double levelMultiplier, EquipmentSlotGroup slot) {
      double scaledArmor = baseArmor * (1.0 + levelMultiplier * 0.5);
      double scaledToughness = 3.0 * (1.0 + levelMultiplier * 0.67);
      double scaledKnockbackResist = Math.min(0.1 * (1.0 + levelMultiplier), 0.25);
      String slotSuffix = "_" + slot.toString().toLowerCase(Locale.ROOT);
      meta.addAttributeModifier(
         Attribute.ARMOR, new AttributeModifier(new NamespacedKey(MetadataHandler.PLUGIN, "elite_armor" + slotSuffix), scaledArmor, Operation.ADD_NUMBER, slot)
      );
      meta.addAttributeModifier(
         Attribute.ARMOR_TOUGHNESS,
         new AttributeModifier(new NamespacedKey(MetadataHandler.PLUGIN, "elite_toughness" + slotSuffix), scaledToughness, Operation.ADD_NUMBER, slot)
      );
      meta.addAttributeModifier(
         Attribute.KNOCKBACK_RESISTANCE,
         new AttributeModifier(
            new NamespacedKey(MetadataHandler.PLUGIN, "elite_knockback_resist" + slotSuffix), scaledKnockbackResist, Operation.ADD_NUMBER, slot
         )
      );
   }

   static {
      registerTier(20, EliteItemSkins.SkinTier.BRONZE);
      registerTier(40, EliteItemSkins.SkinTier.PALLADIUM);
      registerTier(60, EliteItemSkins.SkinTier.CORRUPTED);
      registerTier(80, EliteItemSkins.SkinTier.LIVING);
      registerTier(100, EliteItemSkins.SkinTier.ULTIMATIUM);
   }

   public static enum ItemType {
      HELMET("_HELMET", "helmet", true, true),
      CHESTPLATE("_CHESTPLATE", "chestplate", true, true),
      LEGGINGS("_LEGGINGS", "leggings", true, true),
      BOOTS("_BOOTS", "boots", true, true),
      SWORD("_SWORD", "sword", true, false),
      AXE("_AXE", "axe", true, false),
      SCYTHE("_HOE", "scythe", true, false),
      BOW("BOW", "bow", true, false),
      CROSSBOW("CROSSBOW", "crossbow", true, false);

      private final String materialSuffix;
      private final String modelName;
      private final boolean hasItemModel;
      private final boolean hasEquipmentModel;

      private ItemType(String materialSuffix, String modelName, boolean hasItemModel, boolean hasEquipmentModel) {
         this.materialSuffix = materialSuffix;
         this.modelName = modelName;
         this.hasItemModel = hasItemModel;
         this.hasEquipmentModel = hasEquipmentModel;
      }

      public static EliteItemSkins.ItemType fromMaterial(Material material) {
         String name = material.name();

         for (EliteItemSkins.ItemType type : values()) {
            if (type != BOW && type != CROSSBOW) {
               if (name.endsWith(type.materialSuffix)) {
                  return type;
               }
            } else if (name.equals(type.materialSuffix)) {
               return type;
            }
         }

         return null;
      }

      public String getModelName() {
         return this.modelName;
      }

      public boolean hasItemModel() {
         return this.hasItemModel;
      }

      public boolean hasEquipmentModel() {
         return this.hasEquipmentModel;
      }
   }

   public static enum SkinTier {
      NONE(null),
      BRONZE("bronze"),
      PALLADIUM("palladium"),
      CORRUPTED("corrupted"),
      LIVING("living"),
      ULTIMATIUM("ultimatium");

      private final String id;

      private SkinTier(String id) {
         this.id = id;
      }

      public String getId() {
         return this.id;
      }

      public boolean hasCustomAssets() {
         return this.id != null;
      }
   }
}
