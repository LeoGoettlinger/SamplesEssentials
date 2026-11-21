package de.oliver.cloud.services;

import de.oliver.cloud.services.annotation.ServiceImplementation;
import de.oliver.cloud.services.type.Service;
import io.leangen.geantyref.TypeToken;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.NonNull;

enum AnnotatedMethodServiceFactory {
   INSTANCE;

   @NonNull
   Map<? extends Service<?, ?>, TypeToken<? extends Service<?, ?>>> lookupServices(@NonNull final Object instance) throws Exception {
      Map<Service<?, ?>, TypeToken<? extends Service<?, ?>>> map = new HashMap<>();
      Class<?> clazz = instance.getClass();

      for (Method method : clazz.getDeclaredMethods()) {
         ServiceImplementation serviceImplementation = method.getAnnotation(ServiceImplementation.class);
         if (serviceImplementation != null) {
            if (method.getParameterCount() != 1) {
               throw new IllegalArgumentException(
                  String.format(
                     "Method '%s' in class '%s' has wrong parameter count. Expected 1, got %d",
                     method.getName(),
                     instance.getClass().getCanonicalName(),
                     method.getParameterCount()
                  )
               );
            }

            map.put(new AnnotatedMethodService(instance, method), TypeToken.get(serviceImplementation.value()));
         }
      }

      return map;
   }
}
