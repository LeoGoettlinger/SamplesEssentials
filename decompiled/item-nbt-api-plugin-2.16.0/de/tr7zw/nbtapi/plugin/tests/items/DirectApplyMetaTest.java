package de.tr7zw.nbtapi.plugin.tests.items;

import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.NBTItem;
import de.tr7zw.nbtapi.NbtApiException;
import de.tr7zw.nbtapi.iface.ReadableNBT;
import de.tr7zw.nbtapi.plugin.tests.Test;
import de.tr7zw.nbtapi.utils.MinecraftVersion;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class DirectApplyMetaTest implements Test {
   @Override
   public void test() throws Exception {
      if (!MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_20_R4)) {
         ItemStack baseItem = new ItemStack(Material.STONE);
         NBTItem nbti = new NBTItem(baseItem, true);
         nbti.setString("SomeKey", "SomeValue");
         nbti.modifyMeta(this::modifyMeta);
         if (!new NBTItem(baseItem).hasTag("SomeKey") || !"SomeValue".equals(baseItem.getItemMeta().getDisplayName())) {
            throw new NbtApiException("The item was not modified correctly! " + NBT.itemStackToNBT(baseItem));
         }
      }
   }

   private void modifyMeta(ReadableNBT nbt, ItemMeta meta) {
      meta.setDisplayName(nbt.getString("SomeKey"));
   }
}
