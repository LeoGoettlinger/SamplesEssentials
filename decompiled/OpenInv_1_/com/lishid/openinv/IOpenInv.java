package com.lishid.openinv;

import com.lishid.openinv.internal.IAnySilentContainer;
import com.lishid.openinv.internal.ISpecialEnderChest;
import com.lishid.openinv.internal.ISpecialInventory;
import com.lishid.openinv.internal.ISpecialPlayerInventory;
import java.util.UUID;
import java.util.logging.Logger;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IOpenInv {
   boolean isSupportedVersion();

   boolean disableSaving();

   boolean disableOfflineAccess();

   boolean noArgsOpensSelf();

   @NotNull
   IAnySilentContainer getAnySilentContainer();

   boolean getAnyContainerStatus(@NotNull OfflinePlayer var1);

   void setAnyContainerStatus(@NotNull OfflinePlayer var1, boolean var2);

   boolean getSilentContainerStatus(@NotNull OfflinePlayer var1);

   void setSilentContainerStatus(@NotNull OfflinePlayer var1, boolean var2);

   @NotNull
   ISpecialEnderChest getSpecialEnderChest(@NotNull Player var1, boolean var2) throws InstantiationException;

   @NotNull
   ISpecialPlayerInventory getSpecialInventory(@NotNull Player var1, boolean var2) throws InstantiationException;

   @Deprecated(
      forRemoval = true,
      since = "5.2.0"
   )
   @Nullable
   InventoryView openInventory(@NotNull Player var1, @NotNull ISpecialInventory var2);

   @Nullable
   InventoryView openInventory(@NotNull Player var1, @NotNull ISpecialInventory var2, boolean var3);

   boolean isPlayerLoaded(@NotNull UUID var1);

   @Nullable
   Player loadPlayer(@NotNull OfflinePlayer var1);

   @Nullable
   OfflinePlayer matchPlayer(@NotNull String var1);

   void unload(@NotNull OfflinePlayer var1);

   Logger getLogger();
}
