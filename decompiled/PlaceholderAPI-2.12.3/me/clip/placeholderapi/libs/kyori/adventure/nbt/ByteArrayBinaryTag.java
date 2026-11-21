package me.clip.placeholderapi.libs.kyori.adventure.nbt;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval;

public interface ByteArrayBinaryTag extends ArrayBinaryTag, Iterable<Byte> {
   @NotNull
   static ByteArrayBinaryTag byteArrayBinaryTag(@NotNull final byte... value) {
      return new ByteArrayBinaryTagImpl(value);
   }

   @Deprecated
   @ScheduledForRemoval(
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
