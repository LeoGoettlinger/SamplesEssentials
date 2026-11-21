package net.kyori.adventure.nbt;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.ApiStatus.NonExtendable;

@NonExtendable
public interface ArrayBinaryTag extends BinaryTag {
   @NotNull
   @Override
   BinaryTagType<? extends ArrayBinaryTag> type();
}
