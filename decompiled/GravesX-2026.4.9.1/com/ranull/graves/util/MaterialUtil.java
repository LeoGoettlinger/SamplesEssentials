package com.ranull.graves.util;

import java.util.Set;
import org.bukkit.Material;

public class MaterialUtil {
   private static final Set<String> AIR_NAMES = Set.of("AIR", "CAVE_AIR", "VOID_AIR", "LEGACY_AIR", "LEGACY_CAVE_AIR", "LEGACY_VOID_AIR");
   private static final Set<String> WATER_NAMES = Set.of("WATER", "STATIONARY_WATER", "LEGACY_WATER", "LEGACY_STATIONARY_WATER");
   private static final Set<String> LAVA_NAMES = Set.of("LAVA", "STATIONARY_LAVA", "LEGACY_LAVA", "LEGACY_STATIONARY_LAVA");
   private static final Set<String> PLAYER_HEAD_NAMES = Set.of(
      "PLAYER_HEAD", "PLAYER_WALL_HEAD", "SKULL", "SKULL_ITEM", "LEGACY_PLAYER_HEAD", "LEGACY_PLAYER_WALL_HEAD", "LEGACY_SKULL", "LEGACY_SKULL_ITEM"
   );
   private static final Set<String> NOT_SOLID_FOR_US = Set.of("WEB", "COBWEB", "LEGACY_WEB", "LEGACY_COBWEB");
   private static final Set<String> SAFE_SUPPORTS = Set.of(
      "SCAFFOLDING",
      "POWDER_SNOW",
      "SWEET_BERRY_BUSH",
      "HONEY_BLOCK",
      "TRIPWIRE",
      "TRIPWIRE_HOOK",
      "LEGACY_SWEET_BERRY_BUSH",
      "LEGACY_HONEY_BLOCK",
      "LEGACY_TRIPWIRE",
      "LEGACY_TRIPWIRE_HOOK"
   );

   private MaterialUtil() {
   }

   public static boolean isAir(Material material) {
      return isAir(material.name());
   }

   public static boolean isAir(String name) {
      return AIR_NAMES.contains(name);
   }

   public static boolean isLava(Material material) {
      return isLava(material.name());
   }

   public static boolean isLava(String name) {
      return LAVA_NAMES.contains(name);
   }

   public static boolean isSafeNotSolid(Material material) {
      return !isSolid(material) && !isLava(material);
   }

   public static boolean isSafeSolid(Material material) {
      return isSolid(material) && !isLava(material);
   }

   private static boolean isSolid(Material material) {
      String name = material.name();
      return !isSlabName(name) && !isCarpetName(name) && !NOT_SOLID_FOR_US.contains(name) ? material.isSolid() || isSafeSupport(name) : false;
   }

   private static boolean isSafeSupport(String name) {
      return SAFE_SUPPORTS.contains(name);
   }

   private static boolean isSlabName(String name) {
      return name.endsWith("_SLAB") || name.equals("STEP") || name.equals("DOUBLE_STEP") || name.equals("LEGACY_STEP") || name.equals("LEGACY_DOUBLE_STEP");
   }

   private static boolean isCarpetName(String name) {
      return name.endsWith("_CARPET") || name.equals("CARPET") || name.equals("LEGACY_CARPET");
   }

   public static boolean isWater(Material material) {
      return isWater(material.name());
   }

   public static boolean isWater(String name) {
      return WATER_NAMES.contains(name);
   }

   public static boolean isPlayerHead(Material material) {
      return isPlayerHead(material.name());
   }

   public static boolean isPlayerHead(String name) {
      return PLAYER_HEAD_NAMES.contains(name);
   }
}
