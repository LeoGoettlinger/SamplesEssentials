package de.oliver.cloud.parser.standard;

import de.oliver.cloud.component.CommandComponent;
import de.oliver.cloud.context.CommandContext;
import de.oliver.cloud.context.CommandInput;
import de.oliver.cloud.exception.parsing.NumberParseException;
import de.oliver.cloud.parser.ArgumentParseResult;
import de.oliver.cloud.parser.ParserDescriptor;
import de.oliver.cloud.suggestion.BlockingSuggestionProvider;
import de.oliver.cloud.type.range.LongRange;
import de.oliver.cloud.type.range.Range;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = Status.STABLE
)
public final class LongParser<C> extends NumberParser<C, Long, LongRange> implements BlockingSuggestionProvider.Strings<C> {
   @API(
      status = Status.STABLE
   )
   public static final long DEFAULT_MINIMUM = Long.MIN_VALUE;
   @API(
      status = Status.STABLE
   )
   public static final long DEFAULT_MAXIMUM = Long.MAX_VALUE;

   @API(
      status = Status.STABLE
   )
   @NonNull
   public static <C> ParserDescriptor<C, Long> longParser() {
      return longParser(Long.MIN_VALUE, Long.MAX_VALUE);
   }

   @API(
      status = Status.STABLE
   )
   @NonNull
   public static <C> ParserDescriptor<C, Long> longParser(final long minValue) {
      return ParserDescriptor.of(new LongParser<>(minValue, Long.MAX_VALUE), Long.class);
   }

   @API(
      status = Status.STABLE
   )
   @NonNull
   public static <C> ParserDescriptor<C, Long> longParser(final long minValue, final long maxValue) {
      return ParserDescriptor.of(new LongParser<>(minValue, maxValue), Long.class);
   }

   @API(
      status = Status.STABLE
   )
   @NonNull
   public static <C> CommandComponent.Builder<C, Long> longComponent() {
      return CommandComponent.<C, Long>builder().parser(longParser());
   }

   public LongParser(final long min, final long max) {
      super(Range.longRange(min, max));
   }

   @NonNull
   @Override
   public ArgumentParseResult<Long> parse(@NonNull final CommandContext<C> commandContext, @NonNull final CommandInput commandInput) {
      return !commandInput.isValidLong(this.range())
         ? ArgumentParseResult.failure(new LongParser.LongParseException(commandInput.peekString(), this, commandContext))
         : ArgumentParseResult.success(commandInput.readLong());
   }

   @Override
   public boolean hasMax() {
      return this.range().maxLong() != Long.MAX_VALUE;
   }

   @Override
   public boolean hasMin() {
      return this.range().minLong() != Long.MIN_VALUE;
   }

   @NonNull
   @Override
   public Iterable<String> stringSuggestions(@NonNull final CommandContext<C> commandContext, @NonNull final CommandInput input) {
      return IntegerParser.getSuggestions(this.range(), input);
   }

   @API(
      status = Status.STABLE
   )
   public static final class LongParseException extends NumberParseException {
      @API(
         status = Status.STABLE
      )
      public LongParseException(@NonNull final String input, @NonNull final LongParser<?> parser, @NonNull final CommandContext<?> commandContext) {
         super(input, parser, commandContext);
      }

      @NonNull
      @Override
      public String numberType() {
         return "long";
      }
   }
}
