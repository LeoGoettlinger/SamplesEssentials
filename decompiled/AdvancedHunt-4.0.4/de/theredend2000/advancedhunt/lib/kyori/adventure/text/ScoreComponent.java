package de.theredend2000.advancedhunt.lib.kyori.adventure.text;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.ApiStatus.Obsolete;
import org.jspecify.annotations.Nullable;

public sealed interface ScoreComponent extends ScopedComponent<ScoreComponent> permits ScoreComponentImpl {
   String name();

   @Contract(
      pure = true
   )
   ScoreComponent name(final String name);

   String objective();

   @Contract(
      pure = true
   )
   ScoreComponent objective(final String objective);

   @Obsolete
   @Nullable
   String value();

   @Obsolete
   @Contract(
      pure = true
   )
   ScoreComponent value(final String value);

   ScoreComponent.Builder toBuilder();

   public sealed interface Builder extends ComponentBuilder<ScoreComponent, ScoreComponent.Builder> permits ScoreComponentImpl.BuilderImpl {
      @Contract("_ -> this")
      ScoreComponent.Builder name(final String name);

      @Contract("_ -> this")
      ScoreComponent.Builder objective(final String objective);

      @Contract("_ -> this")
      @Obsolete
      ScoreComponent.Builder value(@Nullable final String value);
   }
}
