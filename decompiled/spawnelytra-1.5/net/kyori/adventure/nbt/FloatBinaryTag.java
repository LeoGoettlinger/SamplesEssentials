package net.kyori.adventure.nbt;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval;

public interface FloatBinaryTag extends NumberBinaryTag {
   @NotNull
   static FloatBinaryTag floatBinaryTag(final float value) {
      return new FloatBinaryTagImpl(value);
   }

   @Deprecated
   @ScheduledForRemoval(
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
