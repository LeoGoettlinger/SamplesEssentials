package com.magmaguy.elitemobs.combatsystem;

import com.magmaguy.elitemobs.api.utils.EliteItemManager;
import com.magmaguy.elitemobs.items.ItemTagger;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ArmorDefenseCalculator {
   public static final double ENCHANT_SCALE = 0.3333333333333333;
   public static final double GEAR_BASE_REDUCTION = 0.5;
   public static final double GEAR_BONUS_PER_LEVEL = 0.025;
   public static final double GEAR_MAX_REDUCTION = 0.75;
   private static final Map<Material, Integer> VANILLA_ARMOR_LEVELS = vanillaArmorLevels();

   private static Map<Material, Integer> vanillaArmorLevels() {
      Map<Material, Integer> levels = new HashMap<>(
         Map.ofEntries(
            Map.entry(Material.LEATHER_HELMET, 1),
            Map.entry(Material.LEATHER_CHESTPLATE, 1),
            Map.entry(Material.LEATHER_LEGGINGS, 1),
            Map.entry(Material.LEATHER_BOOTS, 1),
            Map.entry(Material.GOLDEN_HELMET, 2),
            Map.entry(Material.GOLDEN_CHESTPLATE, 2),
            Map.entry(Material.GOLDEN_LEGGINGS, 2),
            Map.entry(Material.GOLDEN_BOOTS, 2),
            Map.entry(Material.CHAINMAIL_HELMET, 3),
            Map.entry(Material.CHAINMAIL_CHESTPLATE, 3),
            Map.entry(Material.CHAINMAIL_LEGGINGS, 3),
            Map.entry(Material.CHAINMAIL_BOOTS, 3),
            Map.entry(Material.IRON_HELMET, 4),
            Map.entry(Material.IRON_CHESTPLATE, 4),
            Map.entry(Material.IRON_LEGGINGS, 4),
            Map.entry(Material.IRON_BOOTS, 4),
            Map.entry(Material.TURTLE_HELMET, 4),
            Map.entry(Material.DIAMOND_HELMET, 5),
            Map.entry(Material.DIAMOND_CHESTPLATE, 5),
            Map.entry(Material.DIAMOND_LEGGINGS, 5),
            Map.entry(Material.DIAMOND_BOOTS, 5),
            Map.entry(Material.NETHERITE_HELMET, 7),
            Map.entry(Material.NETHERITE_CHESTPLATE, 7),
            Map.entry(Material.NETHERITE_LEGGINGS, 7),
            Map.entry(Material.NETHERITE_BOOTS, 7)
         )
      );

      for (String name : new String[]{"COPPER_HELMET", "COPPER_CHESTPLATE", "COPPER_LEGGINGS", "COPPER_BOOTS"}) {
         Material copper = Material.getMaterial(name);
         if (copper != null) {
            levels.put(copper, 2);
         }
      }

      return Map.copyOf(levels);
   }

   private ArmorDefenseCalculator() {
   }

   public static double getEffectiveArmorLevel(ItemStack item) {
      if (item == null || item.getType().isAir()) {
         return 0.0;
      } else {
         return EliteItemManager.isEliteMobsItem(item) ? EliteItemManager.getArmorLevel(item) : VANILLA_ARMOR_LEVELS.getOrDefault(item.getType(), 0).intValue();
      }
   }

   public static double getEnchantBonus(ItemStack item, ArmorDefenseCalculator.DamageType type) {
      if (item == null) {
         return 0.0;
      } else {
         ItemMeta itemMeta = item.getItemMeta();
         if (itemMeta == null) {
            return 0.0;
         } else {
            int total = ItemTagger.getEnchantment(itemMeta, Enchantment.PROTECTION.getKey());
            switch (type) {
               case PROJECTILE:
                  total += ItemTagger.getEnchantment(itemMeta, Enchantment.PROJECTILE_PROTECTION.getKey());
                  break;
               case EXPLOSION:
                  total += ItemTagger.getEnchantment(itemMeta, Enchantment.BLAST_PROTECTION.getKey());
                  break;
               case FIRE:
                  total += ItemTagger.getEnchantment(itemMeta, Enchantment.FIRE_PROTECTION.getKey());
            }

            return total * 0.3333333333333333;
         }
      }
   }

   public static double getGearScore(Player player, ArmorDefenseCalculator.DamageType type) {
      ItemStack[] armor = player.getInventory().getArmorContents();
      double armorLevelSum = 0.0;
      double enchantBonusSum = 0.0;

      for (ItemStack piece : armor) {
         armorLevelSum += getEffectiveArmorLevel(piece);
         enchantBonusSum += getEnchantBonus(piece, type);
      }

      enchantBonusSum += getEnchantBonus(player.getInventory().getItemInMainHand(), type);
      enchantBonusSum += getEnchantBonus(player.getInventory().getItemInOffHand(), type);
      return armorLevelSum / 4.0 + enchantBonusSum / 4.0;
   }

   public static double getGearReduction(double gearScore, int mobLevel) {
      if (mobLevel <= 0) {
         mobLevel = 1;
      }

      double reduction;
      if (gearScore <= mobLevel) {
         reduction = 0.5 * (gearScore / mobLevel);
      } else {
         reduction = 0.5 + (gearScore - mobLevel) * 0.025;
      }

      return Math.max(0.0, Math.min(0.75, reduction));
   }

   public static double getGearAdjustment(double gearScore, int mobLevel) {
      return 2.0 * (1.0 - getGearReduction(gearScore, mobLevel));
   }

   public static ArmorDefenseCalculator.DamageType fromEvent(EntityDamageByEntityEvent event) {
      return switch (event.getCause()) {
         case ENTITY_ATTACK, ENTITY_SWEEP_ATTACK -> ArmorDefenseCalculator.DamageType.MELEE;
         case PROJECTILE -> ArmorDefenseCalculator.DamageType.PROJECTILE;
         case ENTITY_EXPLOSION, BLOCK_EXPLOSION -> ArmorDefenseCalculator.DamageType.EXPLOSION;
         case FIRE, FIRE_TICK -> ArmorDefenseCalculator.DamageType.FIRE;
         default -> ArmorDefenseCalculator.DamageType.OTHER;
      };
   }

   public static enum DamageType {
      MELEE,
      PROJECTILE,
      EXPLOSION,
      FIRE,
      OTHER;
   }
}
