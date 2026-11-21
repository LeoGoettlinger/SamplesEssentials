package vrx.economy.vRXClans.libs.cloud.services;

import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.NonNull;
import vrx.economy.vRXClans.libs.cloud.services.type.Service;

public final class ServicePump<Context> {
   private final ServicePipeline servicePipeline;
   private final Context context;

   ServicePump(@NonNull final ServicePipeline servicePipeline, @NonNull final Context context) {
      this.servicePipeline = servicePipeline;
      this.context = context;
   }

   @NonNull
   public <Result> ServiceSpigot<Context, Result> through(@NonNull final TypeToken<? extends Service<Context, Result>> type) {
      return new ServiceSpigot<>(this.servicePipeline, this.context, type);
   }

   @NonNull
   public <Result> ServiceSpigot<Context, Result> through(@NonNull final Class<? extends Service<Context, Result>> clazz) {
      return this.through(TypeToken.get(clazz));
   }
}
