package com.oheers.fish.items.nbt.abstracted;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.items.nbt.LegacyItemStackNBTHolder;
import com.oheers.fish.items.nbt.SkullNBTHolder;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Skull;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@ApiStatus.Internal
public abstract class NBTHolder<T> {
   protected final T obj;
   protected boolean autoSave = true;

   @NonNull
   public static NBTHolder<ItemStack> itemStack(@NonNull ItemStack item) {
      if (item.isEmpty()) {
         throw new IllegalStateException("Cannot fetch NBTHolder for an empty item.");
      } else {
         NBTHolder<ItemStack> legacy = new LegacyItemStackNBTHolder(item);
         return legacy.hasNamespace("evenmorefish") ? legacy : EvenMoreFish.getInstance().getVersionProvider().createItemStackNbtHolder(item);
      }
   }

   @Nullable
   public static NBTHolder<ItemStack> itemStack(@NonNull String rawNbt) {
      if (rawNbt.isEmpty()) {
         return null;
      } else {
         ItemStack item = EvenMoreFish.getInstance().getVersionProvider().deserializeItemStack(rawNbt);
         return item != null && !item.isEmpty() ? itemStack(item) : null;
      }
   }

   @NonNull
   public static NBTHolder<Skull> skull(@NonNull Skull skull) {
      return new SkullNBTHolder(skull);
   }

   public NBTHolder(@NonNull T obj) {
      this.obj = obj;
   }

   public abstract boolean hasNamespace(@NonNull String namespace);

   public abstract boolean hasKey(@NonNull NamespacedKey namespacedKey);

   @Nullable
   public abstract String getString(@NonNull NamespacedKey namespacedKey);

   public abstract void setString(@NonNull NamespacedKey namespacedKey, @Nullable String value);

   @Nullable
   public abstract Float getFloat(@NonNull NamespacedKey namespacedKey);

   public abstract void setFloat(@NonNull NamespacedKey namespacedKey, @Nullable Float value);

   @Nullable
   public abstract Integer getInteger(@NonNull NamespacedKey namespacedKey);

   public abstract void setInteger(@NonNull NamespacedKey namespacedKey, @Nullable Integer value);

   @Nullable
   public abstract Boolean getBoolean(@NonNull NamespacedKey namespacedKey);

   public abstract void setBoolean(@NonNull NamespacedKey namespacedKey, @Nullable Boolean value);

   public abstract void save();

   public void setAutoSave(boolean autoSave) {
      this.autoSave = autoSave;
   }

   @NonNull
   public T getObject() {
      return this.obj;
   }
}
