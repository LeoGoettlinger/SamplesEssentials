package com.github.jikoo.openinv.internal.spigot26_2.container.slot;

import com.github.jikoo.openinv.internal.spigot26_2.container.slot.placeholder.Placeholders;
import com.github.jikoo.openinv.internal.spigot26_2.player.OpenPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.jetbrains.annotations.NotNull;

public class ContentCursor implements Content {
   @NotNull
   private ServerPlayer holder;

   public ContentCursor(@NotNull ServerPlayer holder) {
      this.holder = holder;
   }

   @Override
   public void setHolder(@NotNull ServerPlayer holder) {
      this.holder = holder;
   }

   @Override
   public ItemStack get() {
      return this.isAvailable() ? this.holder.containerMenu.getCarried() : ItemStack.EMPTY;
   }

   @Override
   public ItemStack remove() {
      ItemStack carried = this.holder.containerMenu.getCarried();
      this.holder.containerMenu.setCarried(ItemStack.EMPTY);
      return carried;
   }

   @Override
   public ItemStack removePartial(int amount) {
      ItemStack carried = this.holder.containerMenu.getCarried();
      if (!carried.isEmpty() && carried.getCount() >= amount) {
         ItemStack value = carried.split(amount);
         if (carried.isEmpty()) {
            this.holder.containerMenu.setCarried(ItemStack.EMPTY);
         }

         return value;
      } else {
         return ItemStack.EMPTY;
      }
   }

   @Override
   public void set(ItemStack itemStack) {
      if (this.isAvailable()) {
         this.holder.containerMenu.setCarried(itemStack);
      } else {
         this.holder.drop(itemStack, false);
      }
   }

   private boolean isAvailable() {
      return OpenPlayer.isConnected(this.holder.connection) && this.holder.gameMode.isSurvival();
   }

   @Override
   public Slot asSlot(Container container, int slot, int x, int y) {
      return new ContentCursor.SlotCursor(container, slot, x, y);
   }

   @Override
   public SlotType getSlotType() {
      return SlotType.OUTSIDE;
   }

   public class SlotCursor extends SlotPlaceholder {
      private SlotCursor(Container container, int index, int x, int y) {
         super(container, index, x, y);
      }

      @Override
      public ItemStack getOrDefault() {
         if (!ContentCursor.this.isAvailable()) {
            return Placeholders.survivalOnly(ContentCursor.this.holder);
         } else {
            ItemStack carried = ContentCursor.this.holder.containerMenu.getCarried();
            return carried.isEmpty() ? Placeholders.cursor : carried;
         }
      }

      public boolean mayPickup(@NotNull Player player) {
         return ContentCursor.this.isAvailable();
      }

      public boolean mayPlace(@NotNull ItemStack itemStack) {
         return ContentCursor.this.isAvailable();
      }

      public boolean hasItem() {
         return ContentCursor.this.isAvailable() && super.hasItem();
      }

      public boolean isFake() {
         return true;
      }
   }
}
