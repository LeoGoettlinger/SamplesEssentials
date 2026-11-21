package de.theredend2000.advancedhunt.lib.cloud.component.preprocessor;

import de.theredend2000.advancedhunt.lib.cloud.context.CommandContext;
import de.theredend2000.advancedhunt.lib.cloud.context.CommandInput;
import de.theredend2000.advancedhunt.lib.cloud.parser.ArgumentParseResult;
import java.util.function.BiFunction;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

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
