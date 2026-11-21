package de.oliver.cloud.execution;

import de.oliver.cloud.context.CommandContext;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = Status.INTERNAL
)
final class NullCommandExecutionHandler<C> implements CommandExecutionHandler<C> {
   static final CommandExecutionHandler<?> INSTANCE = new NullCommandExecutionHandler();

   @Override
   public void execute(@NonNull final CommandContext<C> commandContext) {
   }
}
