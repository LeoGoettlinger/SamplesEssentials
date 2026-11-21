package net.kyori.adventure.text;

import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

public sealed interface NBTComponent<C extends NBTComponent<C>>
   extends BuildableComponent<C, NBTComponentBuilder<C, ?>>
   permits BlockNBTComponent,
   EntityNBTComponent,
   StorageNBTComponent {
   boolean INTERPRET_DEFAULT = false;
   boolean PLAIN_DEFAULT = false;

   String nbtPath();

   @Contract(
      pure = true
   )
   C nbtPath(final String nbtPath);

   boolean interpret();

   @Contract(
      pure = true
   )
   C interpret(final boolean interpret);

   @Nullable
   Component separator();

   C separator(@Nullable final ComponentLike separator);

   boolean plain();

   C plain(final boolean plain);

   NBTComponentBuilder<C, ? extends NBTComponentBuilder<C, ?>> toBuilder();
}
