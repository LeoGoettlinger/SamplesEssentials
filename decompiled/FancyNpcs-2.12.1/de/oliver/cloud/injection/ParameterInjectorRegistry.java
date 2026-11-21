package de.oliver.cloud.injection;

import de.oliver.cloud.context.CommandContext;
import de.oliver.cloud.exception.InjectionException;
import de.oliver.cloud.services.ServicePipeline;
import de.oliver.cloud.type.tuple.Pair;
import de.oliver.cloud.util.annotation.AnnotationAccessor;
import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.geantyref.TypeToken;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.common.returnsreceiver.qual.This;

@API(
   status = Status.STABLE
)
public final class ParameterInjectorRegistry<C> implements InjectionService<C> {
   private final List<Pair<Predicate<TypeToken<?>>, ParameterInjector<C, ?>>> injectors = new ArrayList<>();
   private final ServicePipeline servicePipeline = ServicePipeline.builder().build();

   public ParameterInjectorRegistry() {
      this.servicePipeline.registerServiceType(new TypeToken<InjectionService<C>>() {}, this);
   }

   @This
   @NonNull
   public synchronized <T> ParameterInjectorRegistry<C> registerInjector(@NonNull final Class<T> clazz, @NonNull final ParameterInjector<C, T> injector) {
      return this.registerInjector(TypeToken.get(clazz), injector);
   }

   @API(
      status = Status.STABLE
   )
   @This
   @NonNull
   public synchronized <T> ParameterInjectorRegistry<C> registerInjector(@NonNull final TypeToken<T> type, @NonNull final ParameterInjector<C, T> injector) {
      return this.registerInjector(cl -> GenericTypeReflector.isSuperType(cl.getType(), type.getType()), injector);
   }

   @API(
      status = Status.STABLE
   )
   @This
   @NonNull
   public synchronized <T> ParameterInjectorRegistry<C> registerInjector(
      @NonNull final Predicate<TypeToken<?>> predicate, @NonNull final ParameterInjector<C, T> injector
   ) {
      this.injectors.add(Pair.of(predicate, injector));
      return this;
   }

   @Nullable
   public Object handle(@NonNull final InjectionRequest<C> request) {
      for (ParameterInjector<C, ?> injector : this.injectors(request.injectedType())) {
         Object value = injector.create(request.commandContext(), request.annotationAccessor());
         if (value != null) {
            return value;
         }
      }

      return null;
   }

   @API(
      status = Status.STABLE
   )
   @NonNull
   public <T> Optional<T> getInjectable(
      @NonNull final Class<T> clazz, @NonNull final CommandContext<C> context, @NonNull final AnnotationAccessor annotationAccessor
   ) {
      return this.getInjectable(TypeToken.get(clazz), context, annotationAccessor);
   }

   @API(
      status = Status.STABLE
   )
   @NonNull
   public <T> Optional<T> getInjectable(
      @NonNull final TypeToken<T> type, @NonNull final CommandContext<C> context, @NonNull final AnnotationAccessor annotationAccessor
   ) {
      InjectionRequest<C> request = InjectionRequest.of(context, type, annotationAccessor);

      try {
         Object rawResult = this.servicePipeline.pump(request).through(new TypeToken<InjectionService<C>>() {}).complete();
         if (!request.injectedClass().isInstance(rawResult)) {
            throw new IllegalStateException(
               String.format("Injector returned type %s which is not an instance of %s", rawResult.getClass().getName(), request.injectedClass().getName())
            );
         } else {
            return Optional.of((T)rawResult);
         }
      } catch (IllegalStateException var7) {
         return Optional.empty();
      } catch (InjectionException var8) {
         throw var8;
      } catch (Exception var9) {
         throw new InjectionException(String.format("Failed to inject type %s", type.getType().getTypeName()), var9);
      }
   }

   @API(
      status = Status.STABLE
   )
   @This
   @NonNull
   public ParameterInjectorRegistry<C> registerInjectionService(final InjectionService<C> service) {
      this.servicePipeline.registerServiceImplementation(new TypeToken<InjectionService<C>>() {}, service, Collections.emptyList());
      return this;
   }

   @NonNull
   private synchronized <T> Collection<ParameterInjector<C, ?>> injectors(@NonNull final TypeToken<T> type) {
      return Collections.unmodifiableCollection(this.injectors.stream().filter(pair -> pair.first().test(type)).map(Pair::second).collect(Collectors.toList()));
   }
}
