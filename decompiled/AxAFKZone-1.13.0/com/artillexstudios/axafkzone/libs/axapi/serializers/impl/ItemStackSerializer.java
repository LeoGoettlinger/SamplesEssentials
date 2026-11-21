package com.artillexstudios.axafkzone.libs.axapi.serializers.impl;

import com.artillexstudios.axafkzone.libs.axapi.items.WrappedItemStack;
import com.artillexstudios.axafkzone.libs.axapi.libs.snakeyaml.external.biz.base64Coder.Base64Coder;
import com.artillexstudios.axafkzone.libs.axapi.serializers.Serializer;
import org.bukkit.inventory.ItemStack;

public class ItemStackSerializer implements Serializer<ItemStack, String> {
   public String serialize(ItemStack object) {
      return Base64Coder.encodeLines(WrappedItemStack.wrap(object).serialize());
   }

   public ItemStack deserialize(String value) {
      return WrappedItemStack.wrap(Base64Coder.decodeLines(value)).toBukkit();
   }
}
