package de.oliver.cloud.annotations.parser;

import de.oliver.cloud.injection.ParameterInjectorRegistry;
import de.oliver.cloud.parser.ParserDescriptor;
import de.oliver.cloud.suggestion.SuggestionProvider;
import io.leangen.geantyref.TypeToken;
import java.lang.reflect.Method;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class MethodArgumentParserFactoryImpl<C> implements MethodArgumentParserFactory<C> {
   @NonNull
   @Override
   public ParserDescriptor<C, ?> createArgumentParser(
      @NonNull final SuggestionProvider<C> suggestionProvider,
      @NonNull final Object instance,
      @NonNull final Method method,
      @NonNull final ParameterInjectorRegistry<C> injectorRegistry
   ) {
      return ParserDescriptor.of(
         new MethodArgumentParser<>(suggestionProvider, instance, method, injectorRegistry), TypeToken.get(method.getGenericReturnType())
      );
   }
}
