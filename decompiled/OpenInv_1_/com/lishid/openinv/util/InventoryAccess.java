package com.lishid.openinv.util;

import com.google.errorprone.annotations.RestrictedApi;
import com.lishid.openinv.internal.ISpecialEnderChest;
import com.lishid.openinv.internal.ISpecialInventory;
import com.lishid.openinv.internal.ISpecialPlayerInventory;
import java.util.function.BiFunction;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

public final class InventoryAccess {
   @Nullable
   private static BiFunction<Inventory, Class<? extends ISpecialInventory>, ISpecialInventory> provider;

   public static boolean isUsable() {
      return provider != null;
   }

   public static boolean isPlayerInventory(@NotNull Inventory inventory) {
      return getPlayerInventory(inventory) != null;
   }

   @Nullable
   public static ISpecialPlayerInventory getPlayerInventory(@NotNull Inventory inventory) {
      return provider == null ? null : (ISpecialPlayerInventory)provider.apply(inventory, ISpecialPlayerInventory.class);
   }

   public static boolean isEnderChest(@NotNull Inventory inventory) {
      return getEnderChest(inventory) != null;
   }

   @Nullable
   public static ISpecialEnderChest getEnderChest(@NotNull Inventory inventory) {
      return provider == null ? null : (ISpecialEnderChest)provider.apply(inventory, ISpecialEnderChest.class);
   }

   @Nullable
   public static ISpecialInventory getInventory(@NotNull Inventory inventory) {
      return provider == null ? null : provider.apply(inventory, ISpecialInventory.class);
   }

   @RestrictedApi(
      explanation = "Not part of the API.",
      allowedOnPath = ".*/com/lishid/openinv/util/InternalAccessor.java"
   )
   @Internal
   static void setProvider(@Nullable BiFunction<Inventory, Class<? extends ISpecialInventory>, ISpecialInventory> provider) {
      InventoryAccess.provider = provider;
   }

   private InventoryAccess() {
      throw new IllegalStateException("Cannot create instance of utility class.");
   }
}
