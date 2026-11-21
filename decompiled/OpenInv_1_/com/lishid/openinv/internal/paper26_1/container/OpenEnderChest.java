package com.lishid.openinv.internal.paper26_1.container;

import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class OpenEnderChest extends com.lishid.openinv.internal.paper26_2.container.OpenEnderChest {
   public OpenEnderChest(@NotNull Player player) {
      super(player);
   }

   @NotNull
   @Override
   protected NonNullList<ItemStack> getEnderChestItems(@NotNull ServerPlayer owner) {
      return owner.getEnderChestInventory().items;
   }
}
