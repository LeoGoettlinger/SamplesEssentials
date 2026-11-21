package net.thenextlvl.nbt.tag;

import org.jetbrains.annotations.Contract;

public interface ValueTag<T> extends Tag {
   @Contract(
      pure = true
   )
   T getValue();
}
