package net.kyori.adventure.text;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import net.kyori.adventure.builder.AbstractBuilder;
import net.kyori.adventure.util.IntFunction2;
import org.intellij.lang.annotations.RegExp;
import org.jetbrains.annotations.Contract;

public sealed interface TextReplacementConfig permits TextReplacementConfigImpl {
   static TextReplacementConfig.Builder builder() {
      return new TextReplacementConfigImpl.Builder();
   }

   Pattern matchPattern();

   public sealed interface Builder extends AbstractBuilder<TextReplacementConfig> permits TextReplacementConfigImpl.Builder {
      @Contract("_ -> this")
      default TextReplacementConfig.Builder matchLiteral(final String literal) {
         return this.match(Pattern.compile(literal, 16));
      }

      @Contract("_ -> this")
      default TextReplacementConfig.Builder match(@RegExp final String pattern) {
         return this.match(Pattern.compile(pattern));
      }

      @Contract("_ -> this")
      TextReplacementConfig.Builder match(final Pattern pattern);

      default TextReplacementConfig.Builder once() {
         return this.times(1);
      }

      @Contract("_ -> this")
      default TextReplacementConfig.Builder times(final int times) {
         return this.condition((index, replaced) -> replaced < times ? PatternReplacementResult.REPLACE : PatternReplacementResult.STOP);
      }

      @Contract("_ -> this")
      default TextReplacementConfig.Builder condition(final IntFunction2<PatternReplacementResult> condition) {
         return this.condition((result, matchCount, replaced) -> condition.apply(matchCount, replaced));
      }

      @Contract("_ -> this")
      TextReplacementConfig.Builder condition(final TextReplacementConfig.Condition condition);

      @Contract("_ -> this")
      default TextReplacementConfig.Builder replacement(final String replacement) {
         Objects.requireNonNull(replacement, "replacement");
         return this.replacement(builder -> builder.content(replacement));
      }

      @Contract("_ -> this")
      default TextReplacementConfig.Builder replacement(final ComponentLike replacement) {
         Component baked = ComponentLike.unbox(replacement);
         return this.replacement((result, input) -> baked);
      }

      @Contract("_ -> this")
      default TextReplacementConfig.Builder replacement(final Function<TextComponent.Builder, ComponentLike> replacement) {
         Objects.requireNonNull(replacement, "replacement");
         return this.replacement((result, input) -> replacement.apply(input));
      }

      @Contract("_ -> this")
      TextReplacementConfig.Builder replacement(final BiFunction<MatchResult, TextComponent.Builder, ComponentLike> replacement);

      @Contract("_ -> this")
      TextReplacementConfig.Builder replaceInsideHoverEvents(final boolean replace);
   }

   @FunctionalInterface
   public interface Condition {
      PatternReplacementResult shouldReplace(final MatchResult result, final int matchCount, final int replaced);
   }
}
