package com.oheers.fish.plugin.loading;

import com.oheers.fish.api.plugin.EMFPlugin;
import com.oheers.fish.items.nbt.abstracted.NBTHolder;
import java.util.UUID;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public abstract class EMFVersionProvider {
   protected final EMFPlugin plugin;

   public EMFVersionProvider(@NonNull EMFPlugin plugin) {
      this.plugin = plugin;
   }

   public abstract void load();

   public abstract void enable();

   public abstract void reload();

   @NonNull
   public abstract NBTHolder<ItemStack> createItemStackNbtHolder(@NonNull ItemStack item);

   @Nullable
   public abstract ItemStack deserializeItemStack(@NonNull String raw);

   @NonNull
   public abstract String serializeItemStack(@NonNull ItemStack item);

   @ApiStatus.Internal
   @NonNull
   public abstract ItemStack getSkullFromUUID(@NonNull UUID uuid);

   @ApiStatus.Internal
   @NonNull
   public abstract ItemStack getSkullFromBase64(@NonNull String base64);
}
