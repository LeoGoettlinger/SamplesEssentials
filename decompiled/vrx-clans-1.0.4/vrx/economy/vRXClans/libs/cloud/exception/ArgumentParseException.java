package vrx.economy.vRXClans.libs.cloud.exception;

import java.util.List;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import vrx.economy.vRXClans.libs.cloud.component.CommandComponent;

@API(
   status = API.Status.STABLE
)
public class ArgumentParseException extends CommandParseException {
   private final Throwable cause;

   @API(
      status = API.Status.INTERNAL,
      consumers = {"vrx.economy.vRXClans.libs.cloud.*"}
   )
   public ArgumentParseException(@NonNull final Throwable throwable, @NonNull final Object commandSender, @NonNull final List<CommandComponent<?>> currentChain) {
      super(commandSender, currentChain);
      this.cause = throwable;
   }

   @NonNull
   @Override
   public synchronized Throwable getCause() {
      return this.cause;
   }
}
