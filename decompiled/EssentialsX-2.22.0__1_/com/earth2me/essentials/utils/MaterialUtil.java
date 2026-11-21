package com.earth2me.essentials.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.material.MaterialData;

public final class MaterialUtil {
   public static final Material SPAWNER = EnumUtil.getMaterial("MOB_SPAWNER", "SPAWNER");
   private static final Set<Material> BEDS = EnumUtil.getAllMatching(
      Material.class,
      "BED",
      "BED_BLOCK",
      "WHITE_BED",
      "ORANGE_BED",
      "MAGENTA_BED",
      "LIGHT_BLUE_BED",
      "YELLOW_BED",
      "LIME_BED",
      "PINK_BED",
      "GRAY_BED",
      "LIGHT_GRAY_BED",
      "CYAN_BED",
      "PURPLE_BED",
      "BLUE_BED",
      "BROWN_BED",
      "GREEN_BED",
      "RED_BED",
      "BLACK_BED"
   );
   private static final Set<Material> BANNERS = EnumUtil.getAllMatching(
      Material.class,
      "BANNER",
      "WHITE_BANNER",
      "ORANGE_BANNER",
      "MAGENTA_BANNER",
      "LIGHT_BLUE_BANNER",
      "YELLOW_BANNER",
      "LIME_BANNER",
      "PINK_BANNER",
      "GRAY_BANNER",
      "LIGHT_GRAY_BANNER",
      "CYAN_BANNER",
      "PURPLE_BANNER",
      "BLUE_BANNER",
      "BROWN_BANNER",
      "GREEN_BANNER",
      "RED_BANNER",
      "BLACK_BANNER",
      "SHIELD"
   );
   private static final Set<Material> EDITABLE_BOOKS = EnumUtil.getAllMatching(Material.class, "WRITTEN_BOOK", "WRITABLE_BOOK", "BOOK_AND_QUILL");
   private static final Set<Material> FIREWORKS = EnumUtil.getAllMatching(Material.class, "FIREWORK", "FIREWORK_ROCKET");
   private static final Set<Material> FIREWORK_CHARGE = EnumUtil.getAllMatching(Material.class, "FIREWORK_CHARGE", "FIREWORK_STAR");
   private static final Set<Material> LEGACY_SKULLS = EnumUtil.getAllMatching(Material.class, "SKULL", "SKULL_ITEM");
   private static final Set<Material> LEATHER_ARMOR = EnumUtil.getAllMatching(
      Material.class, "LEATHER_HELMET", "LEATHER_CHESTPLATE", "LEATHER_LEGGINGS", "LEATHER_BOOTS"
   );
   private static final Set<Material> MOB_HEADS = EnumUtil.getAllMatching(
      Material.class,
      "SKELETON_SKULL",
      "SKELETON_WALL_SKULL",
      "WITHER_SKELETON_SKULL",
      "WITHER_SKELETON_WALL_SKULL",
      "CREEPER_HEAD",
      "CREEPER_WALL_HEAD",
      "ZOMBIE_HEAD",
      "ZOMBIE_WALL_HEAD",
      "DRAGON_HEAD",
      "DRAGON_WALL_HEAD",
      "PIGLIN_HEAD",
      "PIGLIN_WALL_HEAD"
   );
   private static final Set<Material> PLAYER_HEADS = EnumUtil.getAllMatching(Material.class, "PLAYER_HEAD", "PLAYER_WALL_HEAD");
   private static final Set<Material> POTIONS = EnumUtil.getAllMatching(Material.class, "POTION", "SPLASH_POTION", "LINGERING_POTION", "TIPPED_ARROW");
   private static final Set<Material> SIGN_POSTS = EnumUtil.getAllMatching(
      Material.class,
      "SIGN",
      "SIGN_POST",
      "ACACIA_SIGN",
      "BIRCH_SIGN",
      "DARK_OAK_SIGN",
      "JUNGLE_SIGN",
      "OAK_SIGN",
      "SPRUCE_SIGN",
      "CRIMSON_SIGN",
      "WARPED_SIGN",
      "MANGROVE_SIGN",
      "CHERRY_SIGN",
      "BAMBOO_SIGN",
      "PALE_OAK_SIGN"
   );
   private static final Set<Material> WALL_SIGNS = EnumUtil.getAllMatching(
      Material.class,
      "WALL_SIGN",
      "ACACIA_WALL_SIGN",
      "BIRCH_WALL_SIGN",
      "DARK_OAK_WALL_SIGN",
      "JUNGLE_WALL_SIGN",
      "OAK_WALL_SIGN",
      "SPRUCE_WALL_SIGN",
      "CRIMSON_WALL_SIGN",
      "WARPED_WALL_SIGN",
      "MANGROVE_WALL_SIGN",
      "CHERRY_WALL_SIGN",
      "BAMBOO_WALL_SIGN",
      "PALE_OAK_WALL_SIGN"
   );
   private static final Set<Material> HANGING_SIGNS = EnumUtil.getAllMatching(
      Material.class,
      "ACACIA_HANGING_SIGN",
      "BIRCH_HANGING_SIGN",
      "DARK_OAK_HANGING_SIGN",
      "JUNGLE_HANGING_SIGN",
      "OAK_HANGING_SIGN",
      "SPRUCE_HANGING_SIGN",
      "CRIMSON_HANGING_SIGN",
      "WARPED_HANGING_SIGN",
      "MANGROVE_HANGING_SIGN",
      "CHERRY_HANGING_SIGN",
      "BAMBOO_HANGING_SIGN",
      "PALE_OAK_HANGING_SIGN"
   );
   private static final Set<Material> HANGING_WALL_SIGNS = EnumUtil.getAllMatching(
      Material.class,
      "ACACIA_WALL_HANGING_SIGN",
      "BIRCH_WALL_HANGING_SIGN",
      "DARK_OAK_WALL_HANGING_SIGN",
      "JUNGLE_WALL_HANGING_SIGN",
      "OAK_WALL_HANGING_SIGN",
      "SPRUCE_WALL_HANGING_SIGN",
      "CRIMSON_WALL_HANGING_SIGN",
      "WARPED_WALL_HANGING_SIGN",
      "MANGROVE_WALL_HANGING_SIGN",
      "CHERRY_WALL_HANGING_SIGN",
      "BAMBOO_WALL_HANGING_SIGN",
      "PALE_OAK_WALL_HANGING_SIGN"
   );
   private static final Set<Material> HELMETS = EnumUtil.getAllMatching(
      Material.class,
      "LEATHER_HELMET",
      "CHAINMAIL_HELMET",
      "IRON_HELMET",
      "GOLD_HELMET",
      "GOLDEN_HELMET",
      "DIAMOND_HELMET",
      "NETHERITE_HELMET",
      "TURTLE_HELMET",
      "COPPER_HELMET"
   );
   private static final Set<Material> CHESTPLATES = EnumUtil.getAllMatching(
      Material.class,
      "LEATHER_CHESTPLATE",
      "CHAINMAIL_CHESTPLATE",
      "IRON_CHESTPLATE",
      "GOLD_CHESTPLATE",
      "GOLDEN_CHESTPLATE",
      "DIAMOND_CHESTPLATE",
      "NETHERITE_CHESTPLATE",
      "ELYTRA",
      "COPPER_CHESTPLATE"
   );
   private static final Set<Material> LEGGINGS = EnumUtil.getAllMatching(
      Material.class,
      "LEATHER_LEGGINGS",
      "CHAINMAIL_LEGGINGS",
      "IRON_LEGGINGS",
      "GOLD_LEGGINGS",
      "GOLDEN_LEGGINGS",
      "DIAMOND_LEGGINGS",
      "NETHERITE_LEGGINGS",
      "COPPER_LEGGINGS"
   );
   private static final Set<Material> BOOTS = EnumUtil.getAllMatching(
      Material.class, "LEATHER_BOOTS", "CHAINMAIL_BOOTS", "IRON_BOOTS", "GOLD_BOOTS", "GOLDEN_BOOTS", "DIAMOND_BOOTS", "NETHERITE_BOOTS", "COPPER_BOOTS"
   );

