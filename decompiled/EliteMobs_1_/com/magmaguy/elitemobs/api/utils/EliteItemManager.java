package com.magmaguy.elitemobs.api.utils;

import com.google.common.collect.Multimap;
import com.magmaguy.elitemobs.combatsystem.CombatSystem;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.items.EliteItemLore;
import com.magmaguy.elitemobs.items.ItemDurability;
import com.magmaguy.elitemobs.items.ItemTagger;
import com.magmaguy.elitemobs.items.customenchantments.SoulbindEnchantment;
import com.magmaguy.elitemobs.magmacore.util.AttributeManager;
import com.magmaguy.elitemobs.skills.WeaponIdentityResolver;
import javax.annotation.Nullable;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class EliteItemManager {
   public static final double DAMAGE_ENCHANTMENT_PERCENT_PER_LEVEL = 0.025;

   public static double getDPS(@Nullable ItemStack itemStack) {
      double attackSpeed = getAttackSpeed(itemStack);
      double flatDamage = getBaseDamage(itemStack) + getEliteMobsSpecificDamage(itemStack);
      return getDPS(attackSpeed, flatDamage * getEliteDamageEnchantmentMultiplier(itemStack));
   }

   public static double getDPS(Double attackSpeed, Double damage) {
      if (attackSpeed != null && damage != null) {
         double attackSpeedPerSecond = 1.0 / attackSpeed;
         return damage / attackSpeedPerSecond;
      } else {
         return 4.0;
      }
   }

   public static double getBaseDamage(ItemStack itemStack) {
      double defaultDamage = 1.0;
      if (itemStack == null) {
         return defaultDamage;
      } else if (itemStack.getType() == Material.BOW) {
         return 6.0;
      } else if (itemStack.getType() == Material.CROSSBOW) {
         return 7.0;
      } else {
         try {
            for (AttributeModifier attributeModifier : itemStack.getType()
               .getDefaultAttributeModifiers(EquipmentSlot.HAND)
               .get(AttributeManager.getAttribute("generic_attack_damage"))) {
               defaultDamage = attributeCrawler(defaultDamage, attributeModifier);
            }
         } catch (NoSuchMethodError var5) {
            return 1.0;
         }

         if (itemStack.getItemMeta() != null
            && itemStack.getItemMeta().getAttributeModifiers() != null
            && itemStack.getItemMeta().getAttributeModifiers(EquipmentSlot.HAND).containsKey(AttributeManager.getAttribute("generic_attack_damage"))) {
            for (AttributeModifier attributeModifier : itemStack.getItemMeta()
               .getAttributeModifiers()
               .get(AttributeManager.getAttribute("generic_attack_damage"))) {
               defaultDamage = attributeCrawler(defaultDamage, attributeModifier);
            }
         }

         return defaultDamage;
      }
   }

   public static double getAttackSpeed(@Nullable ItemStack itemStack) {
      double defaultAttackSpeed = 4.0;
      if (itemStack == null) {
         return defaultAttackSpeed;
      } else if (itemStack.getType() == Material.BOW) {
         return 1.0;
      } else if (itemStack.getType() == Material.CROSSBOW) {
         return 0.8;
      } else {
         ItemMeta itemMeta = itemStack.getItemMeta();
         Attribute attackSpeedAttribute = AttributeManager.getAttribute("generic_attack_speed");
         Multimap<Attribute, AttributeModifier> handModifiers = itemMeta == null ? null : itemMeta.getAttributeModifiers(EquipmentSlot.HAND);
         if (handModifiers != null && attackSpeedAttribute != null && handModifiers.containsKey(attackSpeedAttribute)) {
            for (AttributeModifier attributeModifier : handModifiers.get(attackSpeedAttribute)) {
               defaultAttackSpeed = attributeCrawler(defaultAttackSpeed, attributeModifier);
            }
         } else {
            try {
               for (AttributeModifier attributeModifier : itemStack.getType().getDefaultAttributeModifiers(EquipmentSlot.HAND).get(attackSpeedAttribute)) {
                  defaultAttackSpeed = attributeCrawler(defaultAttackSpeed, attributeModifier);
               }
            } catch (NoSuchMethodError var8) {
               defaultAttackSpeed = 4.0;
            }
         }

         return defaultAttackSpeed;
      }
   }

   private static double attributeCrawler(double baseValue, AttributeModifier attributeModifier) {
      switch (attributeModifier.getOperation()) {
         case ADD_NUMBER:
            baseValue += attributeModifier.getAmount();
            break;
         case MULTIPLY_SCALAR_1:
         case ADD_SCALAR:
            baseValue += baseValue * attributeModifier.getAmount();
      }

      return baseValue;
   }

   public static double getWeaponLevel(@Nullable ItemStack itemStack) {
      return Math.max(
         Math.round(
            (getDPS(getAttackSpeed(itemStack), getBaseDamage(itemStack)) + getDPS(getAttackSpeed(itemStack), ItemTagger.getEliteDamageAttribute(itemStack)))
               / 1.6
         ),
         0L
      );
   }

   public static double getEliteDamageFromEliteAttributes(@Nullable ItemStack itemStack) {
      return ItemTagger.getEliteDamageAttribute(itemStack);
   }

   public static double getEliteDamageFromEnchantments(@Nullable ItemStack itemStack) {
      if (itemStack == null) {
         return 0.0;
      } else {
         ItemMeta itemMeta = itemStack.getItemMeta();
         if (itemMeta == null) {
            return 0.0;
         } else {
            int enchantmentLevel = ItemTagger.getEnchantment(itemMeta, Enchantment.SHARPNESS.getKey())
               + ItemTagger.getEnchantment(itemMeta, Enchantment.POWER.getKey());
            return calculateDamageEnchantmentBonus(enchantmentLevel);
         }
      }
   }

   public static double getEliteDamageEnchantmentMultiplier(@Nullable ItemStack itemStack) {
      return 1.0 + getEliteDamageFromEnchantments(itemStack);
   }

   static double calculateDamageEnchantmentBonus(int enchantmentLevel) {
      return enchantmentLevel < 1 ? 0.0 : enchantmentLevel * 0.025;
   }

   public static double getEliteMobsSpecificDamage(@Nullable ItemStack itemStack) {
      return getEliteDamageFromEliteAttributes(itemStack);
   }

   public static double getTotalDPS(@Nullable ItemStack itemStack) {
      if (itemStack == null) {
         return 0.0;
      } else {
         double attackSpeed = getAttackSpeed(itemStack);
         double baseDPS = getDPS(attackSpeed, getBaseDamage(itemStack));
         double totalDPS = getDPS(itemStack);
         return Math.max(totalDPS - baseDPS, 0.0);
      }
   }

   public static double getEliteDefense(@Nullable ItemStack itemStack) {
      if (itemStack != null && itemStack.getItemMeta() != null) {
         double materialValue = CombatSystem.getMaterialTier(itemStack.getType()) / 4.0;
         double defenseAttribute = ItemTagger.getEliteDefenseAttribute(itemStack);
         return materialValue + defenseAttribute;
      } else {
         return 0.0;
      }
   }

   public static double getArmorLevel(@Nullable ItemStack itemStack) {
      return itemStack != null && itemStack.getItemMeta() != null ? getEliteDefense(itemStack) * 4.0 : 0.0;
   }

   public static double getBonusEliteDefense(@Nullable ItemStack itemStack) {
      if (itemStack != null && itemStack.getItemMeta() != null) {
         int enchantmentLevel = 0;
         enchantmentLevel = ItemTagger.getEnchantment(itemStack.getItemMeta(), Enchantment.PROTECTION.getKey());
         return enchantmentLevel * 0.25;
      } else {
         return 0.0;
      }
   }

   public static double getItemLevel(@Nullable ItemStack itemStack) {
      int storedLevel = ItemTagger.getStoredItemLevel(itemStack);
      if (storedLevel >= 0) {
         return storedLevel;
      } else {
         double value = getWeaponLevel(itemStack);
         return value > 3.0 ? value : getArmorLevel(itemStack);
      }
   }

   public static double calculateItemLevel(@Nullable ItemStack itemStack) {
      double value = getWeaponLevel(itemStack);
      return value > 3.0 ? value : getArmorLevel(itemStack);
   }

   public static int getRoundedItemLevel(@Nullable ItemStack itemStack) {
      return (int)Math.round(getItemLevel(itemStack));
   }

   public static boolean isWeapon(@Nullable ItemStack itemStack) {
      return isWeapon(itemStack, WeaponIdentityResolver.isMagicWeapon(itemStack));
   }

   public static boolean isWeapon(@Nullable ItemStack itemStack, boolean magicWeapon) {
      if (magicWeapon) {
         return true;
      } else if (itemStack == null) {
         return false;
      } else if (!itemStack.getType().equals(Material.WOODEN_AXE) && !itemStack.getType().equals(Material.CROSSBOW)) {
         if (!ItemSettingsConfig.isUseHoesAsWeapons()
            || !itemStack.getType().equals(Material.WOODEN_HOE)
               && !itemStack.getType().equals(Material.GOLDEN_HOE)
               && !itemStack.getType().equals(Material.STONE_HOE)
               && !itemStack.getType().equals(Material.IRON_HOE)
               && !itemStack.getType().equals(Material.DIAMOND_HOE)
               && !itemStack.getType().equals(Material.NETHERITE_HOE)) {
            try {
               if (itemStack.getType().equals(Material.MACE)) {
                  return true;
               }
            } catch (NoSuchFieldError var3) {
            }

            String materialName = itemStack.getType().name();
            return materialName.endsWith("_SPEAR") ? true : getWeaponLevel(itemStack) > 3.0;
         } else {
            return true;
         }
      } else {
         return true;
      }
   }

   public static boolean isArmor(@Nullable ItemStack itemStack) {
      return getArmorLevel(itemStack) > 0.0;
   }

   public static boolean isOnLastDamage(@Nullable ItemStack itemStack) {
      if (itemStack == null) {
         return false;
      } else if (!itemStack.hasItemMeta()) {
         return false;
      } else if (!ItemTagger.isEliteItem(itemStack)) {
         return false;
      } else if (itemStack.getItemMeta() instanceof Damageable damageable) {
         return ItemDurability.maximum(itemStack) == 0 ? false : damageable.getDamage() + 1 >= ItemDurability.maximum(itemStack);
      } else {
         return false;
      }
   }

   public static void setEliteLevel(@Nullable ItemStack itemStack, int level) {
      if (itemStack != null) {
         initializeEliteLevel(itemStack, level);
         new EliteItemLore(itemStack, false);
      }
   }

   public static void initializeEliteLevel(@Nullable ItemStack itemStack, int level) {
      if (itemStack != null) {
         registerEliteItem(itemStack);
         ItemTagger.setItemLevel(itemStack, level);
         if (isWeapon(itemStack)) {
            double damage = calculateEliteBonus(itemStack, level);
            if (damage > 0.0) {
               ItemTagger.setEliteDamageAttribute(itemStack, damage);
            }
         } else if (isArmor(itemStack)) {
            double defense = calculateEliteBonus(itemStack, level);
            if (defense > 0.0) {
               ItemTagger.setEliteDefenseAttribute(itemStack, defense);
            }
         }
      }
   }

   public static void setEliteLevel(@Nullable ItemStack itemStack, int level, boolean onlyArmorOrWeapons) {
      if (itemStack != null) {
         if (isWeapon(itemStack)) {
            ItemTagger.setItemLevel(itemStack, level);
            double damage = calculateEliteBonus(itemStack, level);
            if (damage > 0.0) {
               ItemTagger.setEliteDamageAttribute(itemStack, damage);
            }
         } else if (isArmor(itemStack)) {
            ItemTagger.setItemLevel(itemStack, level);
            double defense = calculateEliteBonus(itemStack, level);
            if (defense > 0.0) {
               ItemTagger.setEliteDefenseAttribute(itemStack, defense);
            }
         } else {
            if (onlyArmorOrWeapons) {
               return;
            }

            ItemTagger.setItemLevel(itemStack, level);
         }

         if (!isEliteMobsItem(itemStack)) {
            registerEliteItem(itemStack);
            new EliteItemLore(itemStack, false, true);
         } else {
            new EliteItemLore(itemStack, false, false);
         }
      }
   }

   public static double calculateEliteBonus(ItemStack itemStack, int level) {
      if (isWeapon(itemStack)) {
         return level * 1.6 / 1.0 / getAttackSpeed(itemStack) - getBaseDamage(itemStack);
      } else {
         return isArmor(itemStack) ? (level - CombatSystem.getMaterialTier(itemStack.getType())) / 4.0 : 0.0;
      }
   }

   public static void tagArrow(@Nullable Projectile projectile) {
      if (projectile != null) {
         if (projectile.getShooter() instanceof Player) {
            tagArrow(projectile, ((Player)projectile.getShooter()).getInventory().getItemInMainHand());
         }
      }
   }

   public static void tagArrow(@Nullable Projectile projectile, @Nullable ItemStack weapon) {
      if (projectile != null && projectile.getShooter() instanceof Player) {
         ItemTagger.setEliteDamageAttribute(projectile, getEliteMobsSpecificDamage(weapon));
      }
   }

   public static double getArrowEliteDamage(@Nullable Projectile projectile) {
      return ItemTagger.getEliteDamageAttribute(projectile);
   }

   public static boolean isEliteMobsItem(ItemStack itemStack) {
      return ItemTagger.isEliteItem(itemStack);
   }

   public static void registerEliteItem(@Nullable ItemStack itemStack) {
      if (itemStack != null && itemStack.getItemMeta() != null) {
         ItemMeta itemMeta = itemStack.getItemMeta();
         ItemTagger.registerEliteItem(itemMeta);
         itemStack.setItemMeta(itemMeta);
      }
   }

   @Nullable
   public static boolean hasSoulbindEnchantment(@NotNull ItemMeta itemMeta) {
      return SoulbindEnchantment.itemHasSoulbindEnchantment(itemMeta);
   }

   @Nullable
   public static Player getSoulboundPlayer(@NotNull ItemMeta itemMeta) {
      return SoulbindEnchantment.getSoulboundPlayer(itemMeta);
   }
}
