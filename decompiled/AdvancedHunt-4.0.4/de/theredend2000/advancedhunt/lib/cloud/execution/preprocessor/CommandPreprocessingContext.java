package de.theredend2000.advancedhunt.lib.cloud.execution.preprocessor;

import de.theredend2000.advancedhunt.lib.cloud.context.CommandContext;
import de.theredend2000.advancedhunt.lib.cloud.context.CommandInput;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value.Immutable;

@API(
   status = API.Status.STABLE
)
@Immutable
public interface CommandPreprocessingContext<C> {
   @NonNull
   static <C> CommandPreprocessingContext<C> of(@NonNull final CommandContext<C> commandContext, @NonNull final CommandInput commandInput) {
      return CommandPreprocessingContextImpl.of(commandContext, commandInput);
   }

   @NonNull
   CommandContext<C> commandContext();

   @NonNull
   CommandInput commandInput();
}
