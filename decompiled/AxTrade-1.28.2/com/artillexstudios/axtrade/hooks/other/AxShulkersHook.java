package com.artillexstudios.axtrade.hooks.other;

import com.artillexstudios.axshulkers.AxShulkers;
import com.artillexstudios.axshulkers.cache.Shulkerbox;
import com.artillexstudios.axshulkers.cache.Shulkerboxes;
import com.artillexstudios.axshulkers.utils.ShulkerUtils;
import java.util.UUID;
import org.bukkit.inventory.ItemStack;

public class AxShulkersHook {
   public ItemStack[] getItems(ItemStack item) {
      if (!ShulkerUtils.isShulker(item)) {
         return null;
      } else {
         UUID uuid = ShulkerUtils.getShulkerUUID(item);
         if (uuid == null) {
            return null;
         } else {
            Shulkerbox shulkerbox = (Shulkerbox)Shulkerboxes.getShulkerMap().get(uuid);
            return shulkerbox == null ? AxShulkers.getDB().getShulker(uuid) : shulkerbox.getShulkerInventory().getStorageContents();
         }
      }
   }
}
