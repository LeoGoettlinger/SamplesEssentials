package com.fastasyncworldedit.core.nbt;

import java.util.function.Supplier;
import org.enginehub.linbus.tree.LinCompoundTag;

record LazyFaweCompoundTag(Supplier<? extends LinCompoundTag> linTagSupplier) implements FaweCompoundTag {
   @Override
   public LinCompoundTag linTag() {
      return this.linTagSupplier().get();
   }
}
