package net.thenextlvl.nbt.tag;

import org.jetbrains.annotations.Contract;

public sealed interface LongArrayTag extends ValueTag<long[]>, IterableTag<Long> permits LongArrayTagImpl {
   byte ID = 12;

   @Contract(
      value = "_ -> new",
      pure = true
   )
   static LongArrayTag of(long... value) {
      return new LongArrayTagImpl(value);
   }
}
