package xyz.lychee.lagfixer.libs.kyori.adventure.text.minimessage.tag.resolver;

import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.Component;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.ComponentLike;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.format.StyleBuilderApplicable;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.minimessage.tag.Tag;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.minimessage.tag.TagPattern;

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
