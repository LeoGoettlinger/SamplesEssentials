package org.jdbi.v3.sqlobject;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.config.JdbiConfig;
import org.jdbi.v3.core.extension.Extensions;

@Deprecated(
   since = "3.38.0",
   forRemoval = true
)
public class Handlers implements JdbiConfig<Handlers> {
   private final List<HandlerFactory> factories;
   private ConfigRegistry registry;

   public Handlers() {
      this.factories = new CopyOnWriteArrayList<>();
   }

   private Handlers(Handlers that) {
      this.factories = new CopyOnWriteArrayList<>(that.factories);
   }

   @Override
   public void setRegistry(ConfigRegistry registry) {
      this.registry = registry;
   }

   List<HandlerFactory> getFactories() {
      return this.factories;
   }

   public Handlers register(HandlerFactory factory) {
      if (this.registry != null) {
         this.registry.get(Extensions.class).invalidateMetadataCache();
      }

      this.factories.add(0, factory);
      return this;
   }

   public Optional<Handler> findFor(Class<?> sqlObjectType, Method method) {
      return this.factories.stream().flatMap(factory -> factory.buildHandler(sqlObjectType, method).stream()).findFirst();
   }

   public Handlers createCopy() {
      return new Handlers(this);
   }
}
