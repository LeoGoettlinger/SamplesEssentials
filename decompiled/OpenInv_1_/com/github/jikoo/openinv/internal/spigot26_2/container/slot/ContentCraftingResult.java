package com.github.jikoo.openinv.internal.spigot26_2.container.slot;

import com.github.jikoo.openinv.internal.spigot26_2.container.slot.placeholder.Placeholders;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.jetbrains.annotations.NotNull;

public class ContentCraftingResult extends ContentViewOnly {
   public ContentCraftingResult(@NotNull ServerPlayer holder) {
      super(holder);
   }

   @Override
   public ItemStack get() {
      InventoryMenu inventoryMenu = this.holder.inventoryMenu;
      return inventoryMenu.getResultSlot().getItem();
   }

   @Override
   public Slot asSlot(Container container, int slot, int x, int y) {
      return new SlotViewOnly(container, slot, x, y) {
         @Override
         public ItemStack getOrDefault() {
            if (!ContentCrafting.isAvailable(ContentCraftingResult.this.holder)) {
               return Placeholders.survivalOnly(ContentCraftingResult.this.holder);
            } else {
               InventoryMenu inventoryMenu = ContentCraftingResult.this.holder.inventoryMenu;
               return inventoryMenu.getResultSlot().getItem();
            }
         }
      };
   }

   @Override
   public SlotType getSlotType() {
      return SlotType.RESULT;
   }
}
