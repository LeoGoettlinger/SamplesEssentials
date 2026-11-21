package com.lishid.openinv.internal;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Accessor {
   @NotNull
   PlayerManager getPlayerManager();

   @NotNull
   IAnySilentContainer getAnySilentContainer();

   @NotNull
   ISpecialPlayerInventory createPlayerInventory(@NotNull Player var1);

   @NotNull
   ISpecialEnderChest createEnderChest(@NotNull Player var1);

   @Nullable
   <T extends ISpecialInventory> T get(@NotNull Inventory var1, @NotNull Class<T> var2);

   void reload(@NotNull ConfigurationSection var1);
}
