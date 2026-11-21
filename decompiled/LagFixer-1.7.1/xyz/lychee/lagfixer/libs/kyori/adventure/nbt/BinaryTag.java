package xyz.lychee.lagfixer.libs.kyori.adventure.nbt;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import xyz.lychee.lagfixer.libs.kyori.examination.Examinable;

@ApiStatus.NonExtendable
public interface BinaryTag extends BinaryTagLike, Examinable {
   @NotNull
   BinaryTagType<? extends BinaryTag> type();

   @NotNull
   @Override
   default BinaryTag asBinaryTag() {
      return this;
   }
}
