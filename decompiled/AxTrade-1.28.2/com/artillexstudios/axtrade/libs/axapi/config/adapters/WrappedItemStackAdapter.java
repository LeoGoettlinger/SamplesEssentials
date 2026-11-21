package com.artillexstudios.axtrade.libs.axapi.config.adapters;

import com.artillexstudios.axtrade.libs.axapi.items.WrappedItemStack;
import com.artillexstudios.axtrade.libs.axapi.utils.ItemBuilder;
import com.artillexstudios.axtrade.libs.axapi.utils.UncheckedUtils;
import java.lang.reflect.Type;
import java.util.Map;

public final class WrappedItemStackAdapter implements TypeAdapter<WrappedItemStack, Map<String, Object>> {
   public WrappedItemStack deserialize(TypeAdapterHolder holder, Object input, Type type) {
      if (input instanceof Map<?, ?> map) {
         Map<Object, Object> castedMap = UncheckedUtils.unsafeCast(map);
         return ItemBuilder.create(castedMap).wrapped();
      } else {
         throw new IllegalArgumentException();
      }
   }

   public Map<String, Object> serialize(TypeAdapterHolder holder, WrappedItemStack value, Type type) {
      return UncheckedUtils.unsafeCast(ItemBuilder.create(value).serialize(true));
   }
}
