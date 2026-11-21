package me.clip.placeholderapi.libs.kyori.adventure.nbt;

import me.clip.placeholderapi.libs.kyori.examination.Examinable;
import org.jetbrains.annotations.NotNull;

public interface BinaryTag extends BinaryTagLike, Examinable {
   @NotNull
   BinaryTagType<? extends BinaryTag> type();

   @NotNull
   @Override
   default BinaryTag asBinaryTag() {
      return this;
   }
}
