package net.kyori.adventure.text.minimessage;

import net.kyori.adventure.pointer.Pointered;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.NonExtendable;

@NonExtendable
public interface Context {
   @Nullable
   Pointered target();

   @NotNull
   Pointered targetOrThrow();

   @NotNull
   <T extends Pointered> T targetAsType(@NotNull final Class<T> targetClass);

   @NotNull
   Component deserialize(@NotNull final String message);

   @NotNull
   Component deserialize(@NotNull final String message, @NotNull final TagResolver resolver);

   @NotNull
   Component deserialize(@NotNull final String message, @NotNull final TagResolver... resolvers);

   @NotNull
   ParsingException newException(@NotNull final String message, @NotNull final ArgumentQueue tags);

   @NotNull
   ParsingException newException(@NotNull final String message);

   @NotNull
   ParsingException newException(@NotNull final String message, @Nullable final Throwable cause, @NotNull final ArgumentQueue args);
}
