package com.github.jikoo.openinv.internal.spigot26_2.container.bukkit;

import com.github.jikoo.openinv.internal.spigot26_2.container.OpenInventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class OpenPlayerInventorySelf extends OpenPlayerInventory {
   private final int offset;

   public OpenPlayerInventorySelf(@NotNull OpenInventory inventory, int offset) {
      super(inventory);
      this.offset = offset;
   }

   public ItemStack getItem(int index) {
      return super.getItem(this.offset + index);
   }

   public void setItem(int index, ItemStack item) {
      super.setItem(this.offset + index, item);
   }
}
