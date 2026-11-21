package net.kyori.adventure.nbt;

import net.kyori.examination.Examinable;
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
