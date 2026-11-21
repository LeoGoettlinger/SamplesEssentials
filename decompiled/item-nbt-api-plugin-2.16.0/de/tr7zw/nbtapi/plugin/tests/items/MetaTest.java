package de.tr7zw.nbtapi.plugin.tests.items;

import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.NbtApiException;
import de.tr7zw.nbtapi.plugin.tests.Test;
import de.tr7zw.nbtapi.utils.MinecraftVersion;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

public class MetaTest implements Test {
   @Override
   public void test() throws Exception {
      if (!MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_20_R4)) {
         ItemStack item = new ItemStack(Material.STONE);
         NBT.modify(item, nbt -> {
            nbt.setInteger("HideFlags", 1);
            nbt.modifyMeta((rnbt, meta) -> {
               if (!meta.hasItemFlag(ItemFlag.HIDE_ENCHANTS) || rnbt.getInteger("HideFlags") != 1) {
                  throw new NbtApiException("The meta did not correctly update or read! " + rnbt);
               }
            });
         });
      }
   }
}
