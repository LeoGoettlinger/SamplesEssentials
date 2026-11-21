package com.artillexstudios.axrankmenu.libs.axapi.nms.v1_21_R5.items.datacomponents.impl;

public final class IdentityDataComponent<T> implements DataComponentHandler<T, T> {
   @Override
   public T toNMS(T from) {
      return from;
   }

   @Override
   public T fromNMS(T data) {
      return data;
   }
}
