package xyz.lychee.lagfixer.libs.kyori.adventure.text;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lychee.lagfixer.libs.kyori.adventure.internal.Internals;
import xyz.lychee.lagfixer.libs.kyori.examination.ExaminableProperty;

final class TextReplacementConfigImpl implements TextReplacementConfig {
   private final Pattern matchPattern;
   private final BiFunction<MatchResult, TextComponent.Builder, ComponentLike> replacement;
   private final TextReplacementConfig.Condition continuer;
   private final boolean replaceInsideHoverEvents;

   TextReplacementConfigImpl(final TextReplacementConfigImpl.Builder builder) {
      this.matchPattern = builder.matchPattern;
      this.replacement = builder.replacement;
      this.continuer = builder.continuer;
      this.replaceInsideHoverEvents = builder.replaceInsideHoverEvents;
   }

   @NotNull
   @Override
   public Pattern matchPattern() {
      return this.matchPattern;
   }

   TextReplacementRenderer.State createState() {
      return new TextReplacementRenderer.State(this.matchPattern, this.replacement, this.continuer, this.replaceInsideHoverEvents);
   }

   @NotNull
   public TextReplacementConfig.Builder toBuilder() {
      return new TextReplacementConfigImpl.Builder(this);
   }

   @NotNull
   @Override
   public Stream<? extends ExaminableProperty> examinableProperties() {
      return Stream.of(
         ExaminableProperty.of("matchPattern", this.matchPattern),
         ExaminableProperty.of("replacement", this.replacement),
         ExaminableProperty.of("continuer", this.continuer)
      );
   }

   @Override
   public String toString() {
      return Internals.toString(this);
   }

   static final class Builder implements TextReplacementConfig.Builder {
      @Nullable
      Pattern matchPattern;
      @Nullable
      BiFunction<MatchResult, TextComponent.Builder, ComponentLike> replacement;
      TextReplacementConfig.Condition continuer = (matchResult, index, replacement) -> PatternReplacementResult.REPLACE;
      boolean replaceInsideHoverEvents = true;

      Builder() {
      }

      Builder(final TextReplacementConfigImpl instance) {
         this.matchPattern = instance.matchPattern;
         this.replacement = instance.replacement;
         this.continuer = instance.continuer;
      }

      @NotNull
      public TextReplacementConfigImpl.Builder match(@NotNull final Pattern pattern) {
         this.matchPattern = Objects.requireNonNull(pattern, "pattern");
         return this;
      }

      @NotNull
      public TextReplacementConfigImpl.Builder condition(@NotNull final TextReplacementConfig.Condition condition) {
         this.continuer = Objects.requireNonNull(condition, "continuation");
         return this;
      }

      @NotNull
      public TextReplacementConfigImpl.Builder replacement(@NotNull final BiFunction<MatchResult, TextComponent.Builder, ComponentLike> replacement) {
         this.replacement = Objects.requireNonNull(replacement, "replacement");
         return this;
      }

      @NotNull
      @Override
      public TextReplacementConfig.Builder replaceInsideHoverEvents(final boolean replace) {
         this.replaceInsideHoverEvents = replace;
         return this;
      }

      @NotNull
      public TextReplacementConfig build() {
         if (this.matchPattern == null) {
            throw new IllegalStateException("A pattern must be provided to match against");
         } else if (this.replacement == null) {
            throw new IllegalStateException("A replacement action must be provided");
         } else {
            return new TextReplacementConfigImpl(this);
         }
      }
   }
}
