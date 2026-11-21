package com.artillexstudios.axafkzone.libs.axapi.config.adapters;

import com.artillexstudios.axafkzone.libs.axapi.items.WrappedItemStack;
import java.lang.reflect.Type;
import java.util.Map;
import org.bukkit.inventory.ItemStack;

public final class ItemStackAdapter implements TypeAdapter<ItemStack, Map<String, Object>> {
   private final WrappedItemStackAdapter wrappedItemStackAdapter = new WrappedItemStackAdapter();

   public ItemStack deserialize(TypeAdapterHolder holder, Object input, Type type) {
      return this.wrappedItemStackAdapter.deserialize(holder, input, type).toBukkit();
   }

   public Map<String, Object> serialize(TypeAdapterHolder holder, ItemStack value, Type type) {
      return this.wrappedItemStackAdapter.serialize(holder, WrappedItemStack.wrap(value), type);
   }
}
