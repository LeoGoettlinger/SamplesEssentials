package com.artillexstudios.axtrade.utils;

import com.artillexstudios.axtrade.AxTrade;
import com.artillexstudios.axtrade.libs.axapi.items.WrappedItemStack;
import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.block.implementation.Section;
import java.util.List;
import java.util.Map;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class BlacklistUtils {
   public static boolean isBlacklisted(@Nullable ItemStack it) {
      if (it == null || it.getType().isAir()) {
         return false;
      } else if (checkLegacy(it)) {
         return true;
      } else {
         try {
            List<Map<String, Object>> list = AxTrade.CONFIG.getMapList("blacklist-items");
            if (list == null || list.isEmpty()) {
               return false;
            }

            for (ItemStack stack : ShulkerUtils.getStorageContents(it, true)) {
               if (stack != null && !stack.getType().isAir()) {
                  WrappedItemStack wrap = WrappedItemStack.wrap(stack);

                  for (Map<String, Object> map : list) {
                     ItemMatcher matcher = new ItemMatcher(wrap, map);
                     boolean result = matcher.isMatching();
                     if (result) {
                        return true;
                     }
                  }
               }
            }
         } catch (Exception var9) {
            var9.printStackTrace();
         }

         return false;
      }
   }

   private static boolean checkLegacy(ItemStack it) {
      Section section = AxTrade.CONFIG.getSection("blacklisted-items");
      if (section == null) {
         return false;
      } else {
         for (String s : section.getRoutesAsStrings(false)) {
            if (AxTrade.CONFIG.getString("blacklisted-items." + s + ".material") != null) {
               if (it.getType().toString().equalsIgnoreCase(AxTrade.CONFIG.getString("blacklisted-items." + s + ".material"))) {
                  return true;
               }
            } else if (AxTrade.CONFIG.getString("blacklisted-items." + s + ".name-contains") != null
               && it.getItemMeta() != null
               && it.getItemMeta().getDisplayName().contains(AxTrade.CONFIG.getString("blacklisted-items." + s + ".name-contains"))) {
               return true;
            }
         }

         return false;
      }
   }
}
