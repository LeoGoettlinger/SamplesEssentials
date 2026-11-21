package de.theredend2000.advancedhunt.lib.kyori.adventure.text;

import de.theredend2000.advancedhunt.lib.kyori.adventure.text.object.ObjectContents;
import org.jspecify.annotations.Nullable;

public sealed interface ObjectComponent extends ScopedComponent<ObjectComponent> permits ObjectComponentImpl {
   ObjectContents contents();

   ObjectComponent contents(final ObjectContents contents);

   @Nullable
   Component fallback();

   ObjectComponent fallback(@Nullable final ComponentLike fallback);

   ObjectComponent.Builder toBuilder();

   public sealed interface Builder extends ComponentBuilder<ObjectComponent, ObjectComponent.Builder> permits ObjectComponentImpl.BuilderImpl {
      ObjectComponent.Builder contents(final ObjectContents objectContents);

      ObjectComponent.Builder fallback(@Nullable final ComponentLike fallback);
   }
}
