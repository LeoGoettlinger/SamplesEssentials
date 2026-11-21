package com.lishid.openinv.internal.paper26_2.container.slot;

import com.lishid.openinv.internal.paper26_2.container.slot.placeholder.Placeholders;
import java.util.Optional;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SlotViewOnly extends SlotPlaceholder {
   @NotNull
   public static SlotViewOnly wrap(@NotNull final Slot wrapped) {
      SlotViewOnly wrapper;
      if (wrapped instanceof SlotPlaceholder placeholder) {
         wrapper = new SlotViewOnly(wrapped.container, wrapped.getContainerSlot(), wrapped.x, wrapped.y) {
            @Override
            public ItemStack getOrDefault() {
               return placeholder.getOrDefault();
            }
         };
      } else {
         wrapper = new SlotViewOnly(wrapped.container, wrapped.getContainerSlot(), wrapped.x, wrapped.y) {
            @Override
            public ItemStack getOrDefault() {
               return wrapped.getItem();
            }
         };
      }

      wrapper.index = wrapped.index;
      return wrapper;
   }

   public SlotViewOnly(Container container, int index, int x, int y) {
      super(container, index, x, y);
   }

   @Override
   public ItemStack getOrDefault() {
      return Placeholders.notSlot;
   }

   public void onQuickCraft(@NotNull ItemStack itemStack1, @NotNull ItemStack itemStack2) {
   }

   public void onTake(@NotNull Player player, @NotNull ItemStack itemStack) {
   }

   public boolean mayPlace(@NotNull ItemStack itemStack) {
      return false;
   }

   @NotNull
   public ItemStack getItem() {
      return ItemStack.EMPTY;
   }

   public boolean hasItem() {
      return false;
   }

   public void setByPlayer(@NotNull ItemStack newStack) {
   }

   public void setByPlayer(@NotNull ItemStack newStack, @NotNull ItemStack oldStack) {
   }

   public void set(@NotNull ItemStack itemStack) {
   }

   public void setChanged() {
   }

   public int getMaxStackSize() {
      return 0;
   }

   public int getMaxStackSize(@NotNull ItemStack itemStack) {
      return 0;
   }

   @NotNull
   public ItemStack remove(int amount) {
      return ItemStack.EMPTY;
   }

   public boolean mayPickup(@NotNull Player player) {
      return false;
   }

   public boolean isActive() {
      return false;
   }

   @NotNull
   public Optional<ItemStack> tryRemove(int var0, int var1, @NotNull Player player) {
      return Optional.empty();
   }

   @NotNull
   public ItemStack safeTake(int var0, int var1, @NotNull Player player) {
      return ItemStack.EMPTY;
   }

   @NotNull
   public ItemStack safeClone(@NotNull Player player) {
      return ItemStack.EMPTY;
   }

   @NotNull
   public ItemStack safeInsert(@NotNull ItemStack itemStack) {
      return itemStack;
   }

   @NotNull
   public ItemStack safeInsert(@NotNull ItemStack itemStack, int amount) {
      return itemStack;
   }

   public boolean allowModification(@NotNull Player player) {
      return false;
   }

   public boolean isHighlightable() {
      return false;
   }

   public boolean isFake() {
      return true;
   }
}
