package com.lishid.openinv.internal.paper26_2.container.menu;

import com.lishid.openinv.internal.paper26_2.container.OpenEnderChest;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class OpenEnderChestMenu extends OpenSyncMenu<OpenEnderChest> {
   public OpenEnderChestMenu(OpenEnderChest enderChest, ServerPlayer viewer, int containerId, boolean viewOnly) {
      super(getChestMenuType(enderChest.getContainerSize()), containerId, enderChest, viewer, viewOnly);
   }

   public ItemStack quickMoveStack(Player player, int index) {
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

   public void clicked(int slotIndex, int buttonNum, ContainerInput containerInput, Player player) {
      if (this.viewOnly && containerInput == ContainerInput.QUICK_CRAFT) {
         this.sendAllDataToRemote();
      }

      super.clicked(slotIndex, buttonNum, containerInput, player);
   }
}
