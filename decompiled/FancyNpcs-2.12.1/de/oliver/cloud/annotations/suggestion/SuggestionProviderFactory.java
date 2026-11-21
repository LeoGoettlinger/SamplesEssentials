package de.oliver.cloud.annotations.suggestion;

import de.oliver.cloud.injection.ParameterInjectorRegistry;
import de.oliver.cloud.suggestion.SuggestionProvider;
import java.lang.reflect.Method;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@FunctionalInterface
@API(
   status = Status.STABLE
)
public interface SuggestionProviderFactory<C> {
   @NonNull
   static <C> SuggestionProviderFactory<C> defaultFactory() {
      return MethodSuggestionProvider::new;
   }

   @NonNull
   SuggestionProvider<C> createSuggestionProvider(@NonNull Object instance, @NonNull Method method, @NonNull ParameterInjectorRegistry<C> injectorRegistry);
}
