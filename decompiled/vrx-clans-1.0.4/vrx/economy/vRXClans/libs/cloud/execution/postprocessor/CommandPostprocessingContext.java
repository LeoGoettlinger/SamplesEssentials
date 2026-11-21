package vrx.economy.vRXClans.libs.cloud.execution.postprocessor;

import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value.Immutable;
import vrx.economy.vRXClans.libs.cloud.Command;
import vrx.economy.vRXClans.libs.cloud.context.CommandContext;

@API(
   status = API.Status.STABLE
)
@Immutable
public interface CommandPostprocessingContext<C> {
   @NonNull
   static <C> CommandPostprocessingContext<C> of(@NonNull final CommandContext<C> commandContext, @NonNull final Command<C> command) {
      return CommandPostprocessingContextImpl.of(commandContext, command);
   }

   @NonNull
   CommandContext<C> commandContext();

   @NonNull
   Command<C> command();
}
