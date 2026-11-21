package net.kyori.adventure.text.minimessage.tag.resolver;

import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.StyleBuilderApplicable;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.TagPattern;
import org.jetbrains.annotations.NotNull;

public final class Placeholder {
   private Placeholder() {
   }

   @NotNull
   public static TagResolver.Single parsed(@TagPattern @NotNull final String key, @NotNull final String value) {
      return TagResolver.resolver(key, Tag.preProcessParsed(value));
   }

   @NotNull
   public static TagResolver.Single unparsed(@TagPattern @NotNull final String key, @NotNull final String value) {
      Objects.requireNonNull(value, "value");
      return component(key, Component.text(value));
   }

   @NotNull
   public static TagResolver.Single component(@TagPattern @NotNull final String key, @NotNull final ComponentLike value) {
      return TagResolver.resolver(key, Tag.selfClosingInserting(value));
   }

   @NotNull
   public static TagResolver.Single styling(@TagPattern @NotNull final String key, @NotNull final StyleBuilderApplicable... style) {
      return TagResolver.resolver(key, Tag.styling(style));
   }
}
