package net.thenextlvl.nbt.tag;

public sealed interface StringTag extends ValueTag<String> permits StringTagImpl {
   byte ID = 8;

   static StringTag of(String value) {
      return new StringTagImpl(value);
   }
}
