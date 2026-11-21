package de.oliver.cloud.execution.postprocessor;

import de.oliver.cloud.Command;
import de.oliver.cloud.context.CommandContext;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value.Immutable;

@API(
   status = Status.STABLE
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
