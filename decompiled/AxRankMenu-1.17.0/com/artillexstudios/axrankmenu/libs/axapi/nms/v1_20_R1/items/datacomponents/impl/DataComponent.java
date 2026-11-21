package com.artillexstudios.axrankmenu.libs.axapi.nms.v1_20_R1.items.datacomponents.impl;

import com.artillexstudios.axrankmenu.libs.axapi.items.WrappedItemStack;

public final class DataComponent {
   public static <T> com.artillexstudios.axrankmenu.libs.axapi.items.components.DataComponent<T> create(String id, DataComponentHandler<T> handler) {
      return new com.artillexstudios.axrankmenu.libs.axapi.items.components.DataComponent<T>() {
         @Override
         public void apply(WrappedItemStack stack, T data) {
            handler.apply(stack, data);
         }

         @Override
         public T getData(WrappedItemStack stack) {
            return handler.getData(stack);
         }

         @Override
         public String getId() {
            return id;
         }
      };
   }
}
