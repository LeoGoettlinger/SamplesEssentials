package vrx.economy.vRXClans.libs.cloud.component.preprocessor;

import java.util.function.BiFunction;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import vrx.economy.vRXClans.libs.cloud.context.CommandContext;
import vrx.economy.vRXClans.libs.cloud.context.CommandInput;
import vrx.economy.vRXClans.libs.cloud.parser.ArgumentParseResult;

@API(
   status = API.Status.STABLE
)
@FunctionalInterface
public interface ComponentPreprocessor<C> {
   @NonNull
   static <C> ComponentPreprocessor<C> wrap(@NonNull final BiFunction<CommandContext<C>, CommandInput, ArgumentParseResult<Boolean>> function) {
      return function::apply;
   }

   @NonNull
   ArgumentParseResult<Boolean> preprocess(@NonNull CommandContext<C> context, @NonNull CommandInput commandInput);
}
