package com.earth2me.essentials.api;

import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.MaterialUtil;
import com.earth2me.essentials.utils.NumberUtil;
import com.earth2me.essentials.utils.StringUtil;
import java.util.Collection;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

@Deprecated
public interface IItemDb {
   default ItemStack get(final String name, final int quantity) throws Exception {
      ItemStack stack = this.get(name);
      stack.setAmount(quantity);
      return stack;
   }

   ItemStack get(final String name) throws Exception;

   default String names(final ItemStack item) {
      List<String> nameList = this.nameList(item);
      if (nameList.size() > 15) {
         nameList = nameList.subList(0, 14);
      }

      return StringUtil.joinList(", ", nameList);
   }

   List<String> nameList(ItemStack item);

   String name(ItemStack item);

   List<ItemStack> getMatching(User user, String[] args) throws Exception;

   String serialize(ItemStack is);

   Collection<String> listNames();

   @Deprecated
   default Material getFromLegacyId(final int id) {
      return this.getFromLegacy(id, (byte)0);
   }

   @Deprecated
   int getLegacyId(Material material) throws Exception;

   default Material getFromLegacy(final String item) {
      String[] split = item.split(":");
      if (!NumberUtil.isInt(split[0])) {
         return null;
      } else {
         int id = Integer.parseInt(split[0]);
         byte damage = 0;
         if (split.length > 1 && NumberUtil.isInt(split[1])) {
            damage = Byte.parseByte(split[1]);
         }

         return this.getFromLegacy(id, damage);
      }
   }

   default Material getFromLegacy(final int id, final byte damage) {
      return MaterialUtil.convertFromLegacy(id, damage);
   }

   boolean isReady();
}
