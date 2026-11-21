package ac.grim.grimac.shaded.kyori.adventure.nbt;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;

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
