package xyz.lychee.lagfixer.libs.kyori.adventure.nbt;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.NonExtendable
public interface StringBinaryTag extends BinaryTag {
   @NotNull
   static StringBinaryTag stringBinaryTag(@NotNull final String value) {
      return new StringBinaryTagImpl(value);
   }

   @Deprecated
   @ApiStatus.ScheduledForRemoval(
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
