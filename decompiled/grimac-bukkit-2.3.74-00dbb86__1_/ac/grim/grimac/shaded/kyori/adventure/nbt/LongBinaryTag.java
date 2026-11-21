package ac.grim.grimac.shaded.kyori.adventure.nbt;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;

@ApiStatus.NonExtendable
public interface LongBinaryTag extends NumberBinaryTag {
   @NotNull
   static LongBinaryTag longBinaryTag(final long value) {
      return new LongBinaryTagImpl(value);
   }

   @Deprecated
   @ApiStatus.ScheduledForRemoval(
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
