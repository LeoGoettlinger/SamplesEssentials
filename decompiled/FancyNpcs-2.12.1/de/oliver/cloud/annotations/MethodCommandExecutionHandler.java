package de.oliver.cloud.annotations;

import de.oliver.cloud.annotations.descriptor.ArgumentDescriptor;
import de.oliver.cloud.annotations.descriptor.FlagDescriptor;
import de.oliver.cloud.annotations.method.AnnotatedMethodHandler;
import de.oliver.cloud.annotations.method.ParameterValue;
import de.oliver.cloud.component.CommandComponent;
import de.oliver.cloud.context.CommandContext;
import de.oliver.cloud.exception.CommandExecutionException;
import de.oliver.cloud.execution.CommandExecutionHandler;
import de.oliver.cloud.injection.ParameterInjectorRegistry;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class MethodCommandExecutionHandler<C> extends AnnotatedMethodHandler<C> implements CommandExecutionHandler.FutureCommandExecutionHandler<C> {
   private final MethodCommandExecutionHandler.CommandMethodContext<C> context;
   private final AnnotationParser<C> annotationParser;
   private final boolean returnsFuture;

   public MethodCommandExecutionHandler(@NonNull final MethodCommandExecutionHandler.CommandMethodContext<C> context) {
      super(context.method, context.instance, context.annotationParser.manager().parameterInjectorRegistry());
      this.context = context;
      this.annotationParser = context.annotationParser();
      this.returnsFuture = context.method().getReturnType().equals(CompletableFuture.class);
   }

   @NonNull
   public MethodCommandExecutionHandler.CommandMethodContext<C> context() {
      return this.context;
   }

   @Override
   public CompletableFuture<Void> executeFuture(@NonNull final CommandContext<C> commandContext) {
      try {
         Object result = this.methodHandle()
            .invokeWithArguments(this.createParameterValues(commandContext).stream().map(ParameterValue::value).collect(Collectors.toList()));
         return this.returnsFuture ? (CompletableFuture)result : CompletableFuture.completedFuture(null);
      } catch (Error var3) {
         throw var3;
      } catch (Throwable var4) {
         throw new CommandExecutionException(var4, commandContext);
      }
   }

   @Nullable
   @Override
   protected final ParameterValue getParameterValue(@NonNull final Parameter parameter, @NonNull final CommandContext<C> context) {
      ArgumentDescriptor argumentDescriptor = this.context
         .argumentDescriptors
         .stream()
         .filter(descriptor -> descriptor.parameter().equals(parameter))
         .findFirst()
         .orElse(null);
      if (argumentDescriptor != null) {
         String argumentName;
         if (argumentDescriptor.name().equals("__INFERRED_ARGUMENT_NAME__")) {
            argumentName = parameter.getName();
         } else {
            argumentName = this.annotationParser.processString(argumentDescriptor.name());
         }

         CommandComponent<C> commandComponent = this.context.commandComponents.get(argumentName);
         if (commandComponent.required()) {
            return ParameterValue.of(parameter, context.get(argumentName), argumentDescriptor);
         } else {
            Object optional = context.optional(argumentName).orElse(null);
            return ParameterValue.of(parameter, optional, argumentDescriptor);
         }
      } else {
         FlagDescriptor flagDescriptor = this.context
            .flagDescriptors
            .stream()
            .filter(descriptor -> descriptor.parameter().equals(parameter))
            .findFirst()
            .orElse(null);
         if (flagDescriptor != null) {
            if (parameter.getType().equals(boolean.class)) {
               return ParameterValue.of(parameter, context.flags().isPresent(flagDescriptor.name()), flagDescriptor);
            } else {
               return flagDescriptor.repeatable() && parameter.getType().isAssignableFrom(List.class)
                  ? ParameterValue.of(parameter, context.flags().getAll(flagDescriptor.name()), flagDescriptor)
                  : ParameterValue.of(parameter, context.flags().getValue(flagDescriptor.name(), null), flagDescriptor);
            }
         } else {
            return null;
         }
      }
   }

   public static class CommandMethodContext<C> {
      private final Object instance;
      private final Map<String, CommandComponent<C>> commandComponents;
      private final Method method;
      private final ParameterInjectorRegistry<C> injectorRegistry;
      private final AnnotationParser<C> annotationParser;
      private final Collection<ArgumentDescriptor> argumentDescriptors;
      private final Collection<FlagDescriptor> flagDescriptors;

      CommandMethodContext(
         @NonNull final Object instance,
         @NonNull final Map<String, CommandComponent<C>> commandComponents,
         @NonNull final Collection<ArgumentDescriptor> argumentDescriptors,
         @NonNull final Collection<FlagDescriptor> flagDescriptors,
         @NonNull final Method method,
         @NonNull final AnnotationParser<C> annotationParser
      ) {
         this.instance = instance;
         this.commandComponents = commandComponents;
         this.method = method;
         this.method.setAccessible(true);
         this.injectorRegistry = annotationParser.manager().parameterInjectorRegistry();
         this.annotationParser = annotationParser;
         this.argumentDescriptors = argumentDescriptors;
         this.flagDescriptors = flagDescriptors;
      }

      @NonNull
      public Object instance() {
         return this.instance;
      }

      @NonNull
      public final Method method() {
         return this.method;
      }

      @NonNull
      public final Map<String, CommandComponent<C>> commandComponents() {
         return this.commandComponents;
      }

      @NonNull
      public final ParameterInjectorRegistry<C> injectorRegistry() {
         return this.injectorRegistry;
      }

      @NonNull
      public AnnotationParser<C> annotationParser() {
         return this.annotationParser;
      }

      @API(
         status = Status.STABLE
      )
      @NonNull
      public Collection<ArgumentDescriptor> argumentDescriptors() {
         return Collections.unmodifiableCollection(this.argumentDescriptors);
      }

      @API(
         status = Status.STABLE
      )
      @NonNull
      public Collection<FlagDescriptor> flagDescriptors() {
         return Collections.unmodifiableCollection(this.flagDescriptors);
      }
   }
}
