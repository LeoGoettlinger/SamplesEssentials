package com.artillexstudios.axtrade.utils;

import com.artillexstudios.axtrade.hooks.HookManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.block.Container;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.BundleMeta;
import org.jetbrains.annotations.NotNull;

public class ShulkerUtils {
   public static List<ItemStack> getStorageContents(ItemStack item, boolean includeSelf) {
      return item.getType().name().endsWith("BUNDLE") ? getBundleContents(item, includeSelf) : getShulkerContents(item, includeSelf);
   }

   @NotNull
   private static List<ItemStack> getShulkerContents(ItemStack item, boolean includeSelf) {
      if (item == null) {
         return new ArrayList<>();
      } else {
         if (HookManager.getAxShulkersHook() != null) {
            ItemStack[] items = HookManager.getAxShulkersHook().getItems(item);
            if (items != null) {
               List<ItemStack> storage = new ArrayList<>(Arrays.asList(items));
               if (includeSelf) {
                  storage.add(item);
               }

               return storage;
            }
         }

         if (item.getItemMeta() instanceof BlockStateMeta meta) {
            if (meta.getBlockState() instanceof Container container) {
               ArrayList var8 = new ArrayList<>(Arrays.asList(container.getInventory().getStorageContents()));
               if (includeSelf) {
                  var8.add(item);
               }

               return var8;
            } else {
               return List.of(item);
            }
         } else {
            return List.of(item);
         }
      }
   }

   @NotNull
   private static List<ItemStack> getBundleContents(ItemStack item, boolean includeSelf) {
      if (item == null) {
         return new ArrayList<>();
      } else if (item.getItemMeta() instanceof BundleMeta meta) {
         ArrayList var4 = new ArrayList(meta.getItems());
         if (includeSelf) {
            var4.add(item);
         }

         return var4;
      } else {
         return List.of(item);
      }
   }
}
