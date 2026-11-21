package me.clip.placeholderapi.libs.kyori.adventure.nbt;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval;

public interface IntBinaryTag extends NumberBinaryTag {
   @NotNull
   static IntBinaryTag intBinaryTag(final int value) {
      return new IntBinaryTagImpl(value);
   }

   @Deprecated
   @ScheduledForRemoval(
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
