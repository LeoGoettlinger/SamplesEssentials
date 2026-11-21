package com.earth2me.essentials.libs.kyori.adventure.text.minimessage.translation;

import com.earth2me.essentials.libs.kyori.adventure.pointer.Pointered;
import com.earth2me.essentials.libs.kyori.adventure.text.Component;
import com.earth2me.essentials.libs.kyori.adventure.text.ComponentLike;
import com.earth2me.essentials.libs.kyori.adventure.text.TranslationArgument;
import com.earth2me.essentials.libs.kyori.adventure.text.TranslationArgumentLike;
import com.earth2me.essentials.libs.kyori.adventure.text.minimessage.tag.Tag;
import com.earth2me.essentials.libs.kyori.adventure.text.minimessage.tag.TagPattern;
import com.earth2me.essentials.libs.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public final class Argument {
   private Argument() {
   }

   @NotNull
   public static ComponentLike bool(@TagPattern @NotNull final String name, final boolean value) {
      return argument(name, TranslationArgument.bool(value));
   }

   @NotNull
   public static ComponentLike numeric(@TagPattern @NotNull final String name, @NotNull final Number value) {
      return argument(name, TranslationArgument.numeric(value));
   }

   @Deprecated
   @NotNull
   public static ComponentLike numeric(@TagPattern @NotNull final String name, @NotNull final String value) {
      return string(name, value);
   }

   @NotNull
   public static ComponentLike string(@TagPattern @NotNull final String name, @NotNull final String value) {
      return argument(name, TranslationArgument.component(Component.text(value)));
   }

   @NotNull
   public static ComponentLike component(@TagPattern @NotNull final String name, @NotNull final ComponentLike value) {
      return argument(name, TranslationArgument.component(value));
   }

   @NotNull
   public static ComponentLike argument(@TagPattern @NotNull final String name, @NotNull final TranslationArgumentLike argument) {
      return argument(name, Objects.requireNonNull(argument, "argument").asTranslationArgument());
   }

   @NotNull
   public static ComponentLike argument(@TagPattern @NotNull final String name, @NotNull final TranslationArgument argument) {
      return Component.virtual(Void.class, new MiniMessageTranslatorArgument<>(name, Objects.requireNonNull(argument, "argument")));
   }

   @NotNull
   public static ComponentLike tag(@TagPattern @NotNull final String name, @NotNull final Tag tag) {
      return Component.virtual(Void.class, new MiniMessageTranslatorArgument<>(name, Objects.requireNonNull(tag, "tag")));
   }

   @NotNull
   public static ComponentLike tagResolver(@NotNull final TagResolver... resolvers) {
      return tagResolver(TagResolver.resolver(resolvers));
   }

   @NotNull
   public static ComponentLike tagResolver(@NotNull final Iterable<TagResolver> resolvers) {
      return tagResolver(TagResolver.resolver(resolvers));
   }

   @NotNull
   public static ComponentLike tagResolver(@NotNull final TagResolver tagResolver) {
      return Component.virtual(Void.class, new MiniMessageTranslatorArgument<>("unused", Objects.requireNonNull(tagResolver, "tagResolver")));
   }

   @NotNull
   public static ComponentLike target(@NotNull final Pointered target) {
      return Component.virtual(Void.class, new MiniMessageTranslatorTarget(Objects.requireNonNull(target, "target")));
   }
}
