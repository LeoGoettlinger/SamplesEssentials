package net.thenextlvl.nbt.tag;

import org.jetbrains.annotations.Contract;

public sealed interface IntTag extends NumberTag<Integer> permits IntTagImpl {
   byte ID = 3;

   @Contract(
      value = "_ -> new",
      pure = true
   )
   static IntTag of(int value) {
      return new IntTagImpl(value);
   }
}
