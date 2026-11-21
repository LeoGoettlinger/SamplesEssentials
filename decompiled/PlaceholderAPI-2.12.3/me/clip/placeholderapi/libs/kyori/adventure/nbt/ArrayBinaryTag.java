package me.clip.placeholderapi.libs.kyori.adventure.nbt;

import org.jetbrains.annotations.NotNull;

public interface ArrayBinaryTag extends BinaryTag {
   @NotNull
   @Override
   BinaryTagType<? extends ArrayBinaryTag> type();
}
