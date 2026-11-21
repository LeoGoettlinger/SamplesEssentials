package com.magmaguy.elitemobs.items.customloottable;

import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.utils.ObjectSerializer;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemStackCustomLootEntry extends CustomLootEntry implements Serializable {
   private final String serializedItemStack;

   public ItemStackCustomLootEntry(List<CustomLootEntry> entries, ItemStack itemStack) {
      this.serializedItemStack = ObjectSerializer.itemStackArrayToBase64(itemStack);
      entries.add(this);
   }

   public ItemStack generateItemStack() {
      try {
         return ObjectSerializer.itemStackArrayFromBase64(this.serializedItemStack);
      } catch (Exception var2) {
         Logger.warn("Failed to serialize item stack from Custom Loot Table");
         return null;
      }
   }

   @Override
   public boolean directDrop(int itemTier, Player player) {
      ItemStack itemStack = this.generateItemStack();
      if (itemStack != null && !itemStack.getType().isAir() && itemStack.getAmount() > 0 && this.getAmount() > 0) {
         for (int i = 0; i < this.getAmount(); i++) {
            HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(new ItemStack[]{itemStack.clone()});
            overflow.values().forEach(leftover -> player.getWorld().dropItem(player.getLocation(), leftover));
         }

         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean locationDrop(int itemTier, Player player, Location location) {
      ItemStack itemStack = this.generateItemStack();
      if (itemStack != null && !itemStack.getType().isAir() && itemStack.getAmount() > 0 && this.getAmount() > 0) {
         for (int i = 0; i < this.getAmount(); i++) {
            location.getWorld().dropItem(location, itemStack.clone());
         }

         return true;
      } else {
         return false;
      }
   }

   @Override
   public ItemStack previewDrop(int itemTier, Player player) {
      return this.generateItemStack();
   }
}
