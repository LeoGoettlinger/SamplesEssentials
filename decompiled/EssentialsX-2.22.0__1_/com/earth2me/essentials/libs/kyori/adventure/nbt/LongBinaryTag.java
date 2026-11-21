package com.earth2me.essentials.libs.kyori.adventure.nbt;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.ApiStatus.NonExtendable;
import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval;

@NonExtendable
public interface LongBinaryTag extends NumberBinaryTag {
   @NotNull
   static LongBinaryTag longBinaryTag(final long value) {
      return new LongBinaryTagImpl(value);
   }

   @Deprecated
   @ScheduledForRemoval(
      inVersion = "5.0.0"
   )
   @NotNull
   static LongBinaryTag of(final long value) {
      return new LongBinaryTagImpl(value);
   }

   @NotNull
   @Override
   default BinaryTagType<LongBinaryTag> type() {
      return BinaryTagTypes.LONG;
   }

   long value();
}
