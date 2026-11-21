package com.github.jikoo.openinv.internal.spigot26_2.container.slot;

import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.event.inventory.InventoryType.SlotType;

public abstract class ContentList implements Content {
   private final int index;
   private final SlotType slotType;
   protected List<ItemStack> items;

   public ContentList(ServerPlayer holder, int index, SlotType slotType) {
      this.index = index;
      this.slotType = slotType;
      this.setHolder(holder);
   }

   @Override
   public ItemStack get() {
      return this.items.get(this.index);
   }

   @Override
   public ItemStack remove() {
      ItemStack removed = this.items.remove(this.index);
      return removed != null && !removed.isEmpty() ? removed : ItemStack.EMPTY;
   }

   @Override
   public ItemStack removePartial(int amount) {
      return ContainerHelper.removeItem(this.items, this.index, amount);
   }

   @Override
   public void set(ItemStack itemStack) {
      this.items.set(this.index, itemStack);
   }

   @Override
   public Slot asSlot(Container container, int slot, int x, int y) {
      return new Slot(container, slot, x, y);
   }

   @Override
   public SlotType getSlotType() {
      return this.slotType;
   }
}
