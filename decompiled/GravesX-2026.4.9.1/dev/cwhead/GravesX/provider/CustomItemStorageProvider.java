package dev.cwhead.GravesX.provider;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface CustomItemStorageProvider {
   @NotNull
   String getProviderId();

   @Nullable
   String getCustomItemId(@Nullable ItemStack var1);

   @Nullable
   ItemStack rebuildCustomItem(@NotNull String var1, int var2);
}
