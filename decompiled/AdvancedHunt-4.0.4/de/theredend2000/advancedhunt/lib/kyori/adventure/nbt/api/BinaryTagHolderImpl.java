package de.theredend2000.advancedhunt.lib.kyori.adventure.nbt.api;

import de.theredend2000.advancedhunt.lib.kyori.adventure.util.Codec;
import java.util.Objects;

record BinaryTagHolderImpl(String string) implements BinaryTagHolder {
   BinaryTagHolderImpl(final String string) {
      this.string = Objects.requireNonNull(string, "string");
   }

   @Override
   public <T, DX extends Exception> T get(final Codec<T, String, DX, ?> codec) throws DX {
      return codec.decode(this.string);
   }

   @Override
   public String toString() {
      return this.string;
   }
}
