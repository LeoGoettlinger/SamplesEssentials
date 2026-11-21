package vrx.economy.vRXClans.libs.cloud.execution;

import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import vrx.economy.vRXClans.libs.cloud.context.CommandContext;

@API(
   status = API.Status.INTERNAL
)
final class NullCommandExecutionHandler<C> implements CommandExecutionHandler<C> {
   static final CommandExecutionHandler<?> INSTANCE = new NullCommandExecutionHandler();

   @Override
   public void execute(@NonNull final CommandContext<C> commandContext) {
   }
}
