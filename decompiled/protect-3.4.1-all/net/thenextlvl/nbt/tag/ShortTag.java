package net.thenextlvl.nbt.tag;

import org.jetbrains.annotations.Contract;

public sealed interface ShortTag extends NumberTag<Short> permits ShortTagImpl {
   byte ID = 2;

   @Contract(
      value = "_ -> new",
      pure = true
   )
   static ShortTag of(short value) {
      return new ShortTagImpl(value);
   }
}
