package vrx.economy.vRXClans.libs.cloud.execution.preprocessor;

import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value.Immutable;
import vrx.economy.vRXClans.libs.cloud.context.CommandContext;
import vrx.economy.vRXClans.libs.cloud.context.CommandInput;

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
