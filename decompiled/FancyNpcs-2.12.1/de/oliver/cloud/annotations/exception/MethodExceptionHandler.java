package de.oliver.cloud.annotations.exception;

import de.oliver.cloud.annotations.method.AnnotatedMethodHandler;
import de.oliver.cloud.annotations.method.ParameterValue;
import de.oliver.cloud.exception.handling.ExceptionContext;
import de.oliver.cloud.injection.ParameterInjectorRegistry;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = Status.STABLE
)
public final class MethodExceptionHandler<C> extends AnnotatedMethodHandler<C> implements de.oliver.cloud.exception.handling.ExceptionHandler<C, Throwable> {
   public MethodExceptionHandler(@NonNull final Object instance, @NonNull final Method method, @NonNull final ParameterInjectorRegistry<C> injectorRegistry) {
      super(method, instance, injectorRegistry);
   }

   @Override
   public void handle(@NonNull final ExceptionContext<C, Throwable> context) throws Throwable {
      List<Object> arguments = this.createParameterValues(context.context(), this.parameters(), Arrays.asList(context, context.exception()))
         .stream()
         .map(ParameterValue::value)
         .collect(Collectors.toList());
      this.methodHandle().invokeWithArguments(arguments);
   }
}
