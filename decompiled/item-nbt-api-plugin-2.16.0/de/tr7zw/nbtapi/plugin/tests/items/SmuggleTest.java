package de.tr7zw.nbtapi.plugin.tests.items;

import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.NbtApiException;
import de.tr7zw.nbtapi.iface.ReadWriteNBTList;
import de.tr7zw.nbtapi.iface.ReadableNBTList;
import de.tr7zw.nbtapi.plugin.tests.Test;
import java.util.concurrent.atomic.AtomicReference;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class SmuggleTest implements Test {
   @Override
   public void test() throws Exception {
      ItemStack item = new ItemStack(Material.STONE);
      Exception error = null;

      try {
         ReadWriteNBTList<Long> list = NBT.modify(item, nbt -> {
            nbt.getLongList("test").add(123L);
            return nbt.getLongList("test");
         });
         error = new NbtApiException("Managed to smuggle nbt out of the context: " + list);
      } catch (Exception var7) {
      }

      if (error != null) {
         throw error;
      } else {
         try {
            ReadableNBTList<Long> list = NBT.get(item, nbt -> nbt.getLongList("test"));
            error = new NbtApiException("Managed to smuggle nbt out of the context: " + list);
         } catch (Exception var6) {
         }

         if (error != null) {
            throw error;
         } else {
            AtomicReference<ReadableNBTList<Long>> list = new AtomicReference<>();

            try {
               NBT.get(item, nbt -> {
                  list.set(nbt.getLongList("test"));
                  return null;
               });
               if (list.get().size() == 1) {
                  error = new NbtApiException("Managed to smuggle nbt out of the context: " + list);
               }

               error = new NbtApiException("Managed to smuggle nbt out of the context: " + list);
            } catch (Exception var5) {
            }

            if (error != null) {
               throw error;
            }
         }
      }
   }
}
