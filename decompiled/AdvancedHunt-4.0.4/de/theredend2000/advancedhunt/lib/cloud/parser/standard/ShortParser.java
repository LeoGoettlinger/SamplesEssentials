package de.theredend2000.advancedhunt.lib.cloud.parser.standard;

import de.theredend2000.advancedhunt.lib.cloud.component.CommandComponent;
import de.theredend2000.advancedhunt.lib.cloud.context.CommandContext;
import de.theredend2000.advancedhunt.lib.cloud.context.CommandInput;
import de.theredend2000.advancedhunt.lib.cloud.exception.parsing.NumberParseException;
import de.theredend2000.advancedhunt.lib.cloud.parser.ArgumentParseResult;
import de.theredend2000.advancedhunt.lib.cloud.parser.ParserDescriptor;
import de.theredend2000.advancedhunt.lib.cloud.suggestion.BlockingSuggestionProvider;
import de.theredend2000.advancedhunt.lib.cloud.type.range.Range;
import de.theredend2000.advancedhunt.lib.cloud.type.range.ShortRange;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = API.Status.STABLE
)
public final class ShortParser<C> extends NumberParser<C, Short, ShortRange> implements BlockingSuggestionProvider.Strings<C> {
   @API(
      status = API.Status.STABLE
   )
   public static final short DEFAULT_MINIMUM = -32768;
   @API(
      status = API.Status.STABLE
   )
   public static final short DEFAULT_MAXIMUM = 32767;

   @API(
      status = API.Status.STABLE
   )
   @NonNull
   public static <C> ParserDescriptor<C, Short> shortParser() {
      return shortParser((short)-32768, (short)32767);
   }

   @API(
      status = API.Status.STABLE
   )
   @NonNull
   public static <C> ParserDescriptor<C, Short> shortParser(final short minValue) {
      return ParserDescriptor.of(new ShortParser<>(minValue, (short)32767), Short.class);
   }

   @API(
      status = API.Status.STABLE
   )
   @NonNull
   public static <C> ParserDescriptor<C, Short> shortParser(final short minValue, final short maxValue) {
      return ParserDescriptor.of(new ShortParser<>(minValue, maxValue), Short.class);
   }

   @API(
      status = API.Status.STABLE
   )
   @NonNull
   public static <C> CommandComponent.Builder<C, Short> shortComponent() {
      return CommandComponent.<C, Short>builder().parser(shortParser());
   }

   public ShortParser(final short min, final short max) {
      super(Range.shortRange(min, max));
   }

   @NonNull
   @Override
   public ArgumentParseResult<Short> parse(@NonNull final CommandContext<C> commandContext, @NonNull final CommandInput commandInput) {
      return !commandInput.isValidShort(this.range())
         ? ArgumentParseResult.failure(new ShortParser.ShortParseException(commandInput.peekString(), this, commandContext))
         : ArgumentParseResult.success(commandInput.readShort());
   }

   @Override
   public boolean hasMax() {
      return this.range().maxShort() != 32767;
   }

   @Override
   public boolean hasMin() {
      return this.range().minShort() != -32768;
   }

   @NonNull
   @Override
   public Iterable<String> stringSuggestions(@NonNull final CommandContext<C> commandContext, @NonNull final CommandInput input) {
      return IntegerParser.getSuggestions(this.range(), input);
   }

   @API(
      status = API.Status.STABLE
   )
   public static final class ShortParseException extends NumberParseException {
      @API(
         status = API.Status.STABLE
      )
      public ShortParseException(@NonNull final String input, @NonNull final ShortParser<?> parser, @NonNull final CommandContext<?> commandContext) {
         super(input, parser, commandContext);
      }

      @NonNull
      @Override
      public String numberType() {
         return "short";
      }
   }
}
