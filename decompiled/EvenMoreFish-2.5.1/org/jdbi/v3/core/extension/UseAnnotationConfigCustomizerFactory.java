package org.jdbi.v3.core.extension;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;
import org.jdbi.v3.core.config.ConfigCustomizer;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.extension.annotation.UseExtensionConfigurer;
import org.jdbi.v3.core.internal.JdbiClassUtils;

final class UseAnnotationConfigCustomizerFactory implements ConfigCustomizerFactory {
   private static final Class<?>[] EXTENSION_CONFIGURER_TYPES = new Class[]{Annotation.class, Class.class, Method.class};
   static final ConfigCustomizerFactory INSTANCE = new UseAnnotationConfigCustomizerFactory();

   @Override
   public Collection<ConfigCustomizer> forExtensionType(Class<?> extensionType) {
      UseAnnotationConfigCustomizerFactory.ConfigurerMethod forType = (configurer, config, annotation) -> configurer.configureForType(
         config, annotation, extensionType
      );
      return buildConfigCustomizer(extensionType, null, Stream.concat(JdbiClassUtils.superTypes(extensionType), Stream.of(extensionType)), forType);
   }

   @Override
   public Collection<ConfigCustomizer> forExtensionMethod(Class<?> extensionType, Method method) {
      UseAnnotationConfigCustomizerFactory.ConfigurerMethod forMethod = (configurer, config, annotation) -> configurer.configureForMethod(
         config, annotation, extensionType, method
      );
      return buildConfigCustomizer(extensionType, method, Stream.of(method), forMethod);
   }

   private static Collection<ConfigCustomizer> buildConfigCustomizer(
      Class<?> extensionType, Method method, Stream<AnnotatedElement> elements, UseAnnotationConfigCustomizerFactory.ConfigurerMethod consumer
   ) {
      return elements.<Annotation>flatMap(ae -> Arrays.stream(ae.getAnnotations()))
         .filter(a -> a.annotationType().isAnnotationPresent(UseExtensionConfigurer.class))
         .map(
            a -> {
               UseExtensionConfigurer meta = a.annotationType().getAnnotation(UseExtensionConfigurer.class);
               Class<? extends ExtensionConfigurer> klass = meta.value();
               ExtensionConfigurer configurer = JdbiClassUtils.findConstructorAndCreateInstance(
                  klass, EXTENSION_CONFIGURER_TYPES, handle -> (Object)handle.invokeExact((Annotation)a, (Class)extensionType, (Method)method)
               );
               return config -> consumer.configure(configurer, config, a);
            }
         )
         .toList();
   }

   @FunctionalInterface
   private interface ConfigurerMethod {
      void configure(ExtensionConfigurer configurer, ConfigRegistry config, Annotation annotation);
   }
}
