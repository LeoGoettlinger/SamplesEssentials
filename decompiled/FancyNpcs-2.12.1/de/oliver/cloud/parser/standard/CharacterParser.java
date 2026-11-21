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
import java.util.Objects;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = Status.STABLE
)
public final class CharacterParser<C> implements ArgumentParser<C, Character> {
   @API(
      status = Status.STABLE
   )
   @NonNull
   public static <C> ParserDescriptor<C, Character> characterParser() {
      return ParserDescriptor.of(new CharacterParser<>(), Character.class);
   }

   @API(
      status = Status.STABLE
   )
   @NonNull
   public static <C> CommandComponent.Builder<C, Character> characterComponent() {
      return CommandComponent.<C, Character>builder().parser(characterParser());
   }

   @NonNull
   @Override
   public ArgumentParseResult<Character> parse(@NonNull final CommandContext<C> commandContext, @NonNull final CommandInput commandInput) {
      return commandInput.peekString().length() != 1
         ? ArgumentParseResult.failure(new CharacterParser.CharParseException(commandInput.peekString(), commandContext))
         : ArgumentParseResult.success(commandInput.read());
   }

   @API(
      status = Status.STABLE
   )
   public static final class CharParseException extends ParserException {
      private final String input;

      public CharParseException(@NonNull final String input, @NonNull final CommandContext<?> context) {
         super(CharacterParser.class, context, StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_CHAR, CaptionVariable.of("input", input));
         this.input = input;
      }

      @NonNull
      public String input() {
         return this.input;
      }

      @Override
      public boolean equals(final Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            CharacterParser.CharParseException that = (CharacterParser.CharParseException)o;
            return this.input.equals(that.input);
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.input);
      }
   }
}
