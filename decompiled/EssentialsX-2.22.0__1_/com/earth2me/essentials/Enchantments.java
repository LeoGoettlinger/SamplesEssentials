package com.earth2me.essentials;

import com.earth2me.essentials.utils.RegistryUtil;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;

public final class Enchantments {
   private static final Map<String, Enchantment> ENCHANTMENTS = new HashMap<>();
   private static final Map<String, Enchantment> ALIASENCHANTMENTS = new HashMap<>();
   private static boolean isFlat;

   private Enchantments() {
   }

   public static String getRealName(final Enchantment enchantment) {
      if (enchantment == null) {
         return null;
      } else {
         return isFlat ? enchantment.getKey().getKey() : enchantment.getName().toLowerCase(Locale.ENGLISH);
      }
   }

   public static Enchantment getByName(final String name) {
      if (name != null && !name.isEmpty()) {
         Enchantment enchantment = null;
         if (isFlat) {
            try {
               enchantment = Enchantment.getByKey(NamespacedKey.minecraft(name.toLowerCase()));
            } catch (IllegalArgumentException var3) {
            }
         }

         if (enchantment == null) {
            enchantment = Enchantment.getByName(name.toUpperCase());
         }

         if (enchantment == null) {
            enchantment = Enchantment.getByName(name.toLowerCase());
         }

         if (enchantment == null) {
            enchantment = Enchantment.getByName(name);
         }

         if (enchantment == null) {
            enchantment = ENCHANTMENTS.get(name.toLowerCase(Locale.ENGLISH));
         }

         if (enchantment == null) {
            enchantment = ALIASENCHANTMENTS.get(name.toLowerCase(Locale.ENGLISH));
         }

         return enchantment;
      } else {
         return null;
      }
   }

   public static Set<Entry<String, Enchantment>> entrySet() {
      return ENCHANTMENTS.entrySet();
   }

   public static Set<String> keySet() {
      return ENCHANTMENTS.keySet();
   }

   public static void registerEnchantment(String name, Enchantment enchantment) {
      if (!ENCHANTMENTS.containsKey(name) && !ALIASENCHANTMENTS.containsKey(name)) {
         ENCHANTMENTS.put(name, enchantment);
      }
   }

   public static void registerAlias(String name, Enchantment enchantment) {
      if (!ENCHANTMENTS.containsKey(name) && !ALIASENCHANTMENTS.containsKey(name) && ENCHANTMENTS.containsValue(enchantment)) {
         ALIASENCHANTMENTS.put(name, enchantment);
      }
   }

