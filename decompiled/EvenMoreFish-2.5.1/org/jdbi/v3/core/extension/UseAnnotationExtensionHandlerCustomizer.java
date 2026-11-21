package org.jdbi.v3.core.extension;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jdbi.v3.core.extension.annotation.ExtensionHandlerCustomizationOrder;
import org.jdbi.v3.core.extension.annotation.UseExtensionHandlerCustomizer;
import org.jdbi.v3.core.internal.JdbiClassUtils;

final class UseAnnotationExtensionHandlerCustomizer implements ExtensionHandlerCustomizer {
   private static final Class<?>[] EXTENSION_HANDLER_CUSTOMIZER_TYPES = new Class[]{Class.class, Method.class};
   static final ExtensionHandlerCustomizer INSTANCE = new UseAnnotationExtensionHandlerCustomizer();

   @Override
   public ExtensionHandler customize(ExtensionHandler delegate, Class<?> extensionType, Method method) {
      ExtensionHandler extensionHandler = delegate;
      List<Class<? extends Annotation>> annotationTypes = Stream.of(method, extensionType)
         .map(AnnotatedElement::getAnnotations)
         .flatMap(Arrays::stream)
         .map(Annotation::annotationType)
         .filter(type -> type.isAnnotationPresent(UseExtensionHandlerCustomizer.class))
         .collect(Collectors.toCollection(ArrayList::new));
      Stream.of(method, extensionType)
         .map(e -> e.getAnnotation(ExtensionHandlerCustomizationOrder.class))
         .filter(Objects::nonNull)
         .findFirst()
         .ifPresent(order -> annotationTypes.sort(this.createComparator(order).reversed()));

      for (ExtensionHandlerCustomizer customizer : annotationTypes.stream()
         .map(type -> type.getAnnotation(UseExtensionHandlerCustomizer.class))
         .map(UseExtensionHandlerCustomizer::value)
         .map(
            klass -> JdbiClassUtils.findConstructorAndCreateInstance(
               (Class<? extends ExtensionHandlerCustomizer>)klass,
               EXTENSION_HANDLER_CUSTOMIZER_TYPES,
               handle -> (Object)handle.invokeExact((Class)extensionType, (Method)method)
            )
         )
         .toList()) {
         extensionHandler = customizer.customize(extensionHandler, extensionType, method);
      }

      return extensionHandler;
   }

   private Comparator<Class<? extends Annotation>> createComparator(ExtensionHandlerCustomizationOrder order) {
      List<Class<? extends Annotation>> ordering = Arrays.asList(order.value());
      return Comparator.comparingInt(type -> {
         int index = ordering.indexOf(type);
         return index == -1 ? ordering.size() : index;
      });
   }
}
