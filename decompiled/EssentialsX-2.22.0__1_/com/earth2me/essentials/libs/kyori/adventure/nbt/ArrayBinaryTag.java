package com.earth2me.essentials.libs.kyori.adventure.nbt;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.ApiStatus.NonExtendable;

@NonExtendable
public interface ArrayBinaryTag extends BinaryTag {
   @NotNull
   @Override
   BinaryTagType<? extends ArrayBinaryTag> type();
}
