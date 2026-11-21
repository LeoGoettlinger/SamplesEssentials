package com.oheers.fish.selling;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;

@Deprecated(
   forRemoval = true
)
public class SellHelper {
   private final Player player;
   private final Inventory inventory;
   private final ItemStack[] array;

   public SellHelper(@NonNull Inventory inventory, @NonNull Player player) {
      this.inventory = inventory;
      this.player = player;
      this.array = null;
   }

   public SellHelper(@NonNull Inventory inventory, @NonNull Player player, boolean removeFromInventory) {
      this(inventory, player);
   }

   public SellHelper(@NonNull ItemStack[] itemStacks, @NonNull Player player, boolean removeStacks) {
      this.inventory = null;
      this.player = player;
      this.array = itemStacks;
   }

   public SellHelper(@NonNull ItemStack[] itemStacks, @NonNull Player player) {
      this(itemStacks, player, true);
   }

   @Deprecated(
      since = "2.1.4",
      forRemoval = true
   )
   public void sellFish() {
      this.sell();
   }

   public void sell() {
      if (this.inventory != null) {
         com.oheers.fish.api.economy.selling.SellHelper.get().sell(this.inventory, this.player);
      } else {
         if (this.array == null) {
            throw new IllegalStateException("Deprecated SellHelper has nothing to sell.");
         }

         com.oheers.fish.api.economy.selling.SellHelper.get().sell(this.array, this.player);
      }
   }
}
