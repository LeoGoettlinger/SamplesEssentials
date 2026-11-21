package com.artillexstudios.axrankmenu.libs.axapi.nms.v1_21_R6.items.datacomponents.impl;

import com.artillexstudios.axrankmenu.libs.axapi.items.WrappedItemStack;
import net.minecraft.core.component.DataComponentType;

public final class DataComponent {
   public static <T, Z> com.artillexstudios.axrankmenu.libs.axapi.items.components.DataComponent<T> create(
      String id, DataComponentType<Z> type, DataComponentHandler<T, Z> handler
   ) {
      return new com.artillexstudios.axrankmenu.libs.axapi.items.components.DataComponent<T>() {
         @Override
         public void apply(WrappedItemStack stack, T data) {
            handler.apply(stack, type, data);
         }

         @Override
         public T getData(WrappedItemStack stack) {
            return handler.getData(stack, type);
         }

         @Override
         public String getId() {
            return id;
         }
      };
   }
}
