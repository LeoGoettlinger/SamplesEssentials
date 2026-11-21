package vrx.economy.vRXClans.libs.cloud.exception;

import java.util.List;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import vrx.economy.vRXClans.libs.cloud.component.CommandComponent;

@API(
   status = API.Status.STABLE
)
public class InvalidSyntaxException extends CommandParseException {
   private final String correctSyntax;

   @API(
      status = API.Status.INTERNAL,
      consumers = {"vrx.economy.vRXClans.libs.cloud.*"}
   )
   public InvalidSyntaxException(
      @NonNull final String correctSyntax, @NonNull final Object commandSender, @NonNull final List<CommandComponent<?>> currentChain
   ) {
      super(commandSender, currentChain);
      this.correctSyntax = correctSyntax;
   }

   @NonNull
   public String correctSyntax() {
      return this.correctSyntax;
   }

   @Override
   public final String getMessage() {
      return String.format("Invalid command syntax. Correct syntax is: %s", this.correctSyntax);
   }
}
