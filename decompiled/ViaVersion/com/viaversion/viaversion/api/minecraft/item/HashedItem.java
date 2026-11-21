package com.viaversion.viaversion.api.minecraft.item;

import com.viaversion.viaversion.libs.fastutil.ints.Int2IntMap;
import com.viaversion.viaversion.libs.fastutil.ints.IntSet;

public interface HashedItem extends ItemBase {
   Int2IntMap dataHashesById();

   IntSet removedDataIds();

   HashedItem copy();
}
