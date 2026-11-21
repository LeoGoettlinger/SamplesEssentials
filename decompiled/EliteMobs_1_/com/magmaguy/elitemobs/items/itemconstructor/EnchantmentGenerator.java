package com.magmaguy.elitemobs.items.itemconstructor;

import com.magmaguy.elitemobs.combatsystem.CombatSystem;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.config.ProceduralItemGenerationSettingsConfig;
import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfig;
import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfigFields;
import com.magmaguy.elitemobs.items.EliteEnchantmentCatalog;
import com.magmaguy.elitemobs.items.EliteEnchantments;
import com.magmaguy.elitemobs.skills.SkillType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class EnchantmentGenerator {
   public static HashMap<String, Integer> withProceduralCustomEnchantments(int level, CustomItemsConfigFields fields, HashMap<String, Integer> authored) {
      HashMap<String, Integer> result = new HashMap<>(authored);
      if (fields.isProceduralEnchantments()) {
         MagicEnchantmentGeneration.generate(level, fields.getWeaponType()).forEach((key, value) -> result.merge(key, value, Math::max));
      }

      return result;
   }

   public static HashMap<Enchantment, Integer> withProceduralEnchantments(int level, CustomItemsConfigFields fields, HashMap<Enchantment, Integer> authored) {
      HashMap<Enchantment, Integer> result = new HashMap<>(authored);
      if (fields.isProceduralEnchantments()) {
         HashMap<Enchantment, Integer> generated = generateEnchantments(
            level, fields.getMaterial(), fields.getWeaponType(), new ItemStack(fields.getMaterial()).getItemMeta()
         );
         generated.forEach((enchantment, value) -> result.merge(enchantment, value, Math::max));
      }

      return result;
   }

   public static HashMap<Enchantment, Integer> generateEnchantments(double level, Material material, SkillType weaponType, ItemMeta meta) {
      return weaponType != SkillType.STAVES && weaponType != SkillType.WANDS
         ? generateEnchantments(level, material, meta)
         : generateEnchantments(level, Material.BOW, meta, true);
   }

   public static List<Enchantment> supportedMagicEnchantments() {
      return List.of(Enchantment.POWER, Enchantment.UNBREAKING, Enchantment.MENDING, Enchantment.VANISHING_CURSE);
   }

   public static ItemMeta generateEnchantments(ItemMeta itemMeta, HashMap<Enchantment, Integer> enchantmentMap) {
      for (Entry<Enchantment, Integer> entry : enchantmentMap.entrySet()) {
         if (entry != null) {
            EnchantmentsConfigFields enchantmentsConfigFields = EnchantmentsConfig.getEnchantment(
               entry.getKey().getKey().getKey().toLowerCase(Locale.ROOT) + ".yml"
            );
            if (enchantmentsConfigFields != null && enchantmentsConfigFields.isEnabled()) {
               if (enchantmentMap.get(entry.getKey()) > entry.getKey().getMaxLevel()) {
                  if (EliteEnchantments.isPotentialEliteEnchantment(entry.getKey())) {
                     if (enchantmentMap.get(entry.getKey()) > entry.getKey().getMaxLevel()) {
                        writeNative(itemMeta, entry.getKey(), entry.getKey().getMaxLevel());
                     } else {
                        writeNative(itemMeta, entry.getKey(), enchantmentMap.get(entry.getKey()));
                     }
                  } else {
                     writeNative(itemMeta, entry.getKey(), entry.getValue());
                  }
               } else {
                  writeNative(itemMeta, entry.getKey(), entry.getValue());
               }
            }
         }
      }

      return itemMeta;
   }

   private static void writeNative(ItemMeta meta, Enchantment enchantment, int level) {
      if (meta instanceof EnchantmentStorageMeta book) {
         book.addStoredEnchant(enchantment, level, true);
      } else {
         meta.addEnchant(enchantment, level, true);
      }
   }

   public static HashMap<Enchantment, Integer> generateEnchantments(double itemTier, Material material, ItemMeta itemMeta) {
      return generateEnchantments(itemTier, material, itemMeta, false);
   }

   private static HashMap<Enchantment, Integer> generateEnchantments(double itemTier, Material material, ItemMeta itemMeta, boolean magicWeapon) {
      HashMap<Enchantment, Integer> enchantmentMap = new HashMap<>();
      itemTier -= CombatSystem.getMaterialTier(material);
      if (itemTier < 1.0) {
         return enchantmentMap;
      } else {
         HashMap<Enchantment, Integer> validEnchantments = new HashMap<>();
         if (magicWeapon) {
            for (Enchantment enchantment : supportedMagicEnchantments()) {
               validEnchantments.putAll(validateEnchantments(enchantment.getKey().getKey()));
            }
         } else {
            switch (material) {
               case TRIDENT:
                  if (ThreadLocalRandom.current().nextDouble() < 0.5) {
                     validEnchantments.putAll(validateEnchantments("LOYALTY"));
                  } else {
                     validEnchantments.putAll(validateEnchantments("CHANNELING"));
                  }

                  validEnchantments.putAll(validateEnchantments("RIPTIDE"));
                  validEnchantments.putAll(validateEnchantments("IMPALING"));
               case DIAMOND_SWORD:
               case GOLDEN_SWORD:
               case IRON_SWORD:
               case STONE_SWORD:
               case WOODEN_SWORD:
                  validEnchantments.putAll(validateEnchantments("SHARPNESS"));
                  validEnchantments.putAll(validateEnchantments("BANE_OF_ARTHROPODS"));
                  validEnchantments.putAll(validateEnchantments("SMITE"));
                  validEnchantments.putAll(validateEnchantments("UNBREAKING"));
                  validEnchantments.putAll(validateEnchantments("FIRE_ASPECT"));
                  validEnchantments.putAll(validateEnchantments("KNOCKBACK"));
                  validEnchantments.putAll(validateEnchantments("LOOTING"));
                  validEnchantments.putAll(validateEnchantments("MENDING"));
                  validEnchantments.putAll(validateEnchantments("SWEEPING_EDGE"));
                  validEnchantments.putAll(validateEnchantments("VANISHING_CURSE"));
                  break;
               case BOW:
                  validEnchantments.putAll(validateEnchantments("POWER"));
                  validEnchantments.putAll(validateEnchantments("FLAME"));
                  validEnchantments.putAll(validateEnchantments("INFINITY"));
                  validEnchantments.putAll(validateEnchantments("PUNCH"));
                  validEnchantments.putAll(validateEnchantments("UNBREAKING"));
                  validEnchantments.putAll(validateEnchantments("MENDING"));
                  validEnchantments.putAll(validateEnchantments("VANISHING_CURSE"));
                  break;
               case CROSSBOW:
                  validEnchantments.putAll(validateEnchantments("POWER"));
                  validEnchantments.putAll(validateEnchantments("QUICK_CHARGE"));
                  validEnchantments.putAll(validateEnchantments("MULTISHOT"));
                  validEnchantments.putAll(validateEnchantments("PIERCING"));
                  validEnchantments.putAll(validateEnchantments("UNBREAKING"));
                  validEnchantments.putAll(validateEnchantments("MENDING"));
                  validEnchantments.putAll(validateEnchantments("VANISHING_CURSE"));
                  break;
               case MACE:
                  validEnchantments.putAll(validateEnchantments("DENSITY"));
                  validEnchantments.putAll(validateEnchantments("BREACH"));
                  validEnchantments.putAll(validateEnchantments("WIND_BURST"));
                  validEnchantments.putAll(validateEnchantments("SMITE"));
                  validEnchantments.putAll(validateEnchantments("BANE_OF_ARTHROPODS"));
                  validEnchantments.putAll(validateEnchantments("FIRE_ASPECT"));
                  validEnchantments.putAll(validateEnchantments("UNBREAKING"));
                  validEnchantments.putAll(validateEnchantments("MENDING"));
                  validEnchantments.putAll(validateEnchantments("VANISHING_CURSE"));
                  break;
               case DIAMOND_SPEAR:
               case IRON_SPEAR:
               case GOLDEN_SPEAR:
               case STONE_SPEAR:
               case WOODEN_SPEAR:
               case COPPER_SPEAR:
               case NETHERITE_SPEAR:
                  validEnchantments.putAll(validateEnchantments("LUNGE"));
                  validEnchantments.putAll(validateEnchantments("SHARPNESS"));
                  validEnchantments.putAll(validateEnchantments("SMITE"));
                  validEnchantments.putAll(validateEnchantments("BANE_OF_ARTHROPODS"));
                  validEnchantments.putAll(validateEnchantments("FIRE_ASPECT"));
                  validEnchantments.putAll(validateEnchantments("KNOCKBACK"));
                  validEnchantments.putAll(validateEnchantments("LOOTING"));
                  validEnchantments.putAll(validateEnchantments("UNBREAKING"));
                  validEnchantments.putAll(validateEnchantments("MENDING"));
                  validEnchantments.putAll(validateEnchantments("VANISHING_CURSE"));
                  break;
               case DIAMOND_PICKAXE:
               case GOLDEN_PICKAXE:
               case IRON_PICKAXE:
               case STONE_PICKAXE:
               case WOODEN_PICKAXE:
                  validEnchantments.putAll(validateEnchantments("EFFICIENCY"));
                  validEnchantments.putAll(validateEnchantments("UNBREAKING"));
                  validEnchantments.putAll(validateEnchantments("MENDING"));
                  validEnchantments.putAll(validateEnchantments("VANISHING_CURSE"));
                  if (ThreadLocalRandom.current().nextDouble() < 0.5) {
                     validEnchantments.putAll(validateEnchantments("FORTUNE"));
                  } else {
                     validEnchantments.putAll(validateEnchantments("SILK_TOUCH"));
                  }
                  break;
               case DIAMOND_SHOVEL:
               case GOLDEN_SHOVEL:
               case IRON_SHOVEL:
               case STONE_SHOVEL:
               case WOODEN_SHOVEL:
                  validEnchantments.putAll(validateEnchantments("EFFICIENCY"));
                  validEnchantments.putAll(validateEnchantments("UNBREAKING"));
                  validEnchantments.putAll(validateEnchantments("MENDING"));
                  validEnchantments.putAll(validateEnchantments("VANISHING_CURSE"));
                  if (ThreadLocalRandom.current().nextDouble() < 0.5) {
                     validEnchantments.putAll(validateEnchantments("FORTUNE"));
                  } else {
                     validEnchantments.putAll(validateEnchantments("SILK_TOUCH"));
                  }
                  break;
               case DIAMOND_HOE:
               case GOLDEN_HOE:
               case IRON_HOE:
               case STONE_HOE:
               case WOODEN_HOE:
                  if (ItemSettingsConfig.isUseHoesAsWeapons()) {
                     validEnchantments.putAll(validateEnchantments("SHARPNESS"));
                  }

                  validEnchantments.putAll(validateEnchantments("EFFICIENCY"));
                  validEnchantments.putAll(validateEnchantments("UNBREAKING"));
                  validEnchantments.putAll(validateEnchantments("MENDING"));
                  validEnchantments.putAll(validateEnchantments("VANISHING_CURSE"));
                  if (ThreadLocalRandom.current().nextDouble() < 0.5) {
                     validEnchantments.putAll(validateEnchantments("FORTUNE"));
                  } else {
                     validEnchantments.putAll(validateEnchantments("SILK_TOUCH"));
                  }
                  break;
               case SHIELD:
                  validEnchantments.putAll(validateEnchantments("UNBREAKING"));
                  validEnchantments.putAll(validateEnchantments("MENDING"));
                  validEnchantments.putAll(validateEnchantments("VANISHING_CURSE"));
                  break;
               case DIAMOND_AXE:
               case GOLDEN_AXE:
               case IRON_AXE:
               case STONE_AXE:
               case WOODEN_AXE:
                  validEnchantments.putAll(validateEnchantments("SHARPNESS"));
                  validEnchantments.putAll(validateEnchantments("BANE_OF_ARTHROPODS"));
                  validEnchantments.putAll(validateEnchantments("SMITE"));
                  validEnchantments.putAll(validateEnchantments("UNBREAKING"));
                  validEnchantments.putAll(validateEnchantments("MENDING"));
                  validEnchantments.putAll(validateEnchantments("VANISHING_CURSE"));
                  validEnchantments.putAll(validateEnchantments("EFFICIENCY"));
                  validEnchantments.putAll(validateEnchantments("FORTUNE"));
                  break;
               case CHAINMAIL_HELMET:
               case DIAMOND_HELMET:
               case GOLDEN_HELMET:
               case IRON_HELMET:
               case LEATHER_HELMET:
               case TURTLE_HELMET:
                  validEnchantments.putAll(validateEnchantments("PROTECTION"));
                  validEnchantments.putAll(validateEnchantments("BINDING_CURSE"));
                  validEnchantments.putAll(validateEnchantments("UNBREAKING"));
                  validEnchantments.putAll(validateEnchantments("MENDING"));
                  validEnchantments.putAll(validateEnchantments("RESPIRATION"));
                  validEnchantments.putAll(validateEnchantments("BLAST_PROTECTION"));
                  validEnchantments.putAll(validateEnchantments("FIRE_PROTECTION"));
                  validEnchantments.putAll(validateEnchantments("PROJECTILE_PROTECTION"));
                  validEnchantments.putAll(validateEnchantments("THORNS"));
                  validEnchantments.putAll(validateEnchantments("VANISHING_CURSE"));
                  validEnchantments.putAll(validateEnchantments("AQUA_AFFINITY"));
                  break;
               case CHAINMAIL_CHESTPLATE:
               case DIAMOND_CHESTPLATE:
               case GOLDEN_CHESTPLATE:
               case IRON_CHESTPLATE:
               case LEATHER_CHESTPLATE:
                  validEnchantments.putAll(validateEnchantments("PROTECTION"));
                  validEnchantments.putAll(validateEnchantments("UNBREAKING"));
                  validEnchantments.putAll(validateEnchantments("MENDING"));
                  validEnchantments.putAll(validateEnchantments("BLAST_PROTECTION"));
                  validEnchantments.putAll(validateEnchantments("FIRE_PROTECTION"));
                  validEnchantments.putAll(validateEnchantments("PROJECTILE_PROTECTION"));
                  validEnchantments.putAll(validateEnchantments("THORNS"));
                  validEnchantments.putAll(validateEnchantments("VANISHING_CURSE"));
                  break;
               case CHAINMAIL_LEGGINGS:
               case DIAMOND_LEGGINGS:
               case GOLDEN_LEGGINGS:
               case IRON_LEGGINGS:
               case LEATHER_LEGGINGS:
                  validEnchantments.putAll(validateEnchantments("PROTECTION"));
                  validEnchantments.putAll(validateEnchantments("BINDING_CURSE"));
                  validEnchantments.putAll(validateEnchantments("UNBREAKING"));
                  validEnchantments.putAll(validateEnchantments("MENDING"));
                  validEnchantments.putAll(validateEnchantments("BLAST_PROTECTION"));
                  validEnchantments.putAll(validateEnchantments("FIRE_PROTECTION"));
                  validEnchantments.putAll(validateEnchantments("PROJECTILE_PROTECTION"));
                  validEnchantments.putAll(validateEnchantments("THORNS"));
                  validEnchantments.putAll(validateEnchantments("VANISHING_CURSE"));
                  break;
               case CHAINMAIL_BOOTS:
               case DIAMOND_BOOTS:
               case GOLDEN_BOOTS:
               case IRON_BOOTS:
               case LEATHER_BOOTS:
                  validEnchantments.putAll(validateEnchantments("PROTECTION"));
                  validEnchantments.putAll(validateEnchantments("BINDING_CURSE"));
                  validEnchantments.putAll(validateEnchantments("UNBREAKING"));
                  validEnchantments.putAll(validateEnchantments("MENDING"));
                  validEnchantments.putAll(validateEnchantments("BLAST_PROTECTION"));
                  validEnchantments.putAll(validateEnchantments("FEATHER_FALLING"));
                  validEnchantments.putAll(validateEnchantments("FIRE_PROTECTION"));
                  validEnchantments.putAll(validateEnchantments("PROJECTILE_PROTECTION"));
                  validEnchantments.putAll(validateEnchantments("THORNS"));
                  validEnchantments.putAll(validateEnchantments("VANISHING_CURSE"));
                  validEnchantments.putAll(validateEnchantments("DEPTH_STRIDER"));
                  validEnchantments.putAll(validateEnchantments("FROST_WALKER"));
                  validEnchantments.putAll(validateEnchantments("SOUL_SPEED"));
                  break;
               case FISHING_ROD:
                  validEnchantments.putAll(validateEnchantments("UNBREAKING"));
                  validEnchantments.putAll(validateEnchantments("VANISHING_CURSE"));
                  validEnchantments.putAll(validateEnchantments("MENDING"));
                  validEnchantments.putAll(validateEnchantments("LUCK_OF_THE_SEA"));
                  validEnchantments.putAll(validateEnchantments("LURE"));
                  break;
               case SHEARS:
                  validEnchantments.putAll(validateEnchantments("EFFICIENCY"));
                  validEnchantments.putAll(validateEnchantments("VANISHING_CURSE"));
                  validEnchantments.putAll(validateEnchantments("MENDING"));
                  validEnchantments.putAll(validateEnchantments("UNBREAKING"));
            }
         }

         int secondaryEnchantmentTotalParsedLevel = totalSecondaryEnchantmentCount(validEnchantments);
         int maxSecondaryEnchantmentLevel = secondaryEnchantmentTotalParsedLevel < itemTier - 2.0 ? secondaryEnchantmentTotalParsedLevel : (int)itemTier;
         int secondaryEnchantmentCount = ThreadLocalRandom.current().nextInt(maxSecondaryEnchantmentLevel + 1);
         if (!(itemTier < 2.0) && secondaryEnchantmentCount >= 1 && validEnchantments.size() != 0) {
            for (HashMap<Enchantment, Integer> validEnchantmentsClone = (HashMap<Enchantment, Integer>)validEnchantments.clone();
               ThreadLocalRandom.current().nextBoolean() && validEnchantments.size() >= 1;
               secondaryEnchantmentCount--
            ) {
               int randomIndex = ThreadLocalRandom.current().nextInt(validEnchantments.size());
               List<Enchantment> enchantmentList = new ArrayList<>();

               for (Enchantment enchantment : validEnchantments.keySet()) {
                  enchantmentList.add(enchantment);
               }

               Enchantment enchantment = enchantmentList.get(randomIndex);
               validEnchantments.put(enchantment, validEnchantments.get(enchantment) - 1);
               int finalEnchantLevel = validEnchantmentsClone.get(enchantment) - validEnchantments.get(enchantment);
               enchantmentMap.put(enchantment, finalEnchantLevel);
               int newEnchantInt = validEnchantments.get(enchantment);
               if (newEnchantInt == 0) {
                  validEnchantments.remove(enchantment);
               }
            }

            generateEnchantments(itemMeta, enchantmentMap);
            return enchantmentMap;
         } else {
            generateEnchantments(itemMeta, enchantmentMap);
            return enchantmentMap;
         }
      }
   }

   public static HashMap<String, Integer> generateCustomEnchantments(double itemTier, Material material) {
      HashMap<String, Integer> enchantmentMap = new HashMap<>();
      if (itemTier < 1.0) {
         return enchantmentMap;
      } else {
         HashMap<String, Integer> validSecondaryEnchantments = new HashMap<>();
         switch (material) {
            case TRIDENT:
            case DIAMOND_SWORD:
            case GOLDEN_SWORD:
            case IRON_SWORD:
            case STONE_SWORD:
            case WOODEN_SWORD:
               validSecondaryEnchantments.putAll(validateSecondaryCustomEnchantments("critical_strikes"));
               break;
            case BOW:
            case CROSSBOW:
               validSecondaryEnchantments.putAll(validateSecondaryCustomEnchantments("critical_strikes"));
               break;
            case MACE:
            case DIAMOND_SPEAR:
            case IRON_SPEAR:
            case GOLDEN_SPEAR:
            case STONE_SPEAR:
            case WOODEN_SPEAR:
            case COPPER_SPEAR:
            case NETHERITE_SPEAR:
            case DIAMOND_AXE:
            case GOLDEN_AXE:
            case IRON_AXE:
            case STONE_AXE:
            case WOODEN_AXE:
               validSecondaryEnchantments.putAll(validateSecondaryCustomEnchantments("critical_strikes"));
            case DIAMOND_PICKAXE:
            case GOLDEN_PICKAXE:
            case IRON_PICKAXE:
            case STONE_PICKAXE:
            case WOODEN_PICKAXE:
            case DIAMOND_SHOVEL:
            case GOLDEN_SHOVEL:
            case IRON_SHOVEL:
            case STONE_SHOVEL:
            case WOODEN_SHOVEL:
            case DIAMOND_HOE:
            case GOLDEN_HOE:
            case IRON_HOE:
            case STONE_HOE:
            case WOODEN_HOE:
            case SHIELD:
            case TURTLE_HELMET:
            case FISHING_ROD:
            case SHEARS:
            default:
               break;
            case CHAINMAIL_HELMET:
            case DIAMOND_HELMET:
            case GOLDEN_HELMET:
            case IRON_HELMET:
            case LEATHER_HELMET:
               validSecondaryEnchantments.putAll(validateSecondaryCustomEnchantments("hunter"));
               break;
            case CHAINMAIL_CHESTPLATE:
            case DIAMOND_CHESTPLATE:
            case GOLDEN_CHESTPLATE:
            case IRON_CHESTPLATE:
            case LEATHER_CHESTPLATE:
               validSecondaryEnchantments.putAll(validateSecondaryCustomEnchantments("hunter"));
               break;
            case CHAINMAIL_LEGGINGS:
            case DIAMOND_LEGGINGS:
            case GOLDEN_LEGGINGS:
            case IRON_LEGGINGS:
            case LEATHER_LEGGINGS:
               validSecondaryEnchantments.putAll(validateSecondaryCustomEnchantments("hunter"));
               break;
            case CHAINMAIL_BOOTS:
            case DIAMOND_BOOTS:
            case GOLDEN_BOOTS:
            case IRON_BOOTS:
            case LEATHER_BOOTS:
               validSecondaryEnchantments.putAll(validateSecondaryCustomEnchantments("hunter"));
         }

         int secondaryEnchantmentTotalParsedLevel = totalEnchantmentCount(validSecondaryEnchantments);
         if (!(itemTier < 2.0) && secondaryEnchantmentTotalParsedLevel >= 1 && validSecondaryEnchantments.size() != 0) {
            return ThreadLocalRandom.current().nextDouble() > ProceduralItemGenerationSettingsConfig.getCustomEnchantmentChance()
               ? enchantmentMap
               : validSecondaryEnchantments;
         } else {
            return enchantmentMap;
         }
      }
   }

   private static HashMap<Enchantment, Integer> validateEnchantments(String string) {
      EnchantmentsConfigFields enchantmentsConfigFields = EnchantmentsConfig.getEnchantment(string.toLowerCase(Locale.ROOT) + ".yml");
      if (enchantmentsConfigFields != null && enchantmentsConfigFields.isEnabled() && enchantmentsConfigFields.isEnabledForProcedurallyGeneratedItems()) {
         HashMap<Enchantment, Integer> enchantmentMap = new HashMap<>();
         if ((string.equalsIgnoreCase("VANISHING_CURSE") || string.equalsIgnoreCase("BINDING_CURSE")) && ThreadLocalRandom.current().nextDouble() < 0.5) {
            return enchantmentMap;
         } else {
            if (enchantmentsConfigFields != null && enchantmentsConfigFields.isEnabled()) {
               Enchantment enchantment = enchantmentsConfigFields.getEnchantment();
               enchantmentMap.put(enchantment, enchantmentsConfigFields.getMaxLevel());
            }

            return enchantmentMap;
         }
      } else {
         return new HashMap<>();
      }
   }

   private static int totalSecondaryEnchantmentCount(HashMap<Enchantment, Integer> validEnchantments) {
      int totalCount = 0;

      for (Enchantment enchantment : validEnchantments.keySet()) {
         totalCount += validEnchantments.get(enchantment);
      }

      return totalCount;
   }

   private static int totalEnchantmentCount(HashMap<String, Integer> validEnchantments) {
      int totalCount = 0;

      for (String enchantment : validEnchantments.keySet()) {
         totalCount += validEnchantments.get(enchantment);
      }

      return totalCount;
   }

   private static HashMap<String, Integer> validateSecondaryCustomEnchantments(String name) {
      return new HashMap<>(EliteEnchantmentCatalog.procedural("elitemobs:" + name));
   }
}
