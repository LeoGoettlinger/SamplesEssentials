package net.kyori.adventure.text;

import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

public sealed interface NBTComponentBuilder<C extends NBTComponent<C>, B extends NBTComponentBuilder<C, B>>
   extends ComponentBuilder<C, B>
   permits AbstractNBTComponentBuilder,
   BlockNBTComponent.Builder,
   EntityNBTComponent.Builder,
   StorageNBTComponent.Builder {
   @Contract("_ -> this")
   B nbtPath(final String nbtPath);

   @Contract("_ -> this")
   B interpret(final boolean interpret);

   @Contract("_ -> this")
   B separator(@Nullable final ComponentLike separator);

   B plain(final boolean plain);
}
