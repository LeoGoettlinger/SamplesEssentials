package ac.grim.grimac.shaded.kyori.adventure.nbt;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;

@ApiStatus.NonExtendable
public interface ByteBinaryTag extends NumberBinaryTag {
   ByteBinaryTag ZERO = new ByteBinaryTagImpl((byte)0);
   ByteBinaryTag ONE = new ByteBinaryTagImpl((byte)1);

   @NotNull
   static ByteBinaryTag byteBinaryTag(final byte value) {
      if (value == 0) {
         return ZERO;
      } else {
         return (ByteBinaryTag)(value == 1 ? ONE : new ByteBinaryTagImpl(value));
      }
   }

   @Deprecated
   @ApiStatus.ScheduledForRemoval(
      inVersion = "5.0.0"
   )
   @NotNull
   static ByteBinaryTag of(final byte value) {
      return byteBinaryTag(value);
   }

   @NotNull
   @Override
   default BinaryTagType<ByteBinaryTag> type() {
      return BinaryTagTypes.BYTE;
   }

   byte value();
}
