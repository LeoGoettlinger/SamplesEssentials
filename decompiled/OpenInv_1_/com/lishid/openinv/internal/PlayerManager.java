package com.lishid.openinv.internal;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PlayerManager {
   @Nullable
   Player loadPlayer(@NotNull OfflinePlayer var1);

   @NotNull
   Player inject(@NotNull Player var1);

   @Nullable
   InventoryView openInventory(@NotNull Player var1, @NotNull ISpecialInventory var2, boolean var3);
}
