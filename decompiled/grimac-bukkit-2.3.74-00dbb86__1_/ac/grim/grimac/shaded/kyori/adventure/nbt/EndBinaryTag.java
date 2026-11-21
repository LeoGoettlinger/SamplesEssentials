package ac.grim.grimac.shaded.kyori.adventure.nbt;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;

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
