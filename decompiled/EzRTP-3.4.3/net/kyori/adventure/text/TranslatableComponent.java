package net.kyori.adventure.text;

import java.util.List;
import java.util.Objects;
import net.kyori.adventure.translation.Translatable;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

public sealed interface TranslatableComponent
   extends ScopedComponent<TranslatableComponent>,
   BuildableComponent<TranslatableComponent, TranslatableComponent.Builder>
   permits TranslatableComponentImpl {
   String key();

   @Contract(
      pure = true
   )
   default TranslatableComponent key(final Translatable translatable) {
      return this.key(Objects.requireNonNull(translatable, "translatable").translationKey());
   }

   @Contract(
      pure = true
   )
   TranslatableComponent key(final String key);

   List<TranslationArgument> arguments();

   @Contract(
      pure = true
   )
   TranslatableComponent arguments(final ComponentLike... args);

   @Contract(
      pure = true
   )
   TranslatableComponent arguments(final List<? extends ComponentLike> args);

   @Nullable
   String fallback();

   @Contract(
      pure = true
   )
   TranslatableComponent fallback(@Nullable final String fallback);

   TranslatableComponent.Builder toBuilder();

   public sealed interface Builder extends ComponentBuilder<TranslatableComponent, TranslatableComponent.Builder> permits TranslatableComponentImpl.BuilderImpl {
      @Contract(
         pure = true
      )
      default TranslatableComponent.Builder key(final Translatable translatable) {
         return this.key(Objects.requireNonNull(translatable, "translatable").translationKey());
      }

      @Contract("_ -> this")
      TranslatableComponent.Builder key(final String key);

      @Contract("_ -> this")
      TranslatableComponent.Builder arguments(final ComponentLike... args);

      @Contract("_ -> this")
      TranslatableComponent.Builder arguments(final List<? extends ComponentLike> args);

      @Contract("_ -> this")
      TranslatableComponent.Builder fallback(@Nullable final String fallback);
   }
}