   private MaterialUtil() {
   }

   public static boolean isHelmet(final Material material) {
      return HELMETS.contains(material);
   }

   public static boolean isChestplate(final Material material) {
      return CHESTPLATES.contains(material);
   }

   public static boolean isLeggings(final Material material) {
      return LEGGINGS.contains(material);
   }

   public static boolean isBoots(final Material material) {
      return BOOTS.contains(material);
   }

   public static boolean isArmor(final Material material) {
      return isHelmet(material) || isChestplate(material) || isLeggings(material) || isBoots(material);
   }

   public static boolean isBed(final Material material) {
      return BEDS.contains(material);
   }

   public static boolean isBanner(final Material material) {
      return BANNERS.contains(material);
   }

   public static boolean isFirework(final Material material) {
      return FIREWORKS.contains(material);
   }

   public static boolean isFireworkCharge(final Material material) {
      return FIREWORK_CHARGE.contains(material);
   }

   public static boolean isLeatherArmor(final Material material) {
      return LEATHER_ARMOR.contains(material);
   }

   public static boolean isPlayerHead(final ItemStack stack) {
      return PLAYER_HEADS.contains(stack.getType())
         ? true
         : VersionUtil.PRE_FLATTENING && LEGACY_SKULLS.contains(stack.getType()) && stack.getDurability() == 3;
   }

