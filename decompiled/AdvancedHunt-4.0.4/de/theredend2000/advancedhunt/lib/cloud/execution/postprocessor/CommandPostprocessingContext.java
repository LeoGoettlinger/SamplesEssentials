package de.theredend2000.advancedhunt.lib.cloud.execution.postprocessor;

import de.theredend2000.advancedhunt.lib.cloud.Command;
import de.theredend2000.advancedhunt.lib.cloud.context.CommandContext;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value.Immutable;

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
