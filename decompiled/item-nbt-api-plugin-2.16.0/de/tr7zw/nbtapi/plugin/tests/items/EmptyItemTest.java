package de.tr7zw.nbtapi.plugin.tests.items;

import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.NBTItem;
import de.tr7zw.nbtapi.NbtApiException;
import de.tr7zw.nbtapi.plugin.tests.Test;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class EmptyItemTest implements Test {
   @Override
   public void test() throws Exception {
      ItemStack item = new ItemStack(Material.STONE);
      NBTItem nbti = new NBTItem(item);
      if (nbti.hasNBTData()) {
         throw new NbtApiException("Item reported to have data");
      } else if (nbti.getBoolean("test") == null || nbti.getString("test") == null || nbti.getFloatList("test") == null) {
         throw new NbtApiException("Getters return null instead of the default value");
      } else if (!nbti.getKeys().isEmpty()) {
         throw new NbtApiException("getKeys() returned keys!");
      } else {
         nbti.removeKey("test");
         if (nbti.hasNBTData()) {
            throw new NbtApiException("Item reported to have data after remove");
         } else {
            nbti.setString("test", "test");
            if (!nbti.hasNBTData()) {
               throw new NbtApiException("Item reported to have no data");
            } else {
               nbti.removeKey("test");
               if (nbti.hasNBTData()) {
                  throw new NbtApiException("Item reported to have data after deletion");
               } else {
                  try {
                     Material barrel = Material.valueOf("BARREL");
                     item = new ItemStack(barrel);
                     new NBTItem(item);
                  } catch (IllegalArgumentException var5) {
                  }

                  item = new ItemStack(Material.STONE);
                  NBT.modify(item, nbtx -> nbtx.setString("test", "test"));
                  NBT.get(item, nbtx -> {
                     if (!nbtx.hasNBTData()) {
                        throw new NbtApiException("Item reported to have no data");
                     } else {
                        return nbtx.hasNBTData();
                     }
                  });
                  NBT.modify(item, nbtx -> nbtx.removeKey("test"));
                  NBT.get(item, nbtx -> {
                     if (nbtx.hasNBTData()) {
                        throw new NbtApiException("Item reported to have data after deletion");
                     } else {
                        return nbtx.hasNBTData();
                     }
                  });
                  ItemStack testItem = new ItemStack(Material.STONE);
                  NBTItem nbt = new NBTItem(testItem, true);
                  nbt.removeKey("not there");
                  nbt.setBoolean("test", true);
               }
            }
         }
      }
   }
}
