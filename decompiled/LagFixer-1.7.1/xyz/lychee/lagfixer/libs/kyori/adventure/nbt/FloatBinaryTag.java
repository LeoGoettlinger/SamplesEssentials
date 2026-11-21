package xyz.lychee.lagfixer.libs.kyori.adventure.nbt;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.NonExtendable
public interface FloatBinaryTag extends NumberBinaryTag {
   @NotNull
   static FloatBinaryTag floatBinaryTag(final float value) {
      return new FloatBinaryTagImpl(value);
   }

   @Deprecated
   @ApiStatus.ScheduledForRemoval(
      inVersion = "5.0.0"
   )
   @NotNull
   static FloatBinaryTag of(final float value) {
      return new FloatBinaryTagImpl(value);
   }

   @NotNull
   @Override
   default BinaryTagType<FloatBinaryTag> type() {
      return BinaryTagTypes.FLOAT;
   }

   float value();
}
