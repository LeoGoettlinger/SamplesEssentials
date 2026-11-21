package org.jdbi.v3.core.extension;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Optional;

final class InstanceExtensionHandlerFactory implements ExtensionHandlerFactory {
   static final ExtensionHandlerFactory INSTANCE = new InstanceExtensionHandlerFactory();

   @Override
   public boolean accepts(Class<?> extensionType, Method method) {
      return Modifier.isAbstract(method.getModifiers()) || !extensionType.isInterface() && method.getDeclaringClass() != Object.class;
   }

   @Override
   public Optional<ExtensionHandler> createExtensionHandler(Class<?> extensionType, Method method) {
      try {
         return Optional.of(ExtensionHandler.createForMethod(method));
      } catch (IllegalAccessException var4) {
         throw new UnableToCreateExtensionException(var4, "Instance handler for %s couldn't unreflect %s", extensionType, method);
      }
   }
}
