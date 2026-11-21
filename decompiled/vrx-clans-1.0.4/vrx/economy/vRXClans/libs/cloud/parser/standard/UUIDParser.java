package vrx.economy.vRXClans.libs.cloud.parser.standard;

import java.util.Objects;
import java.util.UUID;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import vrx.economy.vRXClans.libs.cloud.caption.CaptionVariable;
import vrx.economy.vRXClans.libs.cloud.caption.StandardCaptionKeys;
import vrx.economy.vRXClans.libs.cloud.component.CommandComponent;
import vrx.economy.vRXClans.libs.cloud.context.CommandContext;
import vrx.economy.vRXClans.libs.cloud.context.CommandInput;
import vrx.economy.vRXClans.libs.cloud.exception.parsing.ParserException;
import vrx.economy.vRXClans.libs.cloud.parser.ArgumentParseResult;
import vrx.economy.vRXClans.libs.cloud.parser.ArgumentParser;
import vrx.economy.vRXClans.libs.cloud.parser.ParserDescriptor;

@API(
   status = API.Status.STABLE
)
public final class UUIDParser<C> implements ArgumentParser<C, UUID> {
   @API(
      status = API.Status.STABLE
   )
   @NonNull
   public static <C> ParserDescriptor<C, UUID> uuidParser() {
      return ParserDescriptor.of(new UUIDParser<>(), UUID.class);
   }

   @API(
      status = API.Status.STABLE
   )
   @NonNull
   public static <C> CommandComponent.Builder<C, UUID> uuidComponent() {
      return CommandComponent.<C, UUID>builder().parser(uuidParser());
   }

   @NonNull
   @Override
   public ArgumentParseResult<UUID> parse(@NonNull final CommandContext<C> commandContext, @NonNull final CommandInput commandInput) {
      String input = commandInput.readString();

      try {
         UUID uuid = UUID.fromString(input);
         return ArgumentParseResult.success(uuid);
      } catch (IllegalArgumentException var5) {
         return ArgumentParseResult.failure(new UUIDParser.UUIDParseException(input, commandContext));
      }
   }

   @API(
      status = API.Status.STABLE
   )
   public static final class UUIDParseException extends ParserException {
      private final String input;

      public UUIDParseException(@NonNull final String input, @NonNull final CommandContext<?> context) {
         super(UUIDParser.class, context, StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_UUID, CaptionVariable.of("input", input));
         this.input = input;
      }

      public String input() {
         return this.input;
      }

      @Override
      public boolean equals(final Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            UUIDParser.UUIDParseException that = (UUIDParser.UUIDParseException)o;
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
