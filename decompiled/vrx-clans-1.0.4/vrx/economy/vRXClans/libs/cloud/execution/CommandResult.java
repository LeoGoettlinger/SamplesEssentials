package vrx.economy.vRXClans.libs.cloud.execution;

import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value.Immutable;
import vrx.economy.vRXClans.libs.cloud.context.CommandContext;

@API(
   status = API.Status.STABLE
)
@Immutable
public interface CommandResult<C> {
   @NonNull
   static <C> CommandResult<C> of(@NonNull final CommandContext<C> context) {
      return CommandResultImpl.of(context);
   }

   @NonNull
   CommandContext<C> commandContext();
}
