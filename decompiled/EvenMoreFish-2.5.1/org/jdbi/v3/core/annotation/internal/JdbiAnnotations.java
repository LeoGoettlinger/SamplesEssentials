package org.jdbi.v3.core.annotation.internal;

import java.lang.reflect.AnnotatedElement;
import java.util.Optional;
import org.jdbi.v3.core.annotation.JdbiProperty;
import org.jdbi.v3.core.internal.UtilityClassException;
import org.jdbi.v3.core.mapper.reflect.internal.PojoProperties;

public final class JdbiAnnotations {
   private JdbiAnnotations() {
      throw new UtilityClassException();
   }

   public static boolean isMapped(AnnotatedElement element) {
      return isMapped(jdbiProperty(element));
   }

   public static boolean isBound(AnnotatedElement element) {
      return isBound(jdbiProperty(element));
   }

   public static boolean isMapped(PojoProperties.PojoProperty<?> property) {
      return isMapped(property.getAnnotation(JdbiProperty.class));
   }

   public static boolean isBound(PojoProperties.PojoProperty<?> property) {
      return isBound(property.getAnnotation(JdbiProperty.class));
   }

   private static boolean isBound(Optional<JdbiProperty> annotation) {
      return annotation.map(JdbiProperty::bind).orElse(true);
   }

   private static boolean isMapped(Optional<JdbiProperty> annotation) {
      return annotation.map(JdbiProperty::map).orElse(true);
   }

   private static Optional<JdbiProperty> jdbiProperty(AnnotatedElement element) {
      return Optional.ofNullable(element.getAnnotation(JdbiProperty.class));
   }
}
