package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfig;
import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfigFields;
import com.magmaguy.elitemobs.items.potioneffects.ElitePotionEffect;
import com.magmaguy.elitemobs.items.potioneffects.ElitePotionEffectContainer;
import com.magmaguy.elitemobs.magmacore.enchantments.EnchantmentItems;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.utils.PersistentVanillaData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import javax.annotation.Nullable;
import lombok.Generated;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Trident;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.tags.ItemTagType;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class ItemTagger {
   public static final NamespacedKey eliteMobsItemNamespacedKey = new NamespacedKey(MetadataHandler.PLUGIN, "EliteMobsItem");
   public static final NamespacedKey customLore = new NamespacedKey(MetadataHandler.PLUGIN, "CustomLore");
   private static final NamespacedKey ELITE_DAMAGE = new NamespacedKey(MetadataHandler.PLUGIN, "eliteDamage");
   private static final NamespacedKey ELITE_DEFENSE = new NamespacedKey(MetadataHandler.PLUGIN, "eliteDefense");
   private static final NamespacedKey ELITE_LEVEL = new NamespacedKey(MetadataHandler.PLUGIN, "eliteLevel");
   private static final NamespacedKey CUSTOM_ITEM_ID = new NamespacedKey(MetadataHandler.PLUGIN, "custom_item_id");
   private static final NamespacedKey ARROW_WEAPON_LEVEL = new NamespacedKey(MetadataHandler.PLUGIN, "arrowWeaponLevel");
   private static final NamespacedKey ARROW_WEAPON_MATERIAL = new NamespacedKey(MetadataHandler.PLUGIN, "arrowWeaponMaterial");
   private static final NamespacedKey ARROW_SKILL_TYPE = new NamespacedKey(MetadataHandler.PLUGIN, "arrowSkillType");
   private static final NamespacedKey ARROW_SKILL_LEVEL = new NamespacedKey(MetadataHandler.PLUGIN, "arrowSkillLevel");
   private static final NamespacedKey ARROW_DAMAGE_MULTIPLIER = new NamespacedKey(MetadataHandler.PLUGIN, "arrowDamageMultiplier");
   private static final NamespacedKey ARROW_LAUNCH_VELOCITY = new NamespacedKey(MetadataHandler.PLUGIN, "arrowLaunchVelocity");
   private static final NamespacedKey ARROW_CRITICAL = new NamespacedKey(MetadataHandler.PLUGIN, "arrowCritical");
   private static final NamespacedKey ARROW_LOUD_STRIKES = new NamespacedKey(MetadataHandler.PLUGIN, "arrowLoudStrikes");
   private static final NamespacedKey ARROW_SECONDARY = new NamespacedKey(MetadataHandler.PLUGIN, "arrowSecondary");
   private static final NamespacedKey ARROW_DRAW_MILLIS = new NamespacedKey(MetadataHandler.PLUGIN, "arrowDrawMillis");
   public static String itemValue = "ItemValue";
   public static NamespacedKey onHitPotionEffectKey = new NamespacedKey(MetadataHandler.PLUGIN, "onHitPotionEffect");
   public static NamespacedKey continuousPotionEffectKey = new NamespacedKey(MetadataHandler.PLUGIN, "continuousPotionEffect");
   public static NamespacedKey itemSource = new NamespacedKey(MetadataHandler.PLUGIN, "itemSource");

   public static void registerEliteItem(ItemMeta itemMeta) {
      itemMeta.getPersistentDataContainer().set(eliteMobsItemNamespacedKey, PersistentDataType.BYTE, (byte)1);
   }

   public static void registerEliteItem(ItemStack itemStack) {
      if (itemStack != null && itemStack.getItemMeta() != null) {
         ItemMeta itemMeta = itemStack.getItemMeta();
         itemMeta.getPersistentDataContainer().set(eliteMobsItemNamespacedKey, PersistentDataType.BYTE, (byte)1);
         itemStack.setItemMeta(itemMeta);
         new EliteItemLore(itemStack, false);
      }
   }

   public static void registerCustomItemId(ItemMeta itemMeta, String itemId) {
      if (itemMeta != null && itemId != null && !itemId.isBlank()) {
         itemMeta.getPersistentDataContainer().set(CUSTOM_ITEM_ID, PersistentDataType.STRING, itemId);
      }
   }

   @Nullable
   public static String getCustomItemId(@Nullable ItemStack itemStack) {
      return itemStack != null && itemStack.hasItemMeta()
         ? (String)itemStack.getItemMeta().getPersistentDataContainer().get(CUSTOM_ITEM_ID, PersistentDataType.STRING)
         : null;
   }

   public static void registerCustomLore(ItemMeta itemMeta, List<String> lore) {
      StringBuilder parsedLore = new StringBuilder();

      for (int i = 0; i < lore.size(); i++) {
         parsedLore.append(lore.get(i));
         if (i < lore.size() - 1) {
            parsedLore.append("\n");
         }
      }

      itemMeta.getPersistentDataContainer().set(customLore, PersistentDataType.STRING, parsedLore.toString());
   }

   public static List<String> getCustomLore(ItemMeta itemMeta) {
      String rawLore = (String)itemMeta.getPersistentDataContainer().get(customLore, PersistentDataType.STRING);
      if (rawLore == null) {
         return new ArrayList<>();
      } else {
         ArrayList<String> parsedLore = new ArrayList<>();
         Collections.addAll(parsedLore, rawLore.split("\n"));
         return parsedLore;
      }
   }

   public static boolean isEliteItem(ItemStack itemStack) {
      if (itemStack == null) {
         return false;
      } else if (!itemStack.hasItemMeta()) {
         return false;
      } else {
         return itemStack.getItemMeta().getCustomTagContainer().hasCustomTag(eliteMobsItemNamespacedKey, ItemTagType.BYTE)
            ? true
            : itemStack.getItemMeta().getPersistentDataContainer().has(eliteMobsItemNamespacedKey, PersistentDataType.BYTE);
      }
   }

   public static void registerEnchantments(ItemMeta itemMeta, HashMap<Enchantment, Integer> enchantmentMap) {
      for (Enchantment enchantment : enchantmentMap.keySet()) {
         if (EliteEnchantments.isPotentialEliteEnchantment(enchantment)) {
            registerEnchantment(itemMeta, enchantment.getKey(), enchantmentMap.get(enchantment));
         }
      }
   }

   public static void registerEnchantment(ItemMeta itemMeta, NamespacedKey enchantmentKey, int enchantmentLevel) {
      itemMeta.getPersistentDataContainer().set(enchantmentKey, PersistentDataType.INTEGER, enchantmentLevel);
   }

   public static int getEnchantment(ItemMeta itemMeta, String enchantmentKey) {
      return getEnchantment(itemMeta, new NamespacedKey(MetadataHandler.PLUGIN, enchantmentKey));
   }

   public static int getEnchantment(ItemMeta itemMeta, NamespacedKey enchantmentKey) {
      if (itemMeta == null) {
         return 0;
      } else if (enchantmentKey.getNamespace().equals("elitemobs") && EliteEnchantmentCatalog.ownsFilename(enchantmentKey.getKey())) {
         return EnchantmentItems.inspectCustom(itemMeta).getOrDefault(enchantmentKey.toString(), 0);
      } else {
         Integer level = (Integer)itemMeta.getPersistentDataContainer().get(enchantmentKey, PersistentDataType.INTEGER);
         if (level == null) {
            Enchantment enchantment = Enchantment.getByKey(enchantmentKey);
            if (enchantment != null) {
               return itemMeta instanceof EnchantmentStorageMeta book ? book.getStoredEnchantLevel(enchantment) : itemMeta.getEnchantLevel(enchantment);
            } else {
               return 0;
            }
         } else {
            return level;
         }
      }
   }

   public static boolean hasKey(ItemStack itemStack, String key) {
      if (itemStack == null) {
         return false;
      } else {
         return !itemStack.hasItemMeta()
            ? false
            : Objects.requireNonNull(itemStack.getItemMeta())
               .getPersistentDataContainer()
               .has(new NamespacedKey(MetadataHandler.PLUGIN, key), PersistentDataType.STRING);
      }
   }

   public static ArrayList<ElitePotionEffect> getPotionEffects(ItemMeta itemMeta, NamespacedKey namespacedKey) {
      return ElitePotionEffectContainer.getElitePotionEffectContainer(itemMeta, namespacedKey);
   }

   public static void registerItemSource(EliteEntity eliteEntity, ItemMeta itemMeta) {
      if (eliteEntity == null) {
         itemMeta.getPersistentDataContainer().set(itemSource, PersistentDataType.STRING, ItemSettingsConfig.getShopItemSource());
      } else {
         itemMeta.getPersistentDataContainer()
            .set(itemSource, PersistentDataType.STRING, ItemSettingsConfig.getMobItemSource().replace("$mob", eliteEntity.getName()));
      }
   }

   public static String getItemSource(ItemMeta itemMeta) {
      return (String)itemMeta.getPersistentDataContainer().get(itemSource, PersistentDataType.STRING);
   }

   public static void writeItemValue(ItemStack itemStack, Player player) {
      PersistentVanillaData.write(itemStack, itemValue, ItemWorthCalculator.writeItemWorth(itemStack, player));
   }

   public static double getItemValue(ItemStack itemStack) {
      return PersistentVanillaData.getDouble(itemStack, itemValue);
   }

   public static double getEliteDamageAttribute(@Nullable ItemStack itemStack) {
      double value = 0.0;
      return itemStack != null
            && itemStack.getItemMeta() != null
            && itemStack.getItemMeta().getPersistentDataContainer().get(ELITE_DAMAGE, PersistentDataType.DOUBLE) != null
         ? (Double)itemStack.getItemMeta().getPersistentDataContainer().get(ELITE_DAMAGE, PersistentDataType.DOUBLE)
         : value;
   }

   public static double getEliteDamageAttribute(@Nullable Projectile projectile) {
      if (projectile == null) {
         return 0.0;
      } else {
         return projectile.getPersistentDataContainer().get(ELITE_DAMAGE, PersistentDataType.DOUBLE) == null
            ? 0.0
            : (Double)projectile.getPersistentDataContainer().get(ELITE_DAMAGE, PersistentDataType.DOUBLE);
      }
   }

   public static void setEliteDamageAttribute(ItemStack itemStack, double damageValue) {
      if (itemStack != null && itemStack.getItemMeta() != null) {
         ItemMeta itemMeta = itemStack.getItemMeta();
         itemMeta.getPersistentDataContainer().set(ELITE_DAMAGE, PersistentDataType.DOUBLE, damageValue);
         itemStack.setItemMeta(itemMeta);
      }
   }

   public static void setEliteDamageAttribute(@Nullable Projectile projectile, double damageValue) {
      if (projectile != null) {
         if (ItemSettingsConfig.isUseEliteEnchantments()) {
            projectile.getPersistentDataContainer().set(ELITE_DAMAGE, PersistentDataType.DOUBLE, damageValue);
         } else if (projectile.getType().equals(EntityType.ARROW)) {
            Arrow arrow = (Arrow)projectile;
            arrow.setDamage(arrow.getDamage() + damageValue);
         } else if (projectile.getType().equals(EntityType.TRIDENT)) {
            Trident trident = (Trident)projectile;
            trident.setDamage(trident.getDamage() + damageValue);
         }
      }
   }

   public static boolean isSecondaryArrow(Projectile projectile) {
      return projectile.getPersistentDataContainer().has(ARROW_SECONDARY, PersistentDataType.BYTE);
   }

   public static void setArrowDrawMillis(Projectile projectile, long millis) {
      projectile.getPersistentDataContainer().set(ARROW_DRAW_MILLIS, PersistentDataType.LONG, Math.max(0L, millis));
   }

   public static long getArrowDrawMillis(Projectile projectile) {
      return (Long)projectile.getPersistentDataContainer().getOrDefault(ARROW_DRAW_MILLIS, PersistentDataType.LONG, 0L);
   }

   public static ItemTagger.ArrowCombatSnapshot snapshotArrowCombat(Projectile projectile) {
      PersistentDataContainer data = projectile.getPersistentDataContainer();
      return new ItemTagger.ArrowCombatSnapshot(
         (Double)data.get(ELITE_DAMAGE, PersistentDataType.DOUBLE),
         (Double)data.get(ARROW_WEAPON_LEVEL, PersistentDataType.DOUBLE),
         (String)data.get(ARROW_WEAPON_MATERIAL, PersistentDataType.STRING),
         (String)data.get(ARROW_SKILL_TYPE, PersistentDataType.STRING),
         (Integer)data.get(ARROW_SKILL_LEVEL, PersistentDataType.INTEGER),
         (Byte)data.get(ARROW_CRITICAL, PersistentDataType.BYTE),
         (Double)data.get(ARROW_LOUD_STRIKES, PersistentDataType.DOUBLE),
         projectile instanceof AbstractArrow arrow ? arrow.getDamage() : null
      );
   }

   public static void setArrowWeaponMaterial(Projectile projectile, ItemStack weapon) {
      if (weapon != null && !weapon.getType().isAir()) {
         projectile.getPersistentDataContainer().set(ARROW_WEAPON_MATERIAL, PersistentDataType.STRING, weapon.getType().name());
      } else {
         projectile.getPersistentDataContainer().remove(ARROW_WEAPON_MATERIAL);
      }
   }

   @Nullable
   public static Material getArrowWeaponMaterial(Projectile projectile) {
      String material = (String)projectile.getPersistentDataContainer().get(ARROW_WEAPON_MATERIAL, PersistentDataType.STRING);
      if (material == null) {
         return null;
      } else {
         try {
            return Material.valueOf(material);
         } catch (IllegalArgumentException var3) {
            return null;
         }
      }
   }

   public static void setArrowWeaponLevel(@Nullable Projectile projectile, double level) {
      if (projectile != null) {
         projectile.getPersistentDataContainer().set(ARROW_WEAPON_LEVEL, PersistentDataType.DOUBLE, level);
      }
   }

   public static double getArrowWeaponLevel(@Nullable Projectile projectile) {
      if (projectile == null) {
         return -1.0;
      } else {
         Double val = (Double)projectile.getPersistentDataContainer().get(ARROW_WEAPON_LEVEL, PersistentDataType.DOUBLE);
         return val != null ? val : -1.0;
      }
   }

   public static void setArrowEnchantmentCombat(Projectile projectile, boolean critical, double loudStrikes) {
      if (Double.isFinite(loudStrikes) && !(loudStrikes < 0.0)) {
         projectile.getPersistentDataContainer().set(ARROW_CRITICAL, PersistentDataType.BYTE, (byte)(critical ? 1 : 0));
         projectile.getPersistentDataContainer().set(ARROW_LOUD_STRIKES, PersistentDataType.DOUBLE, loudStrikes);
      } else {
         throw new IllegalArgumentException("Invalid projectile threat");
      }
   }

   public static boolean hasArrowEnchantmentCombat(Projectile projectile) {
      return projectile.getPersistentDataContainer().has(ARROW_CRITICAL, PersistentDataType.BYTE);
   }

   public static boolean getArrowCritical(Projectile projectile) {
      return Byte.valueOf((byte)1).equals(projectile.getPersistentDataContainer().get(ARROW_CRITICAL, PersistentDataType.BYTE));
   }

   public static double getArrowLoudStrikes(Projectile projectile) {
      Double value = (Double)projectile.getPersistentDataContainer().get(ARROW_LOUD_STRIKES, PersistentDataType.DOUBLE);
      return value != null && Double.isFinite(value) && value >= 0.0 ? value : 0.0;
   }

   public static void setArrowSkillType(@Nullable Projectile projectile, String skillType) {
      if (projectile != null && skillType != null) {
         projectile.getPersistentDataContainer().set(ARROW_SKILL_TYPE, PersistentDataType.STRING, skillType);
      }
   }

   @Nullable
   public static String getArrowSkillType(@Nullable Projectile projectile) {
      return projectile == null ? null : (String)projectile.getPersistentDataContainer().get(ARROW_SKILL_TYPE, PersistentDataType.STRING);
   }

   public static void setArrowSkillLevel(@Nullable Projectile projectile, int level) {
      if (projectile != null) {
         projectile.getPersistentDataContainer().set(ARROW_SKILL_LEVEL, PersistentDataType.INTEGER, level);
      }
   }

   public static int getArrowSkillLevel(@Nullable Projectile projectile) {
      if (projectile == null) {
         return -1;
      } else {
         Integer val = (Integer)projectile.getPersistentDataContainer().get(ARROW_SKILL_LEVEL, PersistentDataType.INTEGER);
         return val != null ? val : -1;
      }
   }

   public static void setArrowDamageMultiplier(@Nullable Projectile projectile, double multiplier) {
      if (projectile != null) {
         projectile.getPersistentDataContainer().set(ARROW_DAMAGE_MULTIPLIER, PersistentDataType.DOUBLE, multiplier);
      }
   }

   public static double getArrowDamageMultiplier(@Nullable Projectile projectile) {
      if (projectile == null) {
         return -1.0;
      } else {
         Double val = (Double)projectile.getPersistentDataContainer().get(ARROW_DAMAGE_MULTIPLIER, PersistentDataType.DOUBLE);
         return val != null ? val : -1.0;
      }
   }

   public static void setArrowLaunchVelocity(@Nullable Projectile projectile, double velocity) {
      if (projectile != null) {
         projectile.getPersistentDataContainer().set(ARROW_LAUNCH_VELOCITY, PersistentDataType.DOUBLE, velocity);
      }
   }

   public static double getArrowLaunchVelocity(@Nullable Projectile projectile) {
      if (projectile == null) {
         return -1.0;
      } else {
         Double val = (Double)projectile.getPersistentDataContainer().get(ARROW_LAUNCH_VELOCITY, PersistentDataType.DOUBLE);
         return val != null ? val : -1.0;
      }
   }

   public static void clearArrowCombatData(@Nullable Projectile projectile) {
      if (projectile != null) {
         PersistentDataContainer data = projectile.getPersistentDataContainer();
         data.remove(ELITE_DAMAGE);
         data.remove(ARROW_WEAPON_LEVEL);
         data.remove(ARROW_SKILL_TYPE);
         data.remove(ARROW_SKILL_LEVEL);
         data.remove(ARROW_DAMAGE_MULTIPLIER);
         data.remove(ARROW_LAUNCH_VELOCITY);
         data.remove(ARROW_WEAPON_MATERIAL);
         data.remove(ARROW_CRITICAL);
         data.remove(ARROW_LOUD_STRIKES);
         data.remove(ARROW_SECONDARY);
         data.remove(ARROW_DRAW_MILLIS);
      }
   }

   public static double getEliteDefenseAttribute(@Nullable ItemStack itemStack) {
      if (itemStack == null) {
         return 0.0;
      } else if (itemStack.getItemMeta() == null) {
         return 0.0;
      } else {
         try {
            Double value = (Double)itemStack.getItemMeta().getPersistentDataContainer().get(ELITE_DEFENSE, PersistentDataType.DOUBLE);
            return value != null ? value : 0.0;
         } catch (IllegalArgumentException var3) {
            Float valuex = (Float)itemStack.getItemMeta().getPersistentDataContainer().get(ELITE_DEFENSE, PersistentDataType.FLOAT);
            return valuex != null ? valuex.doubleValue() : 0.0;
         }
      }
   }

   public static void setEliteDefenseAttribute(@Nullable ItemStack itemStack, double defenseValue) {
      if (itemStack != null && itemStack.getItemMeta() != null) {
         ItemMeta itemMeta = itemStack.getItemMeta();
         itemMeta.getPersistentDataContainer().set(ELITE_DEFENSE, PersistentDataType.DOUBLE, defenseValue);
         itemStack.setItemMeta(itemMeta);
      }
   }

   public static int getStoredItemLevel(@Nullable ItemStack itemStack) {
      if (itemStack == null) {
         return -1;
      } else if (itemStack.getItemMeta() == null) {
         return -1;
      } else {
         Integer level = (Integer)itemStack.getItemMeta().getPersistentDataContainer().get(ELITE_LEVEL, PersistentDataType.INTEGER);
         return level == null ? -1 : level;
      }
   }

   public static void setItemLevel(@Nullable ItemStack itemStack, int level) {
      if (itemStack != null && itemStack.getItemMeta() != null) {
         ItemMeta itemMeta = itemStack.getItemMeta();
         itemMeta.getPersistentDataContainer().set(ELITE_LEVEL, PersistentDataType.INTEGER, level);
         itemStack.setItemMeta(itemMeta);
      }
   }

   public static HashMap<NamespacedKey, Integer> getItemEnchantments(@Nullable ItemStack itemStack) {
      HashMap<NamespacedKey, Integer> itemEnchantmentFilenames = new HashMap<>();
      if (itemStack != null && itemStack.getItemMeta() != null) {
         ItemMeta itemMeta = itemStack.getItemMeta();

         for (Enchantment enchantment : Enchantment.values()) {
            int enchantmentLevel = getEnchantment(itemMeta, enchantment.getKey());
            if (enchantmentLevel > 0) {
               itemEnchantmentFilenames.put(enchantment.getKey(), enchantmentLevel);
            }
         }

         EnchantmentItems.inspectCustom(itemMeta)
            .forEach((id, level) -> itemEnchantmentFilenames.put(Objects.requireNonNull(NamespacedKey.fromString(id)), level));
         return itemEnchantmentFilenames;
      } else {
         return itemEnchantmentFilenames;
      }
   }

   public static HashMap<EnchantmentsConfigFields, Integer> getItemEnchantmentConfigFields(@Nullable ItemStack itemStack) {
      HashMap<EnchantmentsConfigFields, Integer> itemEnchantmentFilenames = new HashMap<>();
      if (itemStack != null && itemStack.getItemMeta() != null) {
         ItemMeta itemMeta = itemStack.getItemMeta();

         for (Enchantment enchantment : Enchantment.values()) {
            int enchantmentLevel = getEnchantment(itemMeta, enchantment.getKey());
            if (enchantmentLevel > 0) {
               EnchantmentsConfigFields enchantmentsConfigFields = EnchantmentsConfig.getEnchantment(enchantment);
               if (enchantmentsConfigFields == null) {
                  Logger.warn("Failed to get configuration file for enchantment called " + enchantment.getKey().getKey().toLowerCase(Locale.ROOT) + ".yml");
               } else {
                  itemEnchantmentFilenames.put(enchantmentsConfigFields, enchantmentLevel);
               }
            }
         }

         return itemEnchantmentFilenames;
      } else {
         return itemEnchantmentFilenames;
      }
   }

   @Generated
   public static NamespacedKey getELITE_DAMAGE() {
      return ELITE_DAMAGE;
   }

   @Generated
   public static NamespacedKey getELITE_DEFENSE() {
      return ELITE_DEFENSE;
   }

   @Generated
   public static NamespacedKey getELITE_LEVEL() {
      return ELITE_LEVEL;
   }

   public record ArrowCombatSnapshot(
      Double eliteDamage,
      Double weaponLevel,
      String weaponMaterial,
      String skillType,
      Integer skillLevel,
      Byte critical,
      Double loudStrikes,
      Double nativeDamage
   ) {
      public void applyToSecondary(Projectile projectile, double damageMultiplier) {
         ItemTagger.clearArrowCombatData(projectile);
         PersistentDataContainer data = projectile.getPersistentDataContainer();
         if (this.eliteDamage != null) {
            data.set(ItemTagger.ELITE_DAMAGE, PersistentDataType.DOUBLE, this.eliteDamage);
         }

         if (this.weaponLevel != null) {
            data.set(ItemTagger.ARROW_WEAPON_LEVEL, PersistentDataType.DOUBLE, this.weaponLevel);
         }

         if (this.weaponMaterial != null) {
            data.set(ItemTagger.ARROW_WEAPON_MATERIAL, PersistentDataType.STRING, this.weaponMaterial);
         }

         if (this.skillType != null) {
            data.set(ItemTagger.ARROW_SKILL_TYPE, PersistentDataType.STRING, this.skillType);
         }

         if (this.skillLevel != null) {
            data.set(ItemTagger.ARROW_SKILL_LEVEL, PersistentDataType.INTEGER, this.skillLevel);
         }

         if (this.critical != null) {
            data.set(ItemTagger.ARROW_CRITICAL, PersistentDataType.BYTE, this.critical);
         }

         if (this.loudStrikes != null) {
            data.set(ItemTagger.ARROW_LOUD_STRIKES, PersistentDataType.DOUBLE, this.loudStrikes);
         }

         if (this.nativeDamage != null && projectile instanceof AbstractArrow arrow) {
            arrow.setDamage(this.nativeDamage);
         }

         data.set(ItemTagger.ARROW_SECONDARY, PersistentDataType.BYTE, (byte)1);
         ItemTagger.setArrowDamageMultiplier(projectile, damageMultiplier);
         ItemTagger.setArrowLaunchVelocity(projectile, projectile.getVelocity().length());
      }
   }
}
