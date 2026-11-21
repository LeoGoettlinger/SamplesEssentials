package com.lishid.openinv.util;

import java.util.function.Predicate;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.BundleMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class SearchHelper {
   public static boolean findMatch(@NotNull Inventory inventory, @NotNull Predicate<ItemStack> predicate) {
      for (ItemStack content : inventory.getContents()) {
         if (findMatch(content, predicate)) {
            return true;
         }
      }

      return false;
   }

   private static boolean findMatch(@Nullable ItemStack itemStack, @NotNull Predicate<ItemStack> predicate) {
      if (itemStack == null || itemStack.getType().isAir()) {
         return false;
      } else if (predicate.test(itemStack)) {
         return true;
      } else if (!itemStack.hasItemMeta()) {
         return false;
      } else {
         ItemMeta meta = itemStack.getItemMeta();
         if (meta instanceof BlockStateMeta stateMeta) {
            if (stateMeta.hasBlockState() && stateMeta.getBlockState() instanceof InventoryHolder holder) {
               Inventory inventory = holder.getInventory();
               return findMatch(inventory, predicate);
            } else {
               return false;
            }
         } else {
            if (meta instanceof BundleMeta bundleMeta) {
               for (ItemStack subStack : bundleMeta.getItems()) {
                  if (findMatch(subStack, predicate)) {
                     return true;
                  }
               }
            }

            return false;
         }
      }
   }

   private SearchHelper() {
      throw new IllegalStateException("Cannot create instance of utility class.");
   }
}
