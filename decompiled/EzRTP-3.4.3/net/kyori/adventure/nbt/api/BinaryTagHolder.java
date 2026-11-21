package net.kyori.adventure.nbt.api;

import net.kyori.adventure.text.event.DataComponentValue;
import net.kyori.adventure.util.Codec;

public interface BinaryTagHolder extends DataComponentValue.TagSerializable {
   static <T, EX extends Exception> BinaryTagHolder encode(final T nbt, final Codec<? super T, String, ?, EX> codec) throws EX {
      return new BinaryTagHolderImpl(codec.encode(nbt));
   }

   static BinaryTagHolder binaryTagHolder(final String string) {
      return new BinaryTagHolderImpl(string);
   }

   String string();

   @Override
   default BinaryTagHolder asBinaryTag() {
      return this;
   }

   <T, DX extends Exception> T get(final Codec<T, String, DX, ?> codec) throws DX;
}
