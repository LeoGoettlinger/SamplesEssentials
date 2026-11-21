package de.theredend2000.advancedhunt.lib.cloud.parser.standard;

import de.theredend2000.advancedhunt.lib.cloud.caption.CaptionVariable;
import de.theredend2000.advancedhunt.lib.cloud.caption.StandardCaptionKeys;
import de.theredend2000.advancedhunt.lib.cloud.component.CommandComponent;
import de.theredend2000.advancedhunt.lib.cloud.context.CommandContext;
import de.theredend2000.advancedhunt.lib.cloud.context.CommandInput;
import de.theredend2000.advancedhunt.lib.cloud.exception.parsing.ParserException;
import de.theredend2000.advancedhunt.lib.cloud.parser.ArgumentParseResult;
import de.theredend2000.advancedhunt.lib.cloud.parser.ArgumentParser;
import de.theredend2000.advancedhunt.lib.cloud.parser.ParserDescriptor;
import de.theredend2000.advancedhunt.lib.cloud.suggestion.BlockingSuggestionProvider;
import java.time.Duration;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = API.Status.STABLE
)
public final class DurationParser<C> implements ArgumentParser<C, Duration>, BlockingSuggestionProvider.Strings<C> {
   private static final Pattern DURATION_PATTERN = Pattern.compile("(([1-9][0-9]+|[1-9])[dhms])");

   @API(
      status = API.Status.STABLE
   )
   @NonNull
   public static <C> ParserDescriptor<C, Duration> durationParser() {
      return ParserDescriptor.of(new DurationParser<>(), Duration.class);
   }

   @API(
      status = API.Status.STABLE
   )
   @NonNull
   public static <C> CommandComponent.Builder<C, Duration> durationComponent() {
      return CommandComponent.<C, Duration>builder().parser(durationParser());
   }

   @NonNull
   @Override
   public ArgumentParseResult<Duration> parse(@NonNull final CommandContext<C> commandContext, @NonNull final CommandInput commandInput) {
      String input = commandInput.readString();
      Matcher matcher = DURATION_PATTERN.matcher(input);
      Duration duration = Duration.ofNanos(0L);

      while (matcher.find()) {
         String group = matcher.group();
         String timeUnit = String.valueOf(group.charAt(group.length() - 1));
         int timeValue = Integer.parseInt(group.substring(0, group.length() - 1));
         switch (timeUnit) {
            case "d":
               duration = duration.plusDays(timeValue);
               break;
            case "h":
               duration = duration.plusHours(timeValue);
               break;
            case "m":
               duration = duration.plusMinutes(timeValue);
               break;
            case "s":
               duration = duration.plusSeconds(timeValue);
               break;
            default:
               return ArgumentParseResult.failure(new DurationParser.DurationParseException(input, commandContext));
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
      status = API.Status.STABLE
   )
   public static final class DurationParseException extends ParserException {
      private final String input;

      public DurationParseException(@NonNull final String input, @NonNull final CommandContext<?> context) {
         super(DurationParser.class, context, StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_DURATION, CaptionVariable.of("input", input));
         this.input = input;
      }

      @NonNull
      public String input() {
         return this.input;
      }
   }
}
