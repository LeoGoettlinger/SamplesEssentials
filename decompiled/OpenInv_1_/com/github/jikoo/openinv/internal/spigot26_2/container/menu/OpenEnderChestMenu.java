package com.github.jikoo.openinv.internal.spigot26_2.container.menu;

import com.github.jikoo.openinv.internal.spigot26_2.container.OpenEnderChest;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class OpenEnderChestMenu extends OpenSyncMenu<OpenEnderChest> {
   public OpenEnderChestMenu(@NotNull OpenEnderChest enderChest, @NotNull ServerPlayer viewer, int containerId, boolean viewOnly) {
      super(getChestMenuType(enderChest.getContainerSize()), containerId, enderChest, viewer, viewOnly);
   }

   @NotNull
   public ItemStack quickMoveStack(@NotNull Player player, int index) {
      if (this.viewOnly) {
         return ItemStack.EMPTY;
      } else {
         Slot slot = (Slot)this.slots.get(index);
         if (!slot.isFake() && slot.hasItem()) {
            ItemStack itemStack = slot.getItem();
            ItemStack original = itemStack.copy();
            if (index < this.topSize) {
               if (!this.moveItemStackTo(itemStack, this.topSize, this.slots.size(), true)) {
                  return ItemStack.EMPTY;
               }
            } else if (!this.moveItemStackTo(itemStack, 0, this.topSize, false)) {
               return ItemStack.EMPTY;
            }

            if (itemStack.isEmpty()) {
               slot.setByPlayer(ItemStack.EMPTY);
            } else {
               slot.setChanged();
            }

            return original;
         } else {
            return ItemStack.EMPTY;
         }
      }
   }
}
