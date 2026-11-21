package vrx.economy.vRXClans.libs.cloud.exception;

import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import vrx.economy.vRXClans.libs.cloud.context.CommandContext;

@API(
   status = API.Status.STABLE
)
public class CommandExecutionException extends IllegalArgumentException {
   private final CommandContext<?> commandContext;

   @API(
      status = API.Status.INTERNAL,
      consumers = {"vrx.economy.vRXClans.libs.cloud.*"}
   )
   public CommandExecutionException(@NonNull final Throwable cause) {
      this(cause, null);
   }

   @API(
      status = API.Status.INTERNAL,
      consumers = {"vrx.economy.vRXClans.libs.cloud.*"}
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
