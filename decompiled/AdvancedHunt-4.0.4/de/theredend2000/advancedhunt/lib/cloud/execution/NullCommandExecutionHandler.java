package de.theredend2000.advancedhunt.lib.cloud.execution;

import de.theredend2000.advancedhunt.lib.cloud.context.CommandContext;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = API.Status.INTERNAL
)
final class NullCommandExecutionHandler<C> implements CommandExecutionHandler<C> {
   static final CommandExecutionHandler<?> INSTANCE = new NullCommandExecutionHandler();

   @Override
   public void execute(@NonNull final CommandContext<C> commandContext) {
   }
}
