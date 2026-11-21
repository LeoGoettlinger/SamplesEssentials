package de.oliver.cloud.annotations.suggestion;

import de.oliver.cloud.annotations.method.AnnotatedMethodHandler;
import de.oliver.cloud.annotations.method.ParameterValue;
import de.oliver.cloud.context.CommandContext;
import de.oliver.cloud.context.CommandInput;
import de.oliver.cloud.injection.ParameterInjectorRegistry;
import de.oliver.cloud.suggestion.Suggestion;
import de.oliver.cloud.suggestion.SuggestionProvider;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class MethodSuggestionProvider<C> extends AnnotatedMethodHandler<C> implements SuggestionProvider<C> {
   public MethodSuggestionProvider(@NonNull final Object instance, @NonNull final Method method, @NonNull final ParameterInjectorRegistry<C> injectorRegistry) {
      super(method, instance, injectorRegistry);
   }

   @NonNull
   @Override
   public CompletableFuture<Iterable<Suggestion>> suggestionsFuture(@NonNull final CommandContext<C> context, @NonNull final CommandInput input) {
      try {
         List<Object> arguments = this.createParameterValues(context, this.parameters(), Arrays.asList(context, input, input.lastRemainingToken()))
            .stream()
            .map(ParameterValue::value)
            .collect(Collectors.toList());
         return mapSuggestions(this.methodHandle().invokeWithArguments(arguments));
      } catch (Throwable var4) {
         throw new RuntimeException(var4);
      }
   }

   @NonNull
   public static CompletableFuture<Iterable<Suggestion>> mapSuggestions(@NonNull final Object input) {
      return input instanceof CompletableFuture ? mapFuture((CompletableFuture)input) : CompletableFuture.completedFuture(mapCompleted(input));
   }

   @NonNull
   public static CompletableFuture<Iterable<Suggestion>> mapFuture(@NonNull final CompletableFuture future) {
      return future.thenApply(MethodSuggestionProvider::mapCompleted);
   }

   @NonNull
   public static Iterable<Suggestion> mapCompleted(@NonNull final Object input) {
      List<?> suggestions;
      if (input instanceof List) {
         suggestions = (List<?>)input;
      } else if (input instanceof Collection) {
         suggestions = new ArrayList((Collection)input);
      } else if (input instanceof Iterable) {
         suggestions = new ArrayList();

         for (Object suggestion : (Iterable)input) {
            ((List<Object>)suggestions).add(suggestion);
         }
      } else {
         if (!(input instanceof Stream)) {
            throw new IllegalArgumentException(String.format("Cannot handle suggestion input of type %s", input.getClass().getName()));
         }

         suggestions = ((Stream)input).collect(Collectors.toList());
      }

      if (suggestions.isEmpty()) {
         return Collections.emptyList();
      } else {
         Object suggestion = suggestions.get(0);
         if (suggestion instanceof Suggestion) {
            return (Iterable<Suggestion>)suggestions;
         } else if (suggestion instanceof String) {
            return suggestions.stream().map(Object::toString).map(Suggestion::suggestion).collect(Collectors.toList());
         } else {
            throw new IllegalArgumentException(String.format("Cannot handle suggestions of type: %s", suggestion.getClass().getName()));
         }
      }
   }
}
