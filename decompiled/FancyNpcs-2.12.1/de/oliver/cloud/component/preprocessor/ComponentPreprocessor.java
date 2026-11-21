package de.oliver.cloud.component.preprocessor;

import de.oliver.cloud.context.CommandContext;
import de.oliver.cloud.context.CommandInput;
import de.oliver.cloud.parser.ArgumentParseResult;
import java.util.function.BiFunction;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = Status.STABLE
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
