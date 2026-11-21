package com.github.jikoo.openinv.internal.spigot26_2.container.slot;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public abstract class SlotPlaceholder extends Slot {
   public SlotPlaceholder(Container container, int index, int x, int y) {
      super(container, index, x, y);
   }

   public abstract ItemStack getOrDefault();
}
