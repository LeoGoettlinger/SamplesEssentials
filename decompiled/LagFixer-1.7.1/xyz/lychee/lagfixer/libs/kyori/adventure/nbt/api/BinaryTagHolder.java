package xyz.lychee.lagfixer.libs.kyori.adventure.nbt.api;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.event.DataComponentValue;
import xyz.lychee.lagfixer.libs.kyori.adventure.util.Codec;

public interface BinaryTagHolder extends DataComponentValue.TagSerializable {
   @NotNull
   static <T, EX extends Exception> BinaryTagHolder encode(@NotNull final T nbt, @NotNull final Codec<? super T, String, ?, EX> codec) throws EX {
      return new BinaryTagHolderImpl(codec.encode(nbt));
   }

   @NotNull
   static BinaryTagHolder binaryTagHolder(@NotNull final String string) {
      return new BinaryTagHolderImpl(string);
   }

   @Deprecated
   @ApiStatus.ScheduledForRemoval(
      inVersion = "5.0.0"
   )
   @NotNull
   static BinaryTagHolder of(@NotNull final String string) {
      return new BinaryTagHolderImpl(string);
   }

   @NotNull
   String string();

   @NotNull
   @Override
   default BinaryTagHolder asBinaryTag() {
      return this;
   }

   @NotNull
   <T, DX extends Exception> T get(@NotNull final Codec<T, String, DX, ?> codec) throws DX;
}
