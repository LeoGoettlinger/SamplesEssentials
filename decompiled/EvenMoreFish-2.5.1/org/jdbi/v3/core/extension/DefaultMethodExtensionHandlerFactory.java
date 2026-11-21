package org.jdbi.v3.core.extension;

import java.lang.reflect.Method;
import java.util.Optional;

final class DefaultMethodExtensionHandlerFactory implements ExtensionHandlerFactory {
   static final ExtensionHandlerFactory INSTANCE = new DefaultMethodExtensionHandlerFactory();

   @Override
   public boolean accepts(Class<?> extensionType, Method method) {
      return extensionType.isInterface() && method.isDefault();
   }

   @Override
   public Optional<ExtensionHandler> createExtensionHandler(Class<?> extensionType, Method method) {
      try {
         return Optional.of(ExtensionHandler.createForSpecialMethod(method));
      } catch (IllegalAccessException var4) {
         throw new UnableToCreateExtensionException(var4, "Default method handler for %s couldn't unreflect %s", extensionType, method);
      }
   }
}
