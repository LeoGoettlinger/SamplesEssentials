package xyz.lychee.lagfixer.libs.kyori.adventure.nbt;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.NonExtendable
public interface ShortBinaryTag extends NumberBinaryTag {
   @NotNull
   static ShortBinaryTag shortBinaryTag(final short value) {
      return new ShortBinaryTagImpl(value);
   }

   @Deprecated
   @ApiStatus.ScheduledForRemoval(
      inVersion = "5.0.0"
   )
   @NotNull
   static ShortBinaryTag of(final short value) {
      return new ShortBinaryTagImpl(value);
   }

   @NotNull
   @Override
   default BinaryTagType<ShortBinaryTag> type() {
      return BinaryTagTypes.SHORT;
   }

   short value();
}
