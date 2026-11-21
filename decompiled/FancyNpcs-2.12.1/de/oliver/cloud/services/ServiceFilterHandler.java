package de.oliver.cloud.services;

import de.oliver.cloud.services.type.Service;
import io.leangen.geantyref.TypeToken;
import java.util.function.Predicate;
import org.checkerframework.checker.nullness.qual.NonNull;

enum ServiceFilterHandler {
   INSTANCE;

   <Context> boolean passes(final ServiceRepository<Context, ?>.ServiceWrapper<? extends Service<Context, ?>> service, @NonNull final Context context) {
      if (!service.isDefaultImplementation()) {
         for (Predicate<Context> predicate : service.filters()) {
            try {
               if (!predicate.test(context)) {
                  return false;
               }
            } catch (Exception var6) {
               throw new PipelineException(
                  String.format("Failed to evaluate filter '%s' for '%s'", TypeToken.get(predicate.getClass()).getType().getTypeName(), service), var6
               );
            }
         }
      }

      return true;
   }
}
