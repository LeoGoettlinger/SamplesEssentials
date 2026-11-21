package de.oliver.cloud.parser.standard;

import de.oliver.cloud.component.CommandComponent;
import de.oliver.cloud.context.CommandContext;
import de.oliver.cloud.context.CommandInput;
import de.oliver.cloud.exception.parsing.NumberParseException;
import de.oliver.cloud.parser.ArgumentParseResult;
import de.oliver.cloud.parser.ParserDescriptor;
import de.oliver.cloud.suggestion.BlockingSuggestionProvider;
import de.oliver.cloud.type.range.IntRange;
import de.oliver.cloud.type.range.Range;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = Status.STABLE
)
public final class IntegerParser<C> extends NumberParser<C, Integer, IntRange> implements BlockingSuggestionProvider.Strings<C> {
   @API(
      status = Status.STABLE
   )
   public static final int DEFAULT_MINIMUM = Integer.MIN_VALUE;
   @API(
      status = Status.STABLE
   )
   public static final int DEFAULT_MAXIMUM = Integer.MAX_VALUE;
   private static final int MAX_SUGGESTIONS_INCREMENT = 10;
   private static final int NUMBER_SHIFT_MULTIPLIER = 10;

   @API(
      status = Status.STABLE
   )
   @NonNull
   public static <C> ParserDescriptor<C, Integer> integerParser() {
      return integerParser(Integer.MIN_VALUE, Integer.MAX_VALUE);
   }

   @API(
      status = Status.STABLE
   )
   @NonNull
   public static <C> ParserDescriptor<C, Integer> integerParser(final int minValue) {
      return ParserDescriptor.of(new IntegerParser<>(minValue, Integer.MAX_VALUE), Integer.class);
   }

   @API(
      status = Status.STABLE
   )
   @NonNull
   public static <C> ParserDescriptor<C, Integer> integerParser(final int minValue, final int maxValue) {
      return ParserDescriptor.of(new IntegerParser<>(minValue, maxValue), Integer.class);
   }

   @API(
      status = Status.STABLE
   )
   @NonNull
   public static <C> CommandComponent.Builder<C, Integer> integerComponent() {
      return CommandComponent.<C, Integer>builder().parser(integerParser());
   }

   public IntegerParser(final int min, final int max) {
      super(Range.intRange(min, max));
   }

   @NonNull
   public static List<String> getSuggestions(@NonNull final Range<? extends Number> range, @NonNull final CommandInput input) {
      Set<Long> numbers = new TreeSet<>();
      String token = input.peekString();

      try {
         long inputNum = Long.parseLong(token.equals("-") ? "-0" : (token.isEmpty() ? "0" : token));
         long inputNumAbsolute = Math.abs(inputNum);
         long min = range.min().longValue();
         long max = range.max().longValue();
         numbers.add(inputNumAbsolute);

         for (int i = 0; i < 10 && inputNum * 10L + i <= max; i++) {
            numbers.add(inputNumAbsolute * 10L + i);
         }

         List<String> suggestions = new LinkedList<>();

         for (long number : numbers) {
            if (token.startsWith("-")) {
               number = -number;
            }

            if (number >= min && number <= max) {
               suggestions.add(String.valueOf(number));
            }
         }

         return suggestions;
      } catch (Exception var16) {
         return Collections.emptyList();
      }
   }

   @NonNull
   @Override
   public ArgumentParseResult<Integer> parse(@NonNull final CommandContext<C> commandContext, @NonNull final CommandInput commandInput) {
      return !commandInput.isValidInteger(this.range())
         ? ArgumentParseResult.failure(new IntegerParser.IntegerParseException(commandInput.peekString(), this, commandContext))
         : ArgumentParseResult.success(commandInput.readInteger());
   }

   @Override
   public boolean hasMax() {
      return this.range().maxInt() != Integer.MAX_VALUE;
   }

   @Override
   public boolean hasMin() {
      return this.range().minInt() != Integer.MIN_VALUE;
   }

   @NonNull
   @Override
   public Iterable<String> stringSuggestions(@NonNull final CommandContext<C> commandContext, @NonNull final CommandInput input) {
      return getSuggestions(this.range(), input);
   }

   @API(
      status = Status.STABLE
   )
   public static final class IntegerParseException extends NumberParseException {
      @API(
         status = Status.STABLE
      )
      public IntegerParseException(@NonNull final String input, @NonNull final IntegerParser<?> parser, @NonNull final CommandContext<?> commandContext) {
         super(input, parser, commandContext);
      }

      @NonNull
      @Override
      public String numberType() {
         return "integer";
      }
   }
}
