package com.fastasyncworldedit.core.nbt;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.sk89q.worldedit.util.concurrency.LazyReference;
import org.enginehub.linbus.tree.LinCompoundTag;

public sealed interface FaweCompoundTag permits EagerFaweCompoundTag, LazyFaweCompoundTag {
   static FaweCompoundTag of(LazyReference<? extends LinCompoundTag> lazyReference) {
      return new LazyFaweCompoundTag(lazyReference::getValue);
   }

   static FaweCompoundTag of(Supplier<? extends LinCompoundTag> supplier) {
      return new LazyFaweCompoundTag(Suppliers.memoize(supplier));
   }

   static FaweCompoundTag of(LinCompoundTag linCompoundTag) {
      return new EagerFaweCompoundTag(linCompoundTag);
   }

   LinCompoundTag linTag();
}
