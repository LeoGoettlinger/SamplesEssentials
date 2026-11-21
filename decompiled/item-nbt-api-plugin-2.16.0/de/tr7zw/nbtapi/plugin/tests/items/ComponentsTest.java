package de.tr7zw.nbtapi.plugin.tests.items;

import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.NbtApiException;
import de.tr7zw.nbtapi.plugin.tests.Test;
import de.tr7zw.nbtapi.utils.MinecraftVersion;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ComponentsTest implements Test {
   @Override
   public void test() throws Exception {
      if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_20_R4)) {
         ItemStack item = new ItemStack(Material.STICK);
         ItemMeta meta = item.getItemMeta();
         meta.setDisplayName("test");
         item.setItemMeta(meta);
         String comp = NBT.modifyComponents(item, n -> n.toString());
         if (!comp.contains("test")) {
            throw new NbtApiException("ReadComponent didn't work!");
         } else {
            NBT.modifyComponents(item, nbt -> {
               if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_21_R4)) {
                  nbt.mergeCompound(NBT.parseNBT("{\"minecraft:custom_name\":[{\"text\":\"foobar\",\"italic\":false}]}"));
               } else {
                  nbt.setString("minecraft:custom_name", "{\"extra\":[\"foobar\"],\"text\":\"\"}");
               }
            });
            if (!item.getItemMeta().getDisplayName().equals("foobar")) {
               throw new NbtApiException("ModifyComponent didn't work!");
            }
         }
      }
   }
}
