package de.oliver.cloud.component.preprocessor;

import de.oliver.cloud.caption.Caption;
import de.oliver.cloud.caption.CaptionVariable;
import de.oliver.cloud.caption.StandardCaptionKeys;
import de.oliver.cloud.context.CommandContext;
import de.oliver.cloud.context.CommandInput;
import de.oliver.cloud.parser.ArgumentParseResult;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = Status.STABLE
)
public final class RegexPreprocessor<C> implements ComponentPreprocessor<C> {
   private final String rawPattern;
   private final Predicate<String> predicate;
   private final Caption failureCaption;

   private RegexPreprocessor(@NonNull final String pattern, @NonNull final Caption failureCaption) {
      this.rawPattern = pattern;
      this.predicate = Pattern.compile(pattern).asPredicate();
      this.failureCaption = failureCaption;
   }

   @NonNull
   public static <C> RegexPreprocessor<C> of(@NonNull final String pattern) {
      return of(pattern, StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_REGEX);
   }

   @NonNull
   public static <C> RegexPreprocessor<C> of(@NonNull final String pattern, @NonNull final Caption failureCaption) {
      return new RegexPreprocessor<>(pattern, failureCaption);
   }

   @NonNull
   @Override
   public ArgumentParseResult<Boolean> preprocess(@NonNull final CommandContext<C> context, @NonNull final CommandInput commandInput) {
      String head = commandInput.peekString();
      return this.predicate.test(head)
         ? ArgumentParseResult.success(true)
         : ArgumentParseResult.failure(new RegexPreprocessor.RegexValidationException(this.rawPattern, head, this.failureCaption, context));
   }

   @API(
      status = Status.STABLE
   )
   public static final class RegexValidationException extends IllegalArgumentException {
      private final String pattern;
      private final String failedString;
      private final Caption failureCaption;
      private final CommandContext<?> commandContext;

      private RegexValidationException(
         @NonNull final String pattern,
         @NonNull final String failedString,
         @NonNull final Caption failureCaption,
         @NonNull final CommandContext<?> commandContext
      ) {
         this.pattern = pattern;
         this.failedString = failedString;
         this.failureCaption = failureCaption;
         this.commandContext = commandContext;
      }

      @Override
      public String getMessage() {
         return this.commandContext
            .formatCaption(this.failureCaption, CaptionVariable.of("input", this.failedString), CaptionVariable.of("pattern", this.pattern));
      }

      @NonNull
      public String failedInput() {
         return this.failedString;
      }

      @NonNull
      public String pattern() {
         return this.pattern;
      }
   }
}
