package de.oliver.cloud.annotations.parser;

import de.oliver.cloud.annotations.method.AnnotatedMethodHandler;
import de.oliver.cloud.annotations.method.ParameterValue;
import de.oliver.cloud.context.CommandContext;
import de.oliver.cloud.context.CommandInput;
import de.oliver.cloud.injection.ParameterInjectorRegistry;
import de.oliver.cloud.parser.ArgumentParseResult;
import de.oliver.cloud.parser.ArgumentParser;
import de.oliver.cloud.suggestion.SuggestionProvider;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class MethodArgumentParser<C, T> extends AnnotatedMethodHandler<C> implements ArgumentParser<C, T> {
   private final SuggestionProvider<C> suggestionProvider;

   public MethodArgumentParser(
      @NonNull final SuggestionProvider<C> suggestionProvider,
      @NonNull final Object instance,
      @NonNull final Method method,
      @NonNull final ParameterInjectorRegistry<C> injectorRegistry
   ) {
      super(method, instance, injectorRegistry);
      this.suggestionProvider = suggestionProvider;
   }

   @NonNull
   @Override
   public ArgumentParseResult<T> parse(@NonNull final CommandContext<C> commandContext, @NonNull final CommandInput commandInput) {
      List<Object> arguments = this.createParameterValues(commandContext, this.parameters(), Collections.singletonList(commandInput))
         .stream()
         .map(ParameterValue::value)
         .collect(Collectors.toList());

      try {
         return ArgumentParseResult.success((T)this.methodHandle().invokeWithArguments(arguments));
      } catch (Throwable var5) {
         return ArgumentParseResult.failure(var5);
      }
   }

   @NonNull
   @Override
   public SuggestionProvider<C> suggestionProvider() {
      return this.suggestionProvider;
   }
}
