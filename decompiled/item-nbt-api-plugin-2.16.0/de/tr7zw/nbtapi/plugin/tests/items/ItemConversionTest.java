package de.tr7zw.nbtapi.plugin.tests.items;

import com.google.common.collect.Lists;
import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.NBTType;
import de.tr7zw.nbtapi.NbtApiException;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import de.tr7zw.nbtapi.plugin.tests.Test;
import de.tr7zw.nbtapi.utils.MinecraftVersion;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemConversionTest implements Test {
   @Override
   public void test() throws Exception {
      ItemStack item = new ItemStack(Material.STONE, 1);
      ItemMeta meta = item.getItemMeta();
      meta.setLore(Lists.newArrayList(new String[]{"Firest Line", "Second Line"}));
      item.setItemMeta(meta);
      ReadWriteNBT nbt = NBT.itemStackToNBT(item);
      if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_12_R1) && !nbt.hasTag("DataVersion", NBTType.NBTTagInt)) {
         throw new NbtApiException("The item nbt '" + nbt + "' didn't contain the data version");
      } else {
         String nbtString = nbt.toString();
         if (nbtString.contains("Firest Line") && nbtString.contains("Second Line")) {
            ItemStack rebuild = NBT.itemStackFromNBT(NBT.parseNBT(nbtString));
            if (!item.isSimilar(rebuild)) {
               throw new NbtApiException("Rebuilt item did not match the original!");
            } else {
               ReadWriteNBT cont = NBT.createNBTObject();
               cont.setItemStack("testItem", item);
               if (!item.isSimilar(cont.getItemStack("testItem"))) {
                  throw new NbtApiException("Rebuilt item did not match the original!");
               }
            }
         } else {
            throw new NbtApiException("The Item nbt '" + nbtString + "' didn't contain the lore");
         }
      }
   }
}
