package de.theredend2000.advancedhunt.lib.cloud.exception.handling;

import de.theredend2000.advancedhunt.lib.cloud.context.CommandContext;
import io.leangen.geantyref.TypeToken;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionException;
import java.util.function.Predicate;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.common.returnsreceiver.qual.This;

@API(
   status = API.Status.STABLE
)
public final class ExceptionController<C> {
   private final ExceptionContextFactory<C> exceptionContextFactory = new ExceptionContextFactory<>(this);
   private final Map<Type, LinkedList<ExceptionHandlerRegistration<C, ?>>> registrations = new HashMap<>();

   @NonNull
   public static Throwable unwrapCompletionException(@NonNull final Throwable throwable) {
      return throwable instanceof CompletionException ? unwrapCompletionException(throwable.getCause()) : throwable;
   }

   public <T extends Throwable> void handleException(@NonNull final CommandContext<C> commandContext, @NonNull final T exception) throws Throwable {
      ExceptionContext<C, T> exceptionContext = this.exceptionContextFactory.createContext(commandContext, exception);

      for (Class<?> exceptionClass = exception.getClass(); exceptionClass != Object.class; exceptionClass = exceptionClass.getSuperclass()) {
         for (ExceptionHandlerRegistration<C, ?> registration : this.registrations(exceptionClass)) {
            if (((Predicate<T>)registration.exceptionFilter()).test(exception)) {
               try {
                  registration.exceptionHandler().handle(exceptionContext);
                  return;
               } catch (Throwable var9) {
                  if (!var9.equals(exception)) {
                     this.handleException(commandContext, var9);
                     return;
                  }
               }
            }
         }
      }

      throw exception;
   }

   @This
   @NonNull
   public synchronized <T extends Throwable> ExceptionController<C> register(@NonNull final ExceptionHandlerRegistration<C, ? extends T> registration) {
      this.registrations.computeIfAbsent(registration.exceptionType().getType(), t -> new LinkedList<>()).addFirst(registration);
      return this;
   }

   @This
   @NonNull
   public <T extends Throwable> ExceptionController<C> register(
      @NonNull final TypeToken<T> exceptionType, @NonNull final ExceptionHandlerRegistration.BuilderDecorator<C, T> decorator
   ) {
      return this.register(decorator.decorate(ExceptionHandlerRegistration.builder(exceptionType)).build());
   }

   @This
   @NonNull
   public <T extends Throwable> ExceptionController<C> register(
      @NonNull final Class<T> exceptionType, @NonNull final ExceptionHandlerRegistration.BuilderDecorator<C, T> decorator
   ) {
      return this.register(decorator.decorate(ExceptionHandlerRegistration.builder(TypeToken.get(exceptionType))).build());
   }

   @This
   @NonNull
   public <T extends Throwable> ExceptionController<C> registerHandler(
      @NonNull final TypeToken<T> exceptionType, @NonNull final ExceptionHandler<C, ? extends T> exceptionHandler
   ) {
      return this.register(ExceptionHandlerRegistration.of(exceptionType, exceptionHandler));
   }

   @This
   @NonNull
   public <T extends Throwable> ExceptionController<C> registerHandler(
      @NonNull final Class<T> exceptionType, @NonNull final ExceptionHandler<C, ? extends T> exceptionHandler
   ) {
      return this.register(ExceptionHandlerRegistration.of(TypeToken.get(exceptionType), exceptionHandler));
   }

   public void clearHandlers() {
      this.registrations.clear();
   }

   @NonNull
   private List<ExceptionHandlerRegistration<C, ?>> registrations(@NonNull final Type type) {
      return Collections.unmodifiableList(this.registrations.getOrDefault(type, new LinkedList<>()));
   }
}
