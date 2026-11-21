package com.oheers.fish.api.items;

import java.util.Map;
import java.util.UUID;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public abstract class AbstractItemFactory {
   public abstract void setRandomIndex(int var1);

   public abstract int getRandomIndex();

   @NonNull
   public abstract AbstractItemFactory createCopy();

   @NonNull
   public final ItemStack createItem() {
      return this.createItem((Map<String, ?>)null);
   }

   @NonNull
   public abstract ItemStack createItem(@Nullable Map<String, ?> var1);

   @NonNull
   public final ItemStack createItem(@NonNull UUID relevantPlayer) {
      return this.createItem(relevantPlayer, null);
   }

   @NonNull
   public abstract ItemStack createItem(@NonNull UUID var1, @Nullable Map<String, ?> var2);
}
