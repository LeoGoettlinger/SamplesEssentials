package com.oheers.fish.items.nbt;

import com.oheers.fish.items.nbt.abstracted.NBTHolder;
import java.util.Locale;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class LegacyItemStackNBTHolder extends NBTHolder<ItemStack> {
   private final ItemMeta meta;

   public LegacyItemStackNBTHolder(@NonNull ItemStack obj) {
      super(obj);
      this.meta = obj.getItemMeta();
   }

   @Override
   public boolean hasNamespace(@NonNull String namespace) {
      return this.getData().getKeys().stream().anyMatch(key -> key.getNamespace().equals(namespace.toLowerCase(Locale.ROOT)));
   }

   @Override
   public boolean hasKey(@NonNull NamespacedKey namespacedKey) {
      return this.getData().has(namespacedKey);
   }

   @Nullable
   @Override
   public String getString(@NonNull NamespacedKey namespacedKey) {
      return (String)this.getData().get(namespacedKey, PersistentDataType.STRING);
   }

   @Override
   public void setString(@NonNull NamespacedKey namespacedKey, @Nullable String value) {
      throw new UnsupportedOperationException("Cannot use set methods with legacy NBT.");
   }

   @Nullable
   @Override
   public Float getFloat(@NonNull NamespacedKey namespacedKey) {
      return (Float)this.getData().get(namespacedKey, PersistentDataType.FLOAT);
   }

   @Override
   public void setFloat(@NonNull NamespacedKey namespacedKey, @Nullable Float value) {
      throw new UnsupportedOperationException("Cannot use set methods with legacy NBT.");
   }

   @Nullable
   @Override
   public Integer getInteger(@NonNull NamespacedKey namespacedKey) {
      return (Integer)this.getData().get(namespacedKey, PersistentDataType.INTEGER);
   }

   @Override
   public void setInteger(@NonNull NamespacedKey namespacedKey, @Nullable Integer value) {
      throw new UnsupportedOperationException("Cannot use set methods with legacy NBT.");
   }

   @Nullable
   @Override
   public Boolean getBoolean(@NonNull NamespacedKey namespacedKey) {
      return (Boolean)this.getData().get(namespacedKey, PersistentDataType.BOOLEAN);
   }

   @Override
   public void setBoolean(@NonNull NamespacedKey namespacedKey, @Nullable Boolean value) {
      throw new UnsupportedOperationException("Cannot use set methods with legacy NBT.");
   }

   @Override
   public void save() {
      this.obj.setItemMeta(this.meta);
   }

   @NonNull
   private PersistentDataContainer getData() {
      return this.meta.getPersistentDataContainer();
   }
}
