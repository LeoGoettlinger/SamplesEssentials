package ac.grim.grimac.shaded.kyori.adventure.nbt;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;

@ApiStatus.NonExtendable
public interface ArrayBinaryTag extends BinaryTag {
   @NotNull
   @Override
   BinaryTagType<? extends ArrayBinaryTag> type();
}
