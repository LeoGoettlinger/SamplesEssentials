package de.tr7zw.nbtapi.plugin.tests.items;

import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.NbtApiException;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import de.tr7zw.nbtapi.plugin.tests.Test;
import de.tr7zw.nbtapi.utils.DataFixerUtil;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public class LegacyItemTest implements Test {
   @Override
   public void test() throws Exception {
      ItemStack item = NBT.itemStackFromNBT(NBT.parseNBT("{id:cobblestone,Count:42,tag:{Enchantments:[{lvl:3,id:unbreaking}]}}"));
      if (item.getType() == Material.COBBLESTONE && item.getAmount() == 42 && item.getEnchantmentLevel(Enchantment.DURABILITY) == 3) {
         ReadWriteNBT nbt = NBT.parseNBT("{id:cobblestone,Count:42,tag:{display:{Name:\"test\"},ench:[{lvl:3,id:34}]}}");
         nbt = DataFixerUtil.fixUpItemData(nbt, 1343, DataFixerUtil.getCurrentVersion());
         item = NBT.itemStackFromNBT(nbt);
         if (item.getType() == Material.COBBLESTONE
            && item.getAmount() == 42
            && item.getEnchantmentLevel(Enchantment.DURABILITY) == 3
            && "test".equals(item.getItemMeta().getDisplayName())) {
            ItemStack item2 = NBT.itemStackFromNBT(
               NBT.parseNBT("{DataVersion:1343,id:cobblestone,Count:42,tag:{display:{Name:\"test\"},ench:[{lvl:3,id:34}]}}")
            );
            if (!item.equals(item2)) {
               throw new NbtApiException("Data-fixed 1.12.2 item didn't load correctly! " + item2);
            } else {
               ReadWriteNBT testNBT = NBT.parseNBT("{id:cobblestone,Count:42,tag:{display:{Name:\"test\"},ench:[{lvl:3,id:34}]},DataVersion:1343}");
               testNBT = DataFixerUtil.fixUpItemData(testNBT, 1343);
               if (testNBT.getInteger("DataVersion") != DataFixerUtil.getCurrentVersion()) {
                  throw new NbtApiException("DataVersion wasn't updated correctly! " + testNBT);
               }
            }
         } else {
            throw new NbtApiException("1.12.2 item didn't load correctly! " + item);
         }
      } else {
         throw new NbtApiException("1.20 item didn't load correctly! " + item);
      }
   }
}
