package ac.grim.grimac.shaded.kyori.adventure.nbt;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;

@ApiStatus.NonExtendable
public interface ListTagSetter<R, T extends BinaryTag> {
   @NotNull
   R add(final T tag);

   @NotNull
   R add(final Iterable<? extends T> tags);
}
