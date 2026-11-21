package de.oliver.cloud.exception;

import de.oliver.cloud.component.CommandComponent;
import java.util.List;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = Status.STABLE
)
public class ArgumentParseException extends CommandParseException {
   private final Throwable cause;

   @API(
      status = Status.INTERNAL,
      consumers = {"de.oliver.cloud.*"}
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
