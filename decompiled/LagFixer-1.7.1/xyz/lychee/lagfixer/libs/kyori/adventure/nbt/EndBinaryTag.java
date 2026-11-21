package xyz.lychee.lagfixer.libs.kyori.adventure.nbt;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.NonExtendable
public interface EndBinaryTag extends BinaryTag {
   @NotNull
   static EndBinaryTag endBinaryTag() {
      return EndBinaryTagImpl.INSTANCE;
   }

   @Deprecated
   @ApiStatus.ScheduledForRemoval(
      inVersion = "5.0.0"
   )
   @NotNull
   static EndBinaryTag get() {
      return EndBinaryTagImpl.INSTANCE;
   }

   @NotNull
   @Override
   default BinaryTagType<EndBinaryTag> type() {
      return BinaryTagTypes.END;
   }
}
