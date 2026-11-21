package net.kyori.adventure.text;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;

record TextReplacementConfigImpl(
   Pattern matchPattern,
   BiFunction<MatchResult, TextComponent.Builder, ComponentLike> replacement,
   TextReplacementConfig.Condition continuer,
   boolean replaceInsideHoverEvents
) implements TextReplacementConfig {
   TextReplacementRenderer.State createState() {
      return new TextReplacementRenderer.State(this.matchPattern, this.replacement, this.continuer, this.replaceInsideHoverEvents);
   }

   static final class Builder implements TextReplacementConfig.Builder {
      @Nullable
      Pattern matchPattern;
      @Nullable
      BiFunction<MatchResult, TextComponent.Builder, ComponentLike> replacement;
      TextReplacementConfig.Condition continuer = (matchResult, index, replacement) -> PatternReplacementResult.REPLACE;
      boolean replaceInsideHoverEvents = true;

      public TextReplacementConfigImpl.Builder match(final Pattern pattern) {
         this.matchPattern = Objects.requireNonNull(pattern, "pattern");
         return this;
      }

      public TextReplacementConfigImpl.Builder condition(final TextReplacementConfig.Condition condition) {
         this.continuer = Objects.requireNonNull(condition, "continuation");
         return this;
      }

      public TextReplacementConfigImpl.Builder replacement(final BiFunction<MatchResult, TextComponent.Builder, ComponentLike> replacement) {
         this.replacement = Objects.requireNonNull(replacement, "replacement");
         return this;
      }

      @Override
      public TextReplacementConfig.Builder replaceInsideHoverEvents(final boolean replace) {
         this.replaceInsideHoverEvents = replace;
         return this;
      }

      public TextReplacementConfig build() {
         if (this.matchPattern == null) {
            throw new IllegalStateException("A pattern must be provided to match against");
         } else if (this.replacement == null) {
            throw new IllegalStateException("A replacement action must be provided");
         } else {
            return new TextReplacementConfigImpl(this.matchPattern, this.replacement, this.continuer, this.replaceInsideHoverEvents);
         }
      }
   }
}
