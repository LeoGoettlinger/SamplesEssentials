package xyz.lychee.lagfixer.libs.kyori.adventure.nbt;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.NonExtendable
public interface ArrayBinaryTag extends BinaryTag {
   @NotNull
   @Override
   BinaryTagType<? extends ArrayBinaryTag> type();
}
