package com.artillexstudios.axtrade.libs.axapi.nms.v1_21_R3.items.datacomponents.impl;

import com.artillexstudios.axtrade.libs.axapi.items.WrappedItemStack;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;

public interface DataComponentHandler<T, Z> {
   default void apply(WrappedItemStack stack, DataComponentType<Z> type, T data) {
      ItemStack wrapped = ((com.artillexstudios.axtrade.libs.axapi.nms.v1_21_R3.items.WrappedItemStack)stack).itemStack;
      if (data == null) {
         wrapped.d(type);
      } else {
         wrapped.b(type, this.toNMS(data));
      }
   }

   default T getData(WrappedItemStack stack, DataComponentType<Z> type) {
      ItemStack wrapped = ((com.artillexstudios.axtrade.libs.axapi.nms.v1_21_R3.items.WrappedItemStack)stack).itemStack;
      Z data = (Z)wrapped.a(type);
      if (data == null) {
         data = this.getDefault();
      }

      return data == null ? null : this.fromNMS(data);
   }

   Z toNMS(T from);

   T fromNMS(Z data);

   default Z getDefault() {
      return null;
   }
}
