package net.kyori.adventure.nbt;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval;

public interface ShortBinaryTag extends NumberBinaryTag {
   @NotNull
   static ShortBinaryTag shortBinaryTag(final short value) {
      return new ShortBinaryTagImpl(value);
   }

   @Deprecated
   @ScheduledForRemoval(
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
