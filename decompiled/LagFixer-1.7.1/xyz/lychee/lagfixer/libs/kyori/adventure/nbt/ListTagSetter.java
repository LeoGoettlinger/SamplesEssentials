package xyz.lychee.lagfixer.libs.kyori.adventure.nbt;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.NonExtendable
public interface ListTagSetter<R, T extends BinaryTag> {
   @NotNull
   R add(final T tag);

   @NotNull
   R add(final Iterable<? extends T> tags);
}
