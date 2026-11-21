package de.oliver.cloud.annotations.method;

import de.oliver.cloud.context.CommandContext;
import de.oliver.cloud.injection.ParameterInjectorRegistry;
import de.oliver.cloud.util.annotation.AnnotationAccessor;
import io.leangen.geantyref.TypeToken;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@API(
   status = Status.INTERNAL
)
public abstract class AnnotatedMethodHandler<C> {
   private final Parameter[] parameters;
   private final MethodHandle methodHandle;
   private final AnnotationAccessor annotationAccessor;
   private final ParameterInjectorRegistry<C> injectorRegistry;

   protected AnnotatedMethodHandler(@NonNull final Method method, @NonNull final Object instance, @NonNull final ParameterInjectorRegistry<C> injectorRegistry) {
      try {
         this.parameters = method.getParameters();
         if (!method.isAccessible()) {
            method.setAccessible(true);
         }

         this.methodHandle = MethodHandles.lookup().unreflect(method).bindTo(instance);
         this.annotationAccessor = AnnotationAccessor.of(method);
         this.injectorRegistry = injectorRegistry;
      } catch (Exception var5) {
         throw new AnnotatedMethodHandlerInitiationException(var5);
      }
   }

   @NonNull
   public Parameter[] parameters() {
      return this.parameters;
   }

   @NonNull
   public MethodHandle methodHandle() {
      return this.methodHandle;
   }

   @NonNull
   public AnnotationAccessor annotationAccessor() {
      return this.annotationAccessor;
   }

   @NonNull
   public ParameterInjectorRegistry<C> injectorRegistry() {
      return this.injectorRegistry;
   }

   @Nullable
   protected ParameterValue getParameterValue(@NonNull final Parameter parameter, @NonNull final CommandContext<C> context) {
      return null;
   }

   @Nullable
   protected ParameterValue getInjectedValue(@NonNull final Parameter parameter, @NonNull final CommandContext<C> context) {
      Optional<?> value = this.injectorRegistry
         .getInjectable(
            TypeToken.get(parameter.getParameterizedType()), context, AnnotationAccessor.of(AnnotationAccessor.of(parameter), this.annotationAccessor())
         );
      if (value.isPresent()) {
         return ParameterValue.of(parameter, value.get());
      } else {
         return parameter.getType() == String.class ? ParameterValue.of(parameter, parameter.getName()) : null;
      }
   }

   @NonNull
   public List<ParameterValue> createParameterValues(@NonNull final CommandContext<C> context) {
      return this.createParameterValues(context, this.parameters());
   }

   @NonNull
   public List<ParameterValue> createParameterValues(@NonNull final CommandContext<C> context, @NonNull final Parameter[] parameters) {
      return this.createParameterValues(context, parameters, Collections.emptyList());
   }

   @NonNull
   public List<ParameterValue> createParameterValues(
      @NonNull final CommandContext<C> context, @NonNull final Parameter[] parameters, @NonNull final Collection<Object> preDeterminedValues
   ) {
      List<ParameterValue> values = new ArrayList<>(parameters.length);

      label33:
      for (Parameter parameter : parameters) {
         for (Object preDeterminedValue : preDeterminedValues) {
            if (parameter.getType().isInstance(preDeterminedValue)) {
               values.add(ParameterValue.of(parameter, preDeterminedValue));
               continue label33;
            }
         }

         ParameterValue contextualValue = this.getParameterValue(parameter, context);
         if (contextualValue != null) {
            values.add(contextualValue);
         } else if (parameter.getType().isAssignableFrom(context.sender().getClass())) {
            values.add(ParameterValue.of(parameter, context.sender()));
         } else {
            ParameterValue injectedValue = this.getInjectedValue(parameter, context);
            if (injectedValue == null) {
               throw new IllegalArgumentException(
                  String.format(
                     "Could not create value for parameter '%s' of type '%s' in method '%s'",
                     parameter.getName(),
                     parameter.getType().getTypeName(),
                     this.methodHandle().toString()
                  )
               );
            }

            values.add(injectedValue);
         }
      }

      return Collections.unmodifiableList(values);
   }
}
