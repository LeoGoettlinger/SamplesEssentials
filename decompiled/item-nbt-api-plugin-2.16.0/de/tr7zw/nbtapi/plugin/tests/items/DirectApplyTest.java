package de.tr7zw.nbtapi.plugin.tests.items;

import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.NBTItem;
import de.tr7zw.nbtapi.NbtApiException;
import de.tr7zw.nbtapi.plugin.tests.Test;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class DirectApplyTest implements Test {
   @Override
   public void test() throws Exception {
      ItemStack baseItem = new ItemStack(Material.STONE);
      NBTItem nbti = new NBTItem(baseItem, true);
      nbti.setString("SomeKey", "SomeValue");
      if (baseItem.equals(nbti.getItem()) && new NBTItem(baseItem).hasTag("SomeKey")) {
         baseItem = new ItemStack(Material.STONE);
         String inside = NBT.modify(baseItem, nbt -> {
            nbt.setString("SomeKey", "SomeValue");
            return nbt.getString("SomeKey");
         });
         baseItem = NBT.itemStackFromNBT(NBT.itemStackToNBT(baseItem));
         String outside = NBT.get(baseItem, nbt -> nbt.getString("SomeKey"));
         if (!new NBTItem(baseItem).hasTag("SomeKey")) {
            throw new NbtApiException("The data was not applied!");
         } else if (!"SomeValue".equals(inside)) {
            throw new NbtApiException("Inside returned the wrong value!");
         } else if (!"SomeValue".equals(outside)) {
            throw new NbtApiException("Outside returned the wrong value!");
         }
      } else {
         throw new NbtApiException("The item's where not equal!");
      }
   }
}
