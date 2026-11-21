package com.artillexstudios.axtrade.libs.axapi.items.components;

import com.artillexstudios.axtrade.libs.axapi.items.WrappedItemStack;

public interface DataComponent<T> {
   void apply(WrappedItemStack stack, T data);

   T getData(WrappedItemStack stack);

   String getId();
}
