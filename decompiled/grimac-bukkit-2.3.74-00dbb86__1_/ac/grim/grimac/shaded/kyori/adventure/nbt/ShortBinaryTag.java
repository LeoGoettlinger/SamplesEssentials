package ac.grim.grimac.shaded.kyori.adventure.nbt;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;

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
