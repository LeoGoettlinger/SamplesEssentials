package org.jdbi.v3.core.extension;

import java.lang.reflect.Method;
import java.util.Optional;
import org.jdbi.v3.meta.Alpha;

@Alpha
public interface ExtensionHandlerFactory {
   boolean accepts(Class<?> extensionType, Method method);

   Optional<ExtensionHandler> createExtensionHandler(Class<?> extensionType, Method method);
}