   public static boolean isPotion(final Material material) {
      return POTIONS.contains(material);
   }

   public static boolean isSignPost(final Material material) {
      return SIGN_POSTS.contains(material);
   }

   public static boolean isWallSign(final Material material) {
      return WALL_SIGNS.contains(material);
   }

   public static boolean isHangingSign(final Material material) {
      return HANGING_SIGNS.contains(material);
   }

   public static boolean isWallHangingSign(final Material material) {
      return HANGING_WALL_SIGNS.contains(material);
   }

   public static boolean isEditableBook(final Material material) {
      return EDITABLE_BOOKS.contains(material);
   }

   public static boolean isSign(final Material material) {
      return isSignPost(material) || isWallSign(material) || isHangingSign(material) || isWallHangingSign(material);
   }

   public static boolean isSkull(final Material material) {
      return PLAYER_HEADS.contains(material) || LEGACY_SKULLS.contains(material);
   }

   public static boolean isAir(final Material material) {
      return material == Material.AIR || VersionUtil.getServerBukkitVersion().isHigherThanOrEqualTo(VersionUtil.v1_14_4_R01) && material.isAir();
   }

   public static int getDamage(final ItemStack stack) {
      if (VersionUtil.PRE_FLATTENING) {
         return stack.getDurability();
      } else {
         return stack.getItemMeta() instanceof Damageable ? ((Damageable)stack.getItemMeta()).getDamage() : 0;
      }
   }

   public static void setDamage(final ItemStack stack, final int damage) {
      if (VersionUtil.PRE_FLATTENING) {
         stack.setDurability((short)damage);
      } else if (stack.getItemMeta() instanceof Damageable) {
         Damageable damageable = (Damageable)stack.getItemMeta();
         damageable.setDamage(damage);
         stack.setItemMeta(damageable);
      }
   }

   public static Material convertFromLegacy(final int id, final byte damage) {
      Iterator getMaterialFromId = EnumSet.allOf(Material.class).iterator();

      while (true) {
         if (getMaterialFromId.hasNext()) {
            Material material = (Material)getMaterialFromId.next();
            if (material.getId() != id) {
               continue;
            }

            try {
               return Bukkit.getUnsafe().fromLegacy(new MaterialData(material, damage));
            } catch (NoSuchMethodError var6) {
            }
         }

         try {
            Method getMaterialFromIdx = Material.class.getDeclaredMethod("getMaterial", int.class);
            return (Material)getMaterialFromIdx.invoke(null, id);
         } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException var5) {
            return null;
         }
      }
   }

   public static DyeColor getColorOf(final Material material) {
      for (DyeColor color : DyeColor.values()) {
         if (material.toString().contains(color.name())) {
            return color;
         }
      }

      return DyeColor.WHITE;
   }
}
