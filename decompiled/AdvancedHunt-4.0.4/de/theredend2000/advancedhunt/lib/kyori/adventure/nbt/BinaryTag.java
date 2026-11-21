package de.theredend2000.advancedhunt.lib.kyori.adventure.nbt;

import de.theredend2000.advancedhunt.lib.kyori.examination.Examinable;
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
