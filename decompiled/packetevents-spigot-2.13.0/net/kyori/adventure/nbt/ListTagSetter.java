package net.kyori.adventure.nbt;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.ApiStatus.NonExtendable;

@NonExtendable
public interface ListTagSetter<R, T extends BinaryTag> {
   @NotNull
   R add(final T tag);

   @NotNull
   R add(final Iterable<? extends T> tags);
}
