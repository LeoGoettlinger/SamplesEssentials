package xyz.lychee.lagfixer.libs.kyori.adventure.nbt;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.NonExtendable
public interface DoubleBinaryTag extends NumberBinaryTag {
   @NotNull
   static DoubleBinaryTag doubleBinaryTag(final double value) {
      return new DoubleBinaryTagImpl(value);
   }

   @Deprecated
   @ApiStatus.ScheduledForRemoval(
      inVersion = "5.0.0"
   )
   @NotNull
   static DoubleBinaryTag of(final double value) {
      return new DoubleBinaryTagImpl(value);
   }

   @NotNull
   @Override
   default BinaryTagType<DoubleBinaryTag> type() {
      return BinaryTagTypes.DOUBLE;
   }

   double value();
}
