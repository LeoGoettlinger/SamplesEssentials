package net.thenextlvl.nbt.tag;

import org.jetbrains.annotations.Contract;

public sealed interface LongTag extends NumberTag<Long> permits LongTagImpl {
   byte ID = 4;

   @Contract(
      value = "_ -> new",
      pure = true
   )
   static LongTag of(long value) {
      return new LongTagImpl(value);
   }
}
