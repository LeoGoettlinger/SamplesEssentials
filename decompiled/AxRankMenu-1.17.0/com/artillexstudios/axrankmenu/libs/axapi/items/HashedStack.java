package com.artillexstudios.axrankmenu.libs.axapi.items;

public interface HashedStack {
   boolean matches(WrappedItemStack stack, HashGenerator generator);
}
