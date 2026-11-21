package com.artillexstudios.axtrade.libs.axapi.items;

public interface HashedStack {
   boolean matches(WrappedItemStack stack, HashGenerator generator);
}
