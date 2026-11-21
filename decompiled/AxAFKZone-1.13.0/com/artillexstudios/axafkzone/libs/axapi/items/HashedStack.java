package com.artillexstudios.axafkzone.libs.axapi.items;

public interface HashedStack {
   boolean matches(WrappedItemStack stack, HashGenerator generator);
}
