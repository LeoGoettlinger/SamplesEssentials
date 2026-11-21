package org.jdbi.v3.sqlobject;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.config.JdbiConfig;
import org.jdbi.v3.core.extension.ExtensionHandler;
import org.jdbi.v3.core.extension.Extensions;

@Deprecated(
   since = "3.38.0",
   forRemoval = true
)
public class HandlerDecorators implements JdbiConfig<HandlerDecorators> {
   private final List<HandlerDecorator> decorators;
   private ConfigRegistry registry;

   public HandlerDecorators() {
      this.decorators = new CopyOnWriteArrayList<>();
      this.register(new SqlMethodAnnotatedHandlerDecorator());
   }

   private HandlerDecorators(HandlerDecorators that) {
      this.decorators = new CopyOnWriteArrayList<>(that.decorators);
   }

   @Override
   public void setRegistry(ConfigRegistry registry) {
      this.registry = registry;
   }

   public HandlerDecorators register(HandlerDecorator decorator) {
      if (this.registry != null) {
         this.registry.get(Extensions.class).invalidateMetadataCache();
      }

      this.decorators.add(decorator);
      return this;
   }

   public Handler applyDecorators(Handler base, Class<?> sqlObjectType, Method method) {
      Handler handler = base;

      for (HandlerDecorator decorator : this.decorators) {
         handler = decorator.decorateHandler(handler, sqlObjectType, method);
      }

      return handler;
   }

   ExtensionHandler customize(ExtensionHandler base, Class<?> sqlObjectType, Method method) {
      ExtensionHandler handler = base;

      for (HandlerDecorator decorator : this.decorators) {
         handler = decorator.customize(handler, sqlObjectType, method);
      }

      return handler;
   }

   public HandlerDecorators createCopy() {
      return new HandlerDecorators(this);
   }
}
