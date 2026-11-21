package com.earth2me.essentials.craftbukkit;

import java.util.HashMap;
import java.util.List;
import org.bukkit.inventory.ItemStack;

public class InventoryData {
   private final List<Integer> emptySlots;
   private final HashMap<ItemStack, List<Integer>> partialSlots;

   public InventoryData(List<Integer> emptySlots, HashMap<ItemStack, List<Integer>> partialSlots) {
      this.emptySlots = emptySlots;
      this.partialSlots = partialSlots;
   }

   public List<Integer> getEmptySlots() {
      return this.emptySlots;
   }

   public HashMap<ItemStack, List<Integer>> getPartialSlots() {
      return this.partialSlots;
   }
}
