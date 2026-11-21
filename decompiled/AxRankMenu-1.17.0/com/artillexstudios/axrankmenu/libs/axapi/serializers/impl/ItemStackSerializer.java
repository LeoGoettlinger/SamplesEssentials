package com.artillexstudios.axrankmenu.libs.axapi.serializers.impl;

import com.artillexstudios.axrankmenu.libs.axapi.items.WrappedItemStack;
import com.artillexstudios.axrankmenu.libs.axapi.libs.snakeyaml.external.biz.base64Coder.Base64Coder;
import com.artillexstudios.axrankmenu.libs.axapi.serializers.Serializer;
import org.bukkit.inventory.ItemStack;

public class ItemStackSerializer implements Serializer<ItemStack, String> {
   public String serialize(ItemStack object) {
      return Base64Coder.encodeLines(WrappedItemStack.wrap(object).serialize());
   }

   public ItemStack deserialize(String value) {
      return WrappedItemStack.wrap(Base64Coder.decodeLines(value)).toBukkit();
   }
}
