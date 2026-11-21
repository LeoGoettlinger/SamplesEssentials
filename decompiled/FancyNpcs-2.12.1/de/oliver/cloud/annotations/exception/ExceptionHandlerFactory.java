package de.oliver.cloud.annotations.exception;

import de.oliver.cloud.injection.ParameterInjectorRegistry;
import java.lang.reflect.Method;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@FunctionalInterface
@API(
   status = Status.STABLE
)
public interface ExceptionHandlerFactory<C> {
   @NonNull
   static <C> ExceptionHandlerFactory<C> defaultFactory() {
      return MethodExceptionHandler::new;
   }

   @NonNull
   de.oliver.cloud.exception.handling.ExceptionHandler<C, Throwable> createExceptionHandler(
      @NonNull Object instance, @NonNull Method method, @NonNull ParameterInjectorRegistry<C> injectorRegistry
   );
}
