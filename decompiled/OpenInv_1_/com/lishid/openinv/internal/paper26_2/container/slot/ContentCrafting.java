package com.lishid.openinv.internal.paper26_2.container.slot;

import com.lishid.openinv.internal.paper26_2.container.slot.placeholder.Placeholders;
import com.lishid.openinv.internal.paper26_2.player.OpenPlayer;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.jetbrains.annotations.NotNull;

public class ContentCrafting implements Content {
   private final int index;
   private ServerPlayer holder;
   private List<ItemStack> items;

   public ContentCrafting(@NotNull ServerPlayer holder, int index) {
      this.setHolder(holder);
      this.index = index;
   }

   private boolean isAvailable() {
      return isAvailable(this.holder);
   }

   public static boolean isAvailable(@NotNull ServerPlayer holder) {
      return OpenPlayer.isConnected(holder.connection) && holder.gameMode.isSurvival();
   }

   @Override
   public void setHolder(@NotNull ServerPlayer holder) {
      this.holder = holder;
      this.items = holder.inventoryMenu.getCraftSlots().getContents();
   }

   @Override
   public ItemStack get() {
      return this.isAvailable() ? this.items.get(this.index) : ItemStack.EMPTY;
   }

   @Override
   public ItemStack remove() {
      if (!this.isAvailable()) {
         return ItemStack.EMPTY;
      } else {
         ItemStack removed = this.items.remove(this.index);
         if (removed.isEmpty()) {
            return ItemStack.EMPTY;
         } else {
            this.holder.inventoryMenu.slotsChanged(this.holder.inventoryMenu.getCraftSlots());
            return removed;
         }
      }
   }

   @Override
   public ItemStack removePartial(int amount) {
      if (!this.isAvailable()) {
         return ItemStack.EMPTY;
      } else {
         ItemStack removed = ContainerHelper.removeItem(this.items, this.index, amount);
         if (removed.isEmpty()) {
            return ItemStack.EMPTY;
         } else {
            this.holder.inventoryMenu.slotsChanged(this.holder.inventoryMenu.getCraftSlots());
            return removed;
         }
      }
   }

   @Override
   public void set(ItemStack itemStack) {
      if (this.isAvailable()) {
         this.items.set(this.index, itemStack);
         this.holder.inventoryMenu.slotsChanged(this.holder.inventoryMenu.getCraftSlots());
      } else {
         this.holder.drop(itemStack, false);
      }
   }

   @Override
   public Slot asSlot(Container container, int slot, int x, int y) {
      return new ContentCrafting.SlotCrafting(container, slot, x, y);
   }

   @Override
   public SlotType getSlotType() {
      return this.isAvailable() ? SlotType.CRAFTING : SlotType.OUTSIDE;
   }

   public class SlotCrafting extends SlotPlaceholder {
      private SlotCrafting(Container container, int index, int x, int y) {
         super(container, index, x, y);
      }

      @Override
      public ItemStack getOrDefault() {
         return ContentCrafting.this.isAvailable()
            ? ContentCrafting.this.items.get(ContentCrafting.this.index)
            : Placeholders.survivalOnly(ContentCrafting.this.holder);
      }

      public boolean mayPickup(@NotNull Player player) {
         return ContentCrafting.this.isAvailable();
      }

      public boolean mayPlace(@NotNull ItemStack itemStack) {
         return ContentCrafting.this.isAvailable();
      }

      public boolean hasItem() {
         return ContentCrafting.this.isAvailable() && super.hasItem();
      }

      public boolean isFake() {
         return !ContentCrafting.this.isAvailable();
      }
   }
}
