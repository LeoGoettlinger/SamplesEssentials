package de.oliver.cloud.execution;

import de.oliver.cloud.context.CommandContext;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value.Immutable;

@API(
   status = Status.STABLE
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
