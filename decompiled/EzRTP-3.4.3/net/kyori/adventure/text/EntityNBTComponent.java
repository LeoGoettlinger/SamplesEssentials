package net.kyori.adventure.text;

import org.jetbrains.annotations.Contract;

public sealed interface EntityNBTComponent extends NBTComponent<EntityNBTComponent>, ScopedComponent<EntityNBTComponent> permits EntityNBTComponentImpl {
   String selector();

   @Contract(
      pure = true
   )
   EntityNBTComponent selector(final String selector);

   EntityNBTComponent.Builder toBuilder();

   public sealed interface Builder extends NBTComponentBuilder<EntityNBTComponent, EntityNBTComponent.Builder> permits EntityNBTComponentImpl.BuilderImpl {
      @Contract("_ -> this")
      EntityNBTComponent.Builder selector(final String selector);
   }
}
