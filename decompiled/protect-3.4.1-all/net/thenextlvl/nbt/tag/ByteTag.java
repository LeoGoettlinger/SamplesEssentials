package net.thenextlvl.nbt.tag;

import org.jetbrains.annotations.Contract;

public sealed interface ByteTag extends NumberTag<Byte> permits ByteTagImpl {
   byte ID = 1;

   @Contract(
      value = "_ -> new",
      pure = true
   )
   static ByteTag of(byte value) {
      return new ByteTagImpl(value);
   }

   @Contract(
      value = "_ -> new",
      pure = true
   )
   static ByteTag of(boolean value) {
      return of((byte)(value ? 1 : 0));
   }
}
