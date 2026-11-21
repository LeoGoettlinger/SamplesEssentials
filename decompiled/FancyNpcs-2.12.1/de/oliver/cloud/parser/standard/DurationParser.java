package de.oliver.cloud.parser.standard;

import de.oliver.cloud.caption.CaptionVariable;
import de.oliver.cloud.caption.StandardCaptionKeys;
import de.oliver.cloud.component.CommandComponent;
import de.oliver.cloud.context.CommandContext;
import de.oliver.cloud.context.CommandInput;
import de.oliver.cloud.exception.parsing.ParserException;
import de.oliver.cloud.parser.ArgumentParseResult;
import de.oliver.cloud.parser.ArgumentParser;
import de.oliver.cloud.parser.ParserDescriptor;
import de.oliver.cloud.suggestion.BlockingSuggestionProvider;
import java.time.Duration;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@API(
   status = Status.STABLE
)
public final class DurationParser<C> implements ArgumentParser<C, Duration>, BlockingSuggestionProvider.Strings<C> {
   @API(
      status = Status.STABLE
   )
   @NonNull
   public static <C> ParserDescriptor<C, Duration> durationParser() {
      return ParserDescriptor.of(new DurationParser<>(), Duration.class);
   }

   @API(
      status = Status.STABLE
   )
   @NonNull
   public static <C> CommandComponent.Builder<C, Duration> durationComponent() {
      return CommandComponent.<C, Duration>builder().parser(durationParser());
   }

   @NonNull
   @Override
   public ArgumentParseResult<Duration> parse(@NonNull final CommandContext<C> commandContext, @NonNull final CommandInput commandInput) {
      String input = commandInput.readString();
      Duration duration = Duration.ofNanos(0L);
      int rangeStart = 0;

      for (int cursor = 0; cursor < input.length(); rangeStart = ++cursor) {
         while (cursor < input.length() && input.charAt(cursor) >= '0' && input.charAt(cursor) <= '9') {
            cursor++;
         }

         if (cursor == input.length()) {
            return ArgumentParseResult.failure(new DurationParser.DurationParseException(input, commandContext));
         }

         long timeValue;
         try {
            timeValue = Long.parseLong(input.substring(rangeStart, cursor));
         } catch (NumberFormatException var12) {
            return ArgumentParseResult.failure(new DurationParser.DurationParseException(var12, input, commandContext));
         }

         char timeUnit = input.charAt(cursor);

         try {
            switch (timeUnit) {
               case 'd':
                  duration = duration.plusDays(timeValue);
                  break;
               case 'h':
                  duration = duration.plusHours(timeValue);
                  break;
               case 'm':
                  duration = duration.plusMinutes(timeValue);
                  break;
               case 's':
                  duration = duration.plusSeconds(timeValue);
                  break;
               default:
                  return ArgumentParseResult.failure(new DurationParser.DurationParseException(input, commandContext));
            }
         } catch (ArithmeticException var11) {
            return ArgumentParseResult.failure(new DurationParser.DurationParseException(var11, input, commandContext));
         }
      }

      return duration.isZero()
         ? ArgumentParseResult.failure(new DurationParser.DurationParseException(input, commandContext))
         : ArgumentParseResult.success(duration);
   }

   @NonNull
   @Override
   public Iterable<String> stringSuggestions(@NonNull final CommandContext<C> commandContext, @NonNull final CommandInput input) {
      if (input.isEmpty(true)) {
         return IntStream.range(1, 10).boxed().sorted().map(String::valueOf).collect(Collectors.toList());
      } else if (Character.isLetter(input.lastRemainingCharacter())) {
         return Collections.emptyList();
      } else {
         String string = input.readString();
         return Stream.of("d", "h", "m", "s").filter(unit -> !string.contains(unit)).map(unit -> string + unit).collect(Collectors.toList());
      }
   }

   @API(
      status = Status.STABLE
   )
   public static final class DurationParseException extends ParserException {
      private final String input;

      public DurationParseException(@NonNull final String input, @NonNull final CommandContext<?> context) {
         super(DurationParser.class, context, StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_DURATION, CaptionVariable.of("input", input));
         this.input = input;
      }

      public DurationParseException(@Nullable final Throwable cause, @NonNull final String input, @NonNull final CommandContext<?> context) {
         super(cause, DurationParser.class, context, StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_DURATION, CaptionVariable.of("input", input));
         this.input = input;
      }

      @NonNull
      public String input() {
         return this.input;
      }
   }
}
