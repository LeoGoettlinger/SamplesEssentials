package ac.grim.grimac.shaded.kyori.adventure.nbt;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;

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
