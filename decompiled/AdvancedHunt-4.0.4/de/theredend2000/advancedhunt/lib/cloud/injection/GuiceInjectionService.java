package de.theredend2000.advancedhunt.lib.cloud.injection;

import com.google.inject.BindingAnnotation;
import com.google.inject.ConfigurationException;
import com.google.inject.Injector;
import com.google.inject.Key;
import de.theredend2000.advancedhunt.lib.cloud.util.annotation.AnnotationAccessor;
import java.lang.annotation.Annotation;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@API(
   status = API.Status.STABLE
)
public final class GuiceInjectionService<C> implements InjectionService<C> {
   private final Injector injector;

   @NonNull
   private static <T> Key<T> createKey(@NonNull final Class<T> clazz, @NonNull final AnnotationAccessor annotationAccessor) {
      Annotation bindingAnnotation = annotationAccessor.annotations()
         .stream()
         .filter(annotation -> annotation.annotationType().isAnnotationPresent(BindingAnnotation.class))
         .findFirst()
         .orElse(null);
      return bindingAnnotation == null ? Key.get(clazz) : Key.get(clazz, bindingAnnotation);
   }

   private GuiceInjectionService(@NonNull final Injector injector) {
      this.injector = injector;
   }

   @NonNull
   public static <C> GuiceInjectionService<C> create(@NonNull final Injector injector) {
      return new GuiceInjectionService<>(injector);
   }

   @Nullable
   public Object handle(@NonNull final InjectionRequest<C> request) {
      try {
         return this.injector.getInstance(createKey(request.injectedClass(), request.annotationAccessor()));
      } catch (ConfigurationException var3) {
         return null;
      }
   }
}
