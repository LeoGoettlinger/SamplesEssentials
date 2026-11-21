package xyz.lychee.lagfixer.libs.kyori.adventure.nbt;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.NonExtendable
public interface IntBinaryTag extends NumberBinaryTag {
   @NotNull
   static IntBinaryTag intBinaryTag(final int value) {
      return new IntBinaryTagImpl(value);
   }

   @Deprecated
   @ApiStatus.ScheduledForRemoval(
      inVersion = "5.0.0"
   )
   @NotNull
   static IntBinaryTag of(final int value) {
      return new IntBinaryTagImpl(value);
   }

   @NotNull
   @Override
   default BinaryTagType<IntBinaryTag> type() {
      return BinaryTagTypes.INT;
   }

   int value();
}
