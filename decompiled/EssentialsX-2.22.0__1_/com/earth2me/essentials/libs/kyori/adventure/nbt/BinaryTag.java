package com.earth2me.essentials.libs.kyori.adventure.nbt;

import com.earth2me.essentials.libs.kyori.examination.Examinable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.ApiStatus.NonExtendable;

@NonExtendable
public interface BinaryTag extends BinaryTagLike, Examinable {
   @NotNull
   BinaryTagType<? extends BinaryTag> type();

   @NotNull
   @Override
   default BinaryTag asBinaryTag() {
      return this;
   }
}
