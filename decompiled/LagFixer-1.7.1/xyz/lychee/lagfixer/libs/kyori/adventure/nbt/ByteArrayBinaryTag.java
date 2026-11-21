package xyz.lychee.lagfixer.libs.kyori.adventure.nbt;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.NonExtendable
public interface ByteArrayBinaryTag extends ArrayBinaryTag, Iterable<Byte> {
   @NotNull
   static ByteArrayBinaryTag byteArrayBinaryTag(@NotNull final byte... value) {
      return new ByteArrayBinaryTagImpl(value);
   }

   @Deprecated
   @ApiStatus.ScheduledForRemoval(
      inVersion = "5.0.0"
   )
   @NotNull
   static ByteArrayBinaryTag of(@NotNull final byte... value) {
      return new ByteArrayBinaryTagImpl(value);
   }

   @NotNull
   @Override
   default BinaryTagType<ByteArrayBinaryTag> type() {
      return BinaryTagTypes.BYTE_ARRAY;
   }

   @NotNull
   byte[] value();

   int size();

   byte get(final int index);
}
