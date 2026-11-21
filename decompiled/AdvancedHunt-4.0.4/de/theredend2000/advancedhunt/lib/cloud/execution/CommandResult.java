package de.theredend2000.advancedhunt.lib.cloud.execution;

import de.theredend2000.advancedhunt.lib.cloud.context.CommandContext;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value.Immutable;

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