   static {
      Enchantment SHARPNESS = RegistryUtil.valueOf(Enchantment.class, "DAMAGE_ALL", "SHARPNESS");
      ENCHANTMENTS.put("alldamage", SHARPNESS);
      ALIASENCHANTMENTS.put("alldmg", SHARPNESS);
      ENCHANTMENTS.put("sharpness", SHARPNESS);
      ALIASENCHANTMENTS.put("sharp", SHARPNESS);
      ALIASENCHANTMENTS.put("dal", SHARPNESS);
      Enchantment BANE_OF_ARTHROPODS = RegistryUtil.valueOf(Enchantment.class, "DAMAGE_ARTHROPODS", "BANE_OF_ARTHROPODS");
      ENCHANTMENTS.put("ardmg", BANE_OF_ARTHROPODS);
      ENCHANTMENTS.put("baneofarthropods", BANE_OF_ARTHROPODS);
      ALIASENCHANTMENTS.put("baneofarthropod", BANE_OF_ARTHROPODS);
      ALIASENCHANTMENTS.put("arthropod", BANE_OF_ARTHROPODS);
      ALIASENCHANTMENTS.put("dar", BANE_OF_ARTHROPODS);
      Enchantment SMITE = RegistryUtil.valueOf(Enchantment.class, "DAMAGE_UNDEAD", "SMITE");
      ENCHANTMENTS.put("undeaddamage", SMITE);
      ENCHANTMENTS.put("smite", SMITE);
      ALIASENCHANTMENTS.put("du", SMITE);
      Enchantment EFFICIENCY = RegistryUtil.valueOf(Enchantment.class, "DIG_SPEED", "EFFICIENCY");
      ENCHANTMENTS.put("digspeed", EFFICIENCY);
      ENCHANTMENTS.put("efficiency", EFFICIENCY);
      ALIASENCHANTMENTS.put("minespeed", EFFICIENCY);
      ALIASENCHANTMENTS.put("cutspeed", EFFICIENCY);
      ALIASENCHANTMENTS.put("ds", EFFICIENCY);
      ALIASENCHANTMENTS.put("eff", EFFICIENCY);
      Enchantment UNBREAKING = RegistryUtil.valueOf(Enchantment.class, "DURABILITY", "UNBREAKING");
      ENCHANTMENTS.put("durability", UNBREAKING);
      ALIASENCHANTMENTS.put("dura", UNBREAKING);
      ENCHANTMENTS.put("unbreaking", UNBREAKING);
      ALIASENCHANTMENTS.put("d", UNBREAKING);
      ENCHANTMENTS.put("thorns", Enchantment.THORNS);
      ENCHANTMENTS.put("highcrit", Enchantment.THORNS);
      ALIASENCHANTMENTS.put("thorn", Enchantment.THORNS);
      ALIASENCHANTMENTS.put("highercrit", Enchantment.THORNS);
      ALIASENCHANTMENTS.put("t", Enchantment.THORNS);
      ENCHANTMENTS.put("fireaspect", Enchantment.FIRE_ASPECT);
      ENCHANTMENTS.put("fire", Enchantment.FIRE_ASPECT);
      ALIASENCHANTMENTS.put("meleefire", Enchantment.FIRE_ASPECT);
      ALIASENCHANTMENTS.put("meleeflame", Enchantment.FIRE_ASPECT);
      ALIASENCHANTMENTS.put("fa", Enchantment.FIRE_ASPECT);
      ENCHANTMENTS.put("knockback", Enchantment.KNOCKBACK);
      ALIASENCHANTMENTS.put("kback", Enchantment.KNOCKBACK);
      ALIASENCHANTMENTS.put("kb", Enchantment.KNOCKBACK);
      ALIASENCHANTMENTS.put("k", Enchantment.KNOCKBACK);
      Enchantment FORTUNE = RegistryUtil.valueOf(Enchantment.class, "LOOT_BONUS_BLOCKS", "FORTUNE");
      ALIASENCHANTMENTS.put("blockslootbonus", FORTUNE);
      ENCHANTMENTS.put("fortune", FORTUNE);
      ALIASENCHANTMENTS.put("fort", FORTUNE);
      ALIASENCHANTMENTS.put("lbb", FORTUNE);
      Enchantment LOOTING = RegistryUtil.valueOf(Enchantment.class, "LOOT_BONUS_MOBS", "LOOTING");
      ALIASENCHANTMENTS.put("mobslootbonus", LOOTING);
      ENCHANTMENTS.put("mobloot", LOOTING);
      ENCHANTMENTS.put("looting", LOOTING);
      ALIASENCHANTMENTS.put("lbm", LOOTING);
      Enchantment RESPIRATION = RegistryUtil.valueOf(Enchantment.class, "OXYGEN", "RESPIRATION");
      ALIASENCHANTMENTS.put("oxygen", RESPIRATION);
      ENCHANTMENTS.put("respiration", RESPIRATION);
      ALIASENCHANTMENTS.put("breathing", RESPIRATION);
      ENCHANTMENTS.put("breath", RESPIRATION);
      ALIASENCHANTMENTS.put("o", RESPIRATION);
      Enchantment PROTECTION = RegistryUtil.valueOf(Enchantment.class, "PROTECTION_ENVIRONMENTAL", "PROTECTION");
      ENCHANTMENTS.put("protection", PROTECTION);
      ALIASENCHANTMENTS.put("prot", PROTECTION);
      ENCHANTMENTS.put("protect", PROTECTION);
      ALIASENCHANTMENTS.put("p", PROTECTION);
      Enchantment BLAST_PROTECTION = RegistryUtil.valueOf(Enchantment.class, "PROTECTION_EXPLOSIONS", "BLAST_PROTECTION");
      ALIASENCHANTMENTS.put("explosionsprotection", BLAST_PROTECTION);
      ALIASENCHANTMENTS.put("explosionprotection", BLAST_PROTECTION);
      ALIASENCHANTMENTS.put("expprot", BLAST_PROTECTION);
      ALIASENCHANTMENTS.put("blastprotection", BLAST_PROTECTION);
      ALIASENCHANTMENTS.put("bprotection", BLAST_PROTECTION);
      ALIASENCHANTMENTS.put("bprotect", BLAST_PROTECTION);
      ENCHANTMENTS.put("blastprotect", BLAST_PROTECTION);
      ALIASENCHANTMENTS.put("pe", BLAST_PROTECTION);
      Enchantment FEATHER_FALLING = RegistryUtil.valueOf(Enchantment.class, "PROTECTION_FALL", "FEATHER_FALLING");
      ALIASENCHANTMENTS.put("fallprotection", FEATHER_FALLING);
      ENCHANTMENTS.put("fallprot", FEATHER_FALLING);
      ENCHANTMENTS.put("featherfall", FEATHER_FALLING);
      ALIASENCHANTMENTS.put("featherfalling", FEATHER_FALLING);
      ALIASENCHANTMENTS.put("pfa", FEATHER_FALLING);
      Enchantment FIRE_PROTECTION = RegistryUtil.valueOf(Enchantment.class, "PROTECTION_FIRE", "FIRE_PROTECTION");
      ALIASENCHANTMENTS.put("fireprotection", FIRE_PROTECTION);
      ALIASENCHANTMENTS.put("flameprotection", FIRE_PROTECTION);
      ENCHANTMENTS.put("fireprotect", FIRE_PROTECTION);
      ALIASENCHANTMENTS.put("flameprotect", FIRE_PROTECTION);
      ENCHANTMENTS.put("fireprot", FIRE_PROTECTION);
      ALIASENCHANTMENTS.put("flameprot", FIRE_PROTECTION);
      ALIASENCHANTMENTS.put("pf", FIRE_PROTECTION);
      Enchantment PROJECTILE_PROTECTION = RegistryUtil.valueOf(Enchantment.class, "PROTECTION_PROJECTILE", "PROJECTILE_PROTECTION");
      ENCHANTMENTS.put("projectileprotection", PROJECTILE_PROTECTION);
      ENCHANTMENTS.put("projprot", PROJECTILE_PROTECTION);
      ALIASENCHANTMENTS.put("pp", PROJECTILE_PROTECTION);
      ENCHANTMENTS.put("silktouch", Enchantment.SILK_TOUCH);
      ALIASENCHANTMENTS.put("softtouch", Enchantment.SILK_TOUCH);
      ALIASENCHANTMENTS.put("st", Enchantment.SILK_TOUCH);
      Enchantment AQUA_AFFINITY = RegistryUtil.valueOf(Enchantment.class, "WATER_WORKER", "AQUA_AFFINITY");
      ENCHANTMENTS.put("waterworker", AQUA_AFFINITY);
      ENCHANTMENTS.put("aquaaffinity", AQUA_AFFINITY);
      ALIASENCHANTMENTS.put("watermine", AQUA_AFFINITY);
      ALIASENCHANTMENTS.put("ww", AQUA_AFFINITY);
      Enchantment FLAME = RegistryUtil.valueOf(Enchantment.class, "ARROW_FIRE", "FLAME");
      ALIASENCHANTMENTS.put("firearrow", FLAME);
      ENCHANTMENTS.put("flame", FLAME);
      ENCHANTMENTS.put("flamearrow", FLAME);
      ALIASENCHANTMENTS.put("af", FLAME);
      Enchantment POWER = RegistryUtil.valueOf(Enchantment.class, "ARROW_DAMAGE", "POWER");
      ENCHANTMENTS.put("arrowdamage", POWER);
      ENCHANTMENTS.put("power", POWER);
      ALIASENCHANTMENTS.put("arrowpower", POWER);
      ALIASENCHANTMENTS.put("ad", POWER);
      Enchantment PUNCH = RegistryUtil.valueOf(Enchantment.class, "ARROW_KNOCKBACK", "PUNCH");
      ENCHANTMENTS.put("arrowknockback", PUNCH);
      ALIASENCHANTMENTS.put("arrowkb", PUNCH);
      ENCHANTMENTS.put("punch", PUNCH);
      ALIASENCHANTMENTS.put("arrowpunch", PUNCH);
      ALIASENCHANTMENTS.put("ak", PUNCH);
      Enchantment INFINITY = RegistryUtil.valueOf(Enchantment.class, "ARROW_INFINITE", "INFINITY");
      ALIASENCHANTMENTS.put("infinitearrows", INFINITY);
      ENCHANTMENTS.put("infarrows", INFINITY);
      ENCHANTMENTS.put("infinity", INFINITY);
      ALIASENCHANTMENTS.put("infinite", INFINITY);
      ALIASENCHANTMENTS.put("unlimited", INFINITY);
      ALIASENCHANTMENTS.put("unlimitedarrows", INFINITY);
      ALIASENCHANTMENTS.put("ai", INFINITY);
      Enchantment LUCK_OF_THE_SEA = RegistryUtil.valueOf(Enchantment.class, "LUCK", "LUCK_OF_THE_SEA");
      ENCHANTMENTS.put("luck", LUCK_OF_THE_SEA);
      ALIASENCHANTMENTS.put("luckofsea", LUCK_OF_THE_SEA);
      ALIASENCHANTMENTS.put("luckofseas", LUCK_OF_THE_SEA);
      ALIASENCHANTMENTS.put("rodluck", LUCK_OF_THE_SEA);
      ENCHANTMENTS.put("lure", Enchantment.LURE);
      ALIASENCHANTMENTS.put("rodlure", Enchantment.LURE);
      ENCHANTMENTS.put("depthstrider", Enchantment.DEPTH_STRIDER);
      ALIASENCHANTMENTS.put("depth", Enchantment.DEPTH_STRIDER);
      ALIASENCHANTMENTS.put("strider", Enchantment.DEPTH_STRIDER);

      try {
         Enchantment frostWalker = Enchantment.getByName("FROST_WALKER");
         if (frostWalker != null) {
            ENCHANTMENTS.put("frostwalker", frostWalker);
            ALIASENCHANTMENTS.put("frost", frostWalker);
            ALIASENCHANTMENTS.put("walker", frostWalker);
         }

         Enchantment mending = Enchantment.getByName("MENDING");
         if (mending != null) {
            ENCHANTMENTS.put("mending", mending);
         }
      } catch (IllegalArgumentException var31) {
      }

      try {
         Enchantment bindingCurse = Enchantment.getByName("BINDING_CURSE");
         if (bindingCurse != null) {
            ENCHANTMENTS.put("bindingcurse", bindingCurse);
            ALIASENCHANTMENTS.put("bindcurse", bindingCurse);
            ALIASENCHANTMENTS.put("binding", bindingCurse);
            ALIASENCHANTMENTS.put("bind", bindingCurse);
         }

         Enchantment vanishingCurse = Enchantment.getByName("VANISHING_CURSE");
         if (vanishingCurse != null) {
            ENCHANTMENTS.put("vanishingcurse", vanishingCurse);
            ALIASENCHANTMENTS.put("vanishcurse", vanishingCurse);
            ALIASENCHANTMENTS.put("vanishing", vanishingCurse);
            ALIASENCHANTMENTS.put("vanish", vanishingCurse);
         }

         Enchantment sweeping = Enchantment.getByName("SWEEPING_EDGE");
         if (sweeping != null) {
            ENCHANTMENTS.put("sweepingedge", sweeping);
            ALIASENCHANTMENTS.put("sweepedge", sweeping);
            ALIASENCHANTMENTS.put("sweeping", sweeping);
         }
      } catch (IllegalArgumentException var30) {
      }

      try {
         Enchantment loyalty = Enchantment.getByName("LOYALTY");
         if (loyalty != null) {
            ENCHANTMENTS.put("loyalty", loyalty);
            ALIASENCHANTMENTS.put("loyal", loyalty);
            ALIASENCHANTMENTS.put("return", loyalty);
         }

         Enchantment impaling = Enchantment.getByName("IMPALING");
         if (impaling != null) {
            ENCHANTMENTS.put("impaling", impaling);
            ALIASENCHANTMENTS.put("impale", impaling);
            ALIASENCHANTMENTS.put("oceandamage", impaling);
            ALIASENCHANTMENTS.put("oceandmg", impaling);
         }

         Enchantment riptide = Enchantment.getByName("RIPTIDE");
         if (riptide != null) {
            ENCHANTMENTS.put("riptide", riptide);
            ALIASENCHANTMENTS.put("rip", riptide);
            ALIASENCHANTMENTS.put("tide", riptide);
            ALIASENCHANTMENTS.put("launch", riptide);
         }

         Enchantment channelling = Enchantment.getByName("CHANNELING");
         if (channelling != null) {
            ENCHANTMENTS.put("channelling", channelling);
            ALIASENCHANTMENTS.put("chanelling", channelling);
            ALIASENCHANTMENTS.put("channeling", channelling);
            ALIASENCHANTMENTS.put("chaneling", channelling);
            ALIASENCHANTMENTS.put("channel", channelling);
         }
      } catch (IllegalArgumentException var29) {
      }

      try {
         Enchantment multishot = Enchantment.getByName("MULTISHOT");
         if (multishot != null) {
            ENCHANTMENTS.put("multishot", multishot);
            ALIASENCHANTMENTS.put("tripleshot", multishot);
         }

         Enchantment quickCharge = Enchantment.getByName("QUICK_CHARGE");
         if (quickCharge != null) {
            ENCHANTMENTS.put("quickcharge", quickCharge);
            ALIASENCHANTMENTS.put("quickdraw", quickCharge);
            ALIASENCHANTMENTS.put("fastcharge", quickCharge);
            ALIASENCHANTMENTS.put("fastdraw", quickCharge);
         }

         Enchantment piercing = Enchantment.getByName("PIERCING");
         if (piercing != null) {
            ENCHANTMENTS.put("piercing", piercing);
         }
      } catch (IllegalArgumentException var28) {
      }

      try {
         Enchantment soulspeed = Enchantment.getByName("SOUL_SPEED");
         if (soulspeed != null) {
            ENCHANTMENTS.put("soulspeed", soulspeed);
            ALIASENCHANTMENTS.put("soilspeed", soulspeed);
            ALIASENCHANTMENTS.put("sandspeed", soulspeed);
         }
      } catch (IllegalArgumentException var27) {
      }

      try {
         Enchantment swiftSneak = Enchantment.getByName("SWIFT_SNEAK");
         if (swiftSneak != null) {
            ENCHANTMENTS.put("swiftsneak", swiftSneak);
         }
      } catch (IllegalArgumentException var26) {
      }

      try {
         Enchantment breach = Enchantment.getByName("BREACH");
         if (breach != null) {
            ENCHANTMENTS.put("breach", breach);
         }

         Enchantment density = Enchantment.getByName("DENSITY");
         if (density != null) {
            ENCHANTMENTS.put("density", density);
         }

         Enchantment windBurst = Enchantment.getByName("WIND_BURST");
         if (breach != null) {
            ENCHANTMENTS.put("windburst", windBurst);
            ALIASENCHANTMENTS.put("wind", windBurst);
            ALIASENCHANTMENTS.put("burst", windBurst);
         }
      } catch (IllegalArgumentException var25) {
      }

      try {
         Enchantment lunge = Enchantment.getByName("LUNGE");
         if (lunge != null) {
            ENCHANTMENTS.put("lunge", lunge);
         }
      } catch (IllegalArgumentException var24) {
      }

      try {
         Class<?> namespacedKeyClass = Class.forName("org.bukkit.NamespacedKey");
         Class<?> enchantmentClass = Class.forName("org.bukkit.enchantments.Enchantment");
         enchantmentClass.getDeclaredMethod("getByKey", namespacedKeyClass);
         isFlat = true;
      } catch (NoSuchMethodException | ClassNotFoundException var23) {
         isFlat = false;
      }
   }
}
