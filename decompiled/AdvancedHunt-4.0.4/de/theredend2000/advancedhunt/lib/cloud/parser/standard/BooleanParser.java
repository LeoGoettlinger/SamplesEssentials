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
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = API.Status.STABLE
)
public final class BooleanParser<C> implements ArgumentParser<C, Boolean>, BlockingSuggestionProvider.Strings<C> {
   private static final List<String> STRICT_LOWER = CommandInput.BOOLEAN_STRICT.stream().map(s -> s.toLowerCase(Locale.ROOT)).collect(Collectors.toList());
   private static final List<String> LIBERAL_LOWER = CommandInput.BOOLEAN_LIBERAL.stream().map(s -> s.toLowerCase(Locale.ROOT)).collect(Collectors.toList());
   private final boolean liberal;

   @API(
      status = API.Status.STABLE
   )
   @NonNull
   public static <C> ParserDescriptor<C, Boolean> booleanParser() {
      return booleanParser(false);
   }

   @API(
      status = API.Status.STABLE
   )
   @NonNull
   public static <C> ParserDescriptor<C, Boolean> booleanParser(final boolean liberal) {
      return ParserDescriptor.of(new BooleanParser<>(liberal), Boolean.class);
   }

   @API(
      status = API.Status.STABLE
   )
   @NonNull
   public static <C> CommandComponent.Builder<C, Boolean> booleanComponent() {
      return CommandComponent.<C, Boolean>builder().parser(booleanParser());
   }

   public BooleanParser(final boolean liberal) {
      this.liberal = liberal;
   }

   @NonNull
   @Override
   public ArgumentParseResult<Boolean> parse(@NonNull final CommandContext<C> commandContext, @NonNull final CommandInput commandInput) {
      return !commandInput.isValidBoolean(this.liberal)
         ? ArgumentParseResult.failure(new BooleanParser.BooleanParseException(commandInput.peekString(), this.liberal, commandContext))
         : ArgumentParseResult.success(commandInput.readBoolean());
   }

   @NonNull
   @Override
   public Iterable<String> stringSuggestions(@NonNull final CommandContext<C> commandContext, @NonNull final CommandInput input) {
      return !this.liberal ? STRICT_LOWER : LIBERAL_LOWER;
   }

   @API(
      status = API.Status.STABLE
   )
   public static final class BooleanParseException extends ParserException {
      private final String input;
      private final boolean liberal;

      public BooleanParseException(@NonNull final String input, final boolean liberal, @NonNull final CommandContext<?> context) {
         super(BooleanParser.class, context, StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_BOOLEAN, CaptionVariable.of("input", input));
         this.input = input;
         this.liberal = liberal;
      }

      @NonNull
      public String input() {
         return this.input;
      }

      public boolean liberal() {
         return this.liberal;
      }

      @Override
      public boolean equals(final Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            BooleanParser.BooleanParseException that = (BooleanParser.BooleanParseException)o;
            return this.liberal == that.liberal && this.input.equals(that.input);
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.input, this.liberal);
      }
   }
}
