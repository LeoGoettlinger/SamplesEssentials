package org.jdbi.v3.core.extension;

import java.lang.reflect.Method;
import java.util.Optional;

final class FilteringExtensionHandlerFactory implements ExtensionHandlerFactory {
   private final ExtensionHandlerFactory delegate;

   static ExtensionHandlerFactory forDelegate(ExtensionHandlerFactory delegate) {
      return new FilteringExtensionHandlerFactory(delegate);
   }

   private FilteringExtensionHandlerFactory(ExtensionHandlerFactory delegate) {
      this.delegate = delegate;
   }

   @Override
   public boolean accepts(Class<?> extensionType, Method method) {
      return method != null && !method.isSynthetic() && !method.isBridge() ? this.delegate.accepts(extensionType, method) : false;
   }

   @Override
   public Optional<ExtensionHandler> createExtensionHandler(Class<?> extensionType, Method method) {
      return this.delegate.createExtensionHandler(extensionType, method);
   }
}
