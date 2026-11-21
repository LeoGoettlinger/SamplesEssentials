package de.oliver.cloud.annotations.parser;

import de.oliver.cloud.injection.ParameterInjectorRegistry;
import de.oliver.cloud.parser.ParserDescriptor;
import de.oliver.cloud.suggestion.SuggestionProvider;
import java.lang.reflect.Method;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@FunctionalInterface
@API(
   status = Status.EXPERIMENTAL
)
public interface MethodArgumentParserFactory<C> {
   @NonNull
   static <C> MethodArgumentParserFactory<C> defaultFactory() {
      return new MethodArgumentParserFactoryImpl<>();
   }

   @NonNull
   ParserDescriptor<C, ?> createArgumentParser(
      @NonNull SuggestionProvider<C> suggestionProvider,
      @NonNull Object instance,
      @NonNull Method method,
      @NonNull ParameterInjectorRegistry<C> injectorRegistry
   );
}
