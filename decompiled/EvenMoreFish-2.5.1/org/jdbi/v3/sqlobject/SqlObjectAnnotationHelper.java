package org.jdbi.v3.sqlobject;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import org.jdbi.v3.core.extension.annotation.UseExtensionHandler;
import org.jdbi.v3.core.internal.UtilityClassException;

final class SqlObjectAnnotationHelper {
   private SqlObjectAnnotationHelper() {
      throw new UtilityClassException();
   }

   static boolean matchSqlAnnotations(Annotation a) {
      return matchNewAnnotation(a) || matchOldAnnotation(a);
   }

   static List<Class<?>> findSqlMethodAnnotations(Method method) {
      return findOldAnnotations(method).toList();
   }

   private static boolean matchNewAnnotation(Annotation a) {
      UseExtensionHandler extensionHandlerAnnotation = a.annotationType().getAnnotation(UseExtensionHandler.class);
      return extensionHandlerAnnotation != null && "SQL_OBJECT".equals(extensionHandlerAnnotation.id());
   }

   private static boolean matchOldAnnotation(Annotation a) {
      return a.annotationType().isAnnotationPresent(SqlOperation.class);
   }

   static Stream<Class<?>> findOldAnnotations(Method method) {
      return Stream.of(method.getAnnotations()).filter(SqlObjectAnnotationHelper::matchOldAnnotation).map(Annotation::annotationType);
   }

   static <T extends Annotation> Optional<T> findAnnotation(Class<T> annotationClass, AnnotatedElement... elements) {
      return Arrays.stream(elements).map(e -> e.getAnnotation(annotationClass)).filter(Objects::nonNull).findFirst();
   }
}
