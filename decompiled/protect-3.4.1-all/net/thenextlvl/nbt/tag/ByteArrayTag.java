package net.thenextlvl.nbt.tag;

import org.jetbrains.annotations.Contract;

public sealed interface ByteArrayTag extends ValueTag<byte[]>, IterableTag<Byte> permits ByteArrayTagImpl {
   byte ID = 7;

   @Contract(
      value = "_ -> new",
      pure = true
   )
   static ByteArrayTag of(byte... value) {
      return new ByteArrayTagImpl(value);
   }
}
