package vrx.economy.vRXClans.libs.cloud.parser.standard;

import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import vrx.economy.vRXClans.libs.cloud.component.CommandComponent;
import vrx.economy.vRXClans.libs.cloud.context.CommandContext;
import vrx.economy.vRXClans.libs.cloud.context.CommandInput;
import vrx.economy.vRXClans.libs.cloud.exception.parsing.NumberParseException;
import vrx.economy.vRXClans.libs.cloud.parser.ArgumentParseResult;
import vrx.economy.vRXClans.libs.cloud.parser.ParserDescriptor;
import vrx.economy.vRXClans.libs.cloud.suggestion.BlockingSuggestionProvider;
import vrx.economy.vRXClans.libs.cloud.type.range.ByteRange;
import vrx.economy.vRXClans.libs.cloud.type.range.Range;

@API(
   status = API.Status.STABLE
)
public final class ByteParser<C> extends NumberParser<C, Byte, ByteRange> implements BlockingSuggestionProvider.Strings<C> {
   @API(
      status = API.Status.STABLE
   )
   public static final byte DEFAULT_MINIMUM = -128;
   @API(
      status = API.Status.STABLE
   )
   public static final byte DEFAULT_MAXIMUM = 127;

   @API(
      status = API.Status.STABLE
   )
   @NonNull
   public static <C> ParserDescriptor<C, Byte> byteParser() {
      return byteParser((byte)-128, (byte)127);
   }

   @API(
      status = API.Status.STABLE
   )
   @NonNull
   public static <C> ParserDescriptor<C, Byte> byteParser(final byte minValue) {
      return ParserDescriptor.of(new ByteParser<>(minValue, (byte)127), Byte.class);
   }

   @API(
      status = API.Status.STABLE
   )
   @NonNull
   public static <C> ParserDescriptor<C, Byte> byteParser(final byte minValue, final byte maxValue) {
      return ParserDescriptor.of(new ByteParser<>(minValue, maxValue), Byte.class);
   }

   @API(
      status = API.Status.STABLE
   )
   @NonNull
   public static <C> CommandComponent.Builder<C, Byte> byteComponent() {
      return CommandComponent.<C, Byte>builder().parser(byteParser());
   }

   public ByteParser(final byte min, final byte max) {
      super(Range.byteRange(min, max));
   }

   @NonNull
   @Override
   public ArgumentParseResult<Byte> parse(@NonNull final CommandContext<C> commandContext, @NonNull final CommandInput commandInput) {
      return !commandInput.isValidByte(this.range())
         ? ArgumentParseResult.failure(new ByteParser.ByteParseException(commandInput.peekString(), this, commandContext))
         : ArgumentParseResult.success(commandInput.readByte());
   }

   @Override
   public boolean hasMax() {
      return this.range().maxByte() != 127;
   }

   @Override
   public boolean hasMin() {
      return this.range().minByte() != -128;
   }

   @NonNull
   @Override
   public Iterable<String> stringSuggestions(@NonNull final CommandContext<C> commandContext, @NonNull final CommandInput input) {
      return IntegerParser.getSuggestions(this.range(), input);
   }

   @API(
      status = API.Status.STABLE
   )
   public static final class ByteParseException extends NumberParseException {
      @API(
         status = API.Status.STABLE
      )
      public ByteParseException(@NonNull final String input, @NonNull final ByteParser<?> parser, @NonNull final CommandContext<?> commandContext) {
         super(input, parser, commandContext);
      }

      @NonNull
      @Override
      public String numberType() {
         return "byte";
      }
   }
}
