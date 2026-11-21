package de.theredend2000.advancedhunt.lib.kyori.adventure.text;

import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

public sealed interface SelectorComponent extends ScopedComponent<SelectorComponent> permits SelectorComponentImpl {
   String pattern();

   @Contract(
      pure = true
   )
   SelectorComponent pattern(final String pattern);

   @Nullable
   Component separator();

   SelectorComponent separator(@Nullable final ComponentLike separator);

   SelectorComponent.Builder toBuilder();

   public sealed interface Builder extends ComponentBuilder<SelectorComponent, SelectorComponent.Builder> permits SelectorComponentImpl.BuilderImpl {
      @Contract("_ -> this")
      SelectorComponent.Builder pattern(final String pattern);

      @Contract("_ -> this")
      SelectorComponent.Builder separator(@Nullable final ComponentLike separator);
   }
}
