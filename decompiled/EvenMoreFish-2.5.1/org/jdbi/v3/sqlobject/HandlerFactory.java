package org.jdbi.v3.sqlobject;

import java.lang.reflect.Method;
import java.util.Optional;
import org.jdbi.v3.core.extension.ExtensionHandler;
import org.jdbi.v3.core.extension.ExtensionHandlerFactory;

@Deprecated(
   since = "3.38.0",
   forRemoval = true
)
public interface HandlerFactory extends ExtensionHandlerFactory {
   Optional<Handler> buildHandler(Class<?> sqlObjectType, Method method);

   @Override
   default boolean accepts(Class<?> extensionType, Method method) {
      return true;
   }

   @Override
   default Optional<ExtensionHandler> createExtensionHandler(Class<?> extensionType, Method method) {
      return this.buildHandler(extensionType, method).map(h -> (ExtensionHandler)h);
   }
}
