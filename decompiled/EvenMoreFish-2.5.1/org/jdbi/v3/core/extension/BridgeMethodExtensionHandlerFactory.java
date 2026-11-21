package org.jdbi.v3.core.extension;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

final class BridgeMethodExtensionHandlerFactory implements ExtensionHandlerFactory {
   static final ExtensionHandlerFactory INSTANCE = new BridgeMethodExtensionHandlerFactory();

   @Override
   public boolean accepts(Class<?> extensionType, Method method) {
      return method.isBridge();
   }

   @Override
   public Optional<ExtensionHandler> createExtensionHandler(Class<?> extensionType, Method method) {
      for (Method candidate : Stream.of(extensionType.getMethods())
         .filter(candidatex -> !candidatex.isBridge())
         .filter(candidatex -> Objects.equals(candidatex.getName(), method.getName()))
         .filter(candidatex -> candidatex.getParameterCount() == method.getParameterCount())
         .filter(candidatex -> {
            Class<?>[] candidateParamTypes = candidatex.getParameterTypes();
            Class<?>[] methodParamTypes = method.getParameterTypes();
            return IntStream.range(0, method.getParameterCount()).allMatch(i -> methodParamTypes[i].isAssignableFrom(candidateParamTypes[i]));
         })
         .toList()) {
         try {
            return Optional.of(ExtensionHandler.createForMethod(candidate));
         } catch (IllegalAccessException var7) {
         }
      }

      throw new UnableToCreateExtensionException("Could not create an extension handler for bridge method %s#%s.", extensionType, method);
   }
}
