package net.thenextlvl.nbt.tag;

import org.jetbrains.annotations.Contract;

public sealed interface IntArrayTag extends ValueTag<int[]>, IterableTag<Integer> permits IntArrayTagImpl {
   byte ID = 11;

   @Contract(
      value = "_ -> new",
      pure = true
   )
   static IntArrayTag of(int... value) {
      return new IntArrayTagImpl(value);
   }
}
