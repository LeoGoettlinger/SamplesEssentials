package org.jdbi.v3.core.extension;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.jdbi.v3.core.extension.annotation.UseExtensionHandler;
import org.jdbi.v3.core.internal.JdbiClassUtils;

final class UseAnnotationExtensionHandlerFactory implements ExtensionHandlerFactory {
   private static final Class<?>[] EXTENSION_HANDLER_TYPES = new Class[]{Class.class, Method.class};
   static final ExtensionHandlerFactory INSTANCE = new UseAnnotationExtensionHandlerFactory();

   @Override
   public boolean accepts(Class<?> extensionType, Method method) {
      return !method.isBridge() && !findAnnotations(method).isEmpty();
   }

   private static boolean matchAnnotation(Annotation a) {
      return a.annotationType().isAnnotationPresent(UseExtensionHandler.class);
   }

   static List<? extends Class<?>> findAnnotations(Method method) {
      return Stream.of(method.getAnnotations()).filter(UseAnnotationExtensionHandlerFactory::matchAnnotation).map(Annotation::annotationType).toList();
   }

   @Override
   public Optional<ExtensionHandler> createExtensionHandler(Class<?> extensionType, Method method) {
      List<? extends Class<?>> extensionAnnotations = findAnnotations(method);
      if (extensionAnnotations.size() > 1) {
         throw new IllegalStateException(
            String.format("Mutually exclusive extension annotations on method %s.%s: %s", extensionType.getName(), method.getName(), extensionAnnotations)
         );
      } else if (method.isDefault() && !method.isSynthetic()) {
         throw new IllegalStateException(
            String.format(
               "Default method %s.%s has @%s annotation. Extension type methods may be default, or have a @UseExtensionHandler annotation, but not both.",
               extensionType.getSimpleName(),
               method.getName(),
               extensionAnnotations.get(0).getSimpleName()
            )
         );
      } else {
         return extensionAnnotations.stream()
            .map(type -> type.getAnnotation(UseExtensionHandler.class))
            .map(UseExtensionHandler::value)
            .map(
               klass -> JdbiClassUtils.findConstructorAndCreateInstance(
                  (Class<? extends ExtensionHandler>)klass, EXTENSION_HANDLER_TYPES, handle -> (Object)handle.invokeExact((Class)extensionType, (Method)method)
               )
            )
            .findFirst();
      }
   }
}
