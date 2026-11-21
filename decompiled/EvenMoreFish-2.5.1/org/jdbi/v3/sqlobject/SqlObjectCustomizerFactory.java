package org.jdbi.v3.sqlobject;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;
import org.jdbi.v3.core.config.ConfigCustomizer;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.extension.ConfigCustomizerFactory;
import org.jdbi.v3.core.internal.JdbiClassUtils;
import org.jdbi.v3.sqlobject.config.Configurer;
import org.jdbi.v3.sqlobject.config.ConfiguringAnnotation;

class SqlObjectCustomizerFactory implements ConfigCustomizerFactory {
   static final ConfigCustomizerFactory FACTORY = new SqlObjectCustomizerFactory();

   @Override
   public Collection<ConfigCustomizer> forExtensionType(Class<?> sqlObjectType) {
      SqlObjectCustomizerFactory.ConfigurerMethod forType = (configurer, config, annotation) -> configurer.configureForType(config, annotation, sqlObjectType);
      return buildConfigCustomizer(Stream.concat(JdbiClassUtils.superTypes(sqlObjectType), Stream.of(sqlObjectType)), forType);
   }

   @Override
   public Collection<ConfigCustomizer> forExtensionMethod(Class<?> sqlObjectType, Method method) {
      SqlObjectCustomizerFactory.ConfigurerMethod forMethod = (configurer, config, annotation) -> configurer.configureForMethod(
         config, annotation, sqlObjectType, method
      );
      return buildConfigCustomizer(Stream.of(method), forMethod);
   }

   private static Collection<ConfigCustomizer> buildConfigCustomizer(Stream<AnnotatedElement> elements, SqlObjectCustomizerFactory.ConfigurerMethod consumer) {
      return elements.<Annotation>flatMap(ae -> Arrays.stream(ae.getAnnotations()))
         .filter(a -> a.annotationType().isAnnotationPresent(ConfiguringAnnotation.class))
         .map(a -> {
            ConfiguringAnnotation meta = a.annotationType().getAnnotation(ConfiguringAnnotation.class);
            Configurer configurer = JdbiClassUtils.checkedCreateInstance(meta.value());
            return config -> consumer.configure(configurer, config, a);
         })
         .toList();
   }

   private interface ConfigurerMethod {
      void configure(Configurer configurer, ConfigRegistry config, Annotation annotation);
   }
}
