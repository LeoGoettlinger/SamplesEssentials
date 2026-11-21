package org.jdbi.v3.sqlobject;

import java.lang.reflect.Method;
import org.jdbi.v3.core.extension.ExtensionHandler;
import org.jdbi.v3.core.extension.ExtensionHandlerCustomizer;

@Deprecated(
   since = "3.38.0",
   forRemoval = true
)
@FunctionalInterface
public interface HandlerDecorator extends ExtensionHandlerCustomizer {
   Handler decorateHandler(Handler base, Class<?> sqlObjectType, Method method);

   @Override
   default ExtensionHandler customize(ExtensionHandler defaultHandler, Class<?> extensionType, Method method) {
      ExtensionHandler handler = defaultHandler;
      if (defaultHandler instanceof Handler) {
         handler = this.decorateHandler((Handler)defaultHandler, extensionType, method);
      }

      return handler;
   }
}
