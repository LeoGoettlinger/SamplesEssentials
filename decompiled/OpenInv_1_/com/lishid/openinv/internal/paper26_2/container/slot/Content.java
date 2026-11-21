package com.lishid.openinv.internal.paper26_2.container.slot;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.jetbrains.annotations.NotNull;

public interface Content {
   void setHolder(@NotNull ServerPlayer var1);

   ItemStack get();

   ItemStack remove();

   ItemStack removePartial(int var1);

   void set(ItemStack var1);

   Slot asSlot(Container var1, int var2, int var3, int var4);

   SlotType getSlotType();
}
