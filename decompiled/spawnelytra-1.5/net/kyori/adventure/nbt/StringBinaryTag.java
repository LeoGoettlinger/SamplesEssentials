package net.kyori.adventure.nbt;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval;

public interface StringBinaryTag extends BinaryTag {
   @NotNull
   static StringBinaryTag stringBinaryTag(@NotNull final String value) {
      return new StringBinaryTagImpl(value);
   }

   @Deprecated
   @ScheduledForRemoval(
      inVersion = "5.0.0"
   )
   @NotNull
   static StringBinaryTag of(@NotNull final String value) {
      return new StringBinaryTagImpl(value);
   }

   @NotNull
   @Override
   default BinaryTagType<StringBinaryTag> type() {
      return BinaryTagTypes.STRING;
   }

   @NotNull
   String value();
}
