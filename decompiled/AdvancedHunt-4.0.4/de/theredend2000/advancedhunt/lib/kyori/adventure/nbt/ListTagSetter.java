package de.theredend2000.advancedhunt.lib.kyori.adventure.nbt;

import org.jetbrains.annotations.NotNull;

public interface ListTagSetter<R, T extends BinaryTag> {
   @NotNull
   R add(final T tag);

   @NotNull
   R add(final Iterable<? extends T> tags);
}
