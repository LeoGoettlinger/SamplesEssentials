package com.skyblockexp.ezrtp.util;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class ItemFlagUtil {
   private ItemFlagUtil() {
   }

   public static void applyStandardHideFlags(ItemMeta meta) {
      ItemFlagCompat.applyStandardHideFlags(meta);
   }

   public static void setItemMetaCompatibly(ItemStack itemStack, ItemMeta meta) {
      ItemFlagCompat.setItemMetaCompatibly(itemStack, meta);
   }
}
