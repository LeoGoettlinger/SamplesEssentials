package de.theredend2000.advancedhunt.lib.cloud.exception;

import de.theredend2000.advancedhunt.lib.cloud.context.CommandContext;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@API(
   status = API.Status.STABLE
)
public class CommandExecutionException extends IllegalArgumentException {
   private final CommandContext<?> commandContext;

   @API(
      status = API.Status.INTERNAL,
      consumers = {"de.theredend2000.advancedhunt.lib.cloud.*"}
   )
   public CommandExecutionException(@NonNull final Throwable cause) {
      this(cause, null);
   }

   @API(
      status = API.Status.INTERNAL,
      consumers = {"de.theredend2000.advancedhunt.lib.cloud.*"}
   )
   public CommandExecutionException(@NonNull final Throwable cause, @Nullable final CommandContext<?> commandContext) {
      super(cause);
      this.commandContext = commandContext;
   }

   @API(
      status = API.Status.STABLE
   )
   @Nullable
   public CommandContext<?> context() {
      return this.commandContext;
   }
}
