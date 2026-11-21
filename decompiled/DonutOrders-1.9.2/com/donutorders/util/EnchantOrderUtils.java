package com.donutorders.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

public final class EnchantOrderUtils {
   private static final String[] ROMAN = new String[]{"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
   private static List<Enchantment> cachedBookEnchantments;

   private EnchantOrderUtils() {
   }

   public static boolean needsEnchantPicker(Material material) {
      return material == Material.ENCHANTED_BOOK;
   }

   public static void clearBookEnchantmentCache() {
      cachedBookEnchantments = null;
   }

   public static List<Enchantment> listBookEnchantments() {
      if (cachedBookEnchantments != null) {
         return cachedBookEnchantments;
      } else {
         List<Enchantment> enchants = new ArrayList<>();

         for (Enchantment enchant : Registry.ENCHANTMENT) {
            if (enchant.getMaxLevel() > 0) {
               ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
               if (book.getItemMeta() instanceof EnchantmentStorageMeta storageMeta && storageMeta.addStoredEnchant(enchant, 1, true)) {
                  enchants.add(enchant);
               }
            }
         }

         enchants.sort(Comparator.comparing(EnchantOrderUtils::prettyEnchantName));
         cachedBookEnchantments = List.copyOf(enchants);
         return cachedBookEnchantments;
      }
   }

   public static ItemStack buildEnchantedBook(Enchantment enchantment, int level) {
      ItemStack book = new ItemStack(Material.ENCHANTED_BOOK, 1);
      if (book.getItemMeta() instanceof EnchantmentStorageMeta storageMeta) {
         storageMeta.addStoredEnchant(enchantment, level, true);
         book.setItemMeta(storageMeta);
      }

      return book;
   }

   public static String prettyEnchantName(Enchantment enchantment) {
      return enchantment != null && enchantment.getKey() != null ? prettyKey(enchantment.getKey().getKey()) : "";
   }

   public static String describeOrderItem(ItemStack item) {
      if (item == null || item.getType() == Material.AIR) {
         return "";
      } else if (item.getType() == Material.ENCHANTED_BOOK) {
         if (item.getItemMeta() instanceof EnchantmentStorageMeta storageMeta) {
            Map<Enchantment, Integer> stored = storageMeta.getStoredEnchants();
            if (stored.size() == 1) {
               Entry<Enchantment, Integer> entry = stored.entrySet().iterator().next();
               return prettyEnchantName(entry.getKey()) + " " + toRoman(entry.getValue()) + " Enchanted Book";
            }
         }

         return ItemUtils.prettyName(Material.ENCHANTED_BOOK);
      } else {
         return ItemUtils.prettyName(item.getType());
      }
   }

   public static String toRoman(int level) {
      return level >= 1 && level < ROMAN.length ? ROMAN[level] : String.valueOf(level);
   }

   private static String prettyKey(String key) {
      String raw = key.replace('_', ' ').toLowerCase();
      StringBuilder sb = new StringBuilder();
      boolean cap = true;

      for (char c : raw.toCharArray()) {
         sb.append(cap ? Character.toUpperCase(c) : c);
         cap = c == ' ';
      }

      return sb.toString();
   }
}
