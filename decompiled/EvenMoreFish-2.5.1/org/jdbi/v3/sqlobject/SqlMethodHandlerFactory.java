package org.jdbi.v3.sqlobject;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import org.jdbi.v3.core.extension.ExtensionHandler;
import org.jdbi.v3.core.extension.ExtensionHandlerFactory;
import org.jdbi.v3.core.internal.JdbiClassUtils;

class SqlMethodHandlerFactory implements ExtensionHandlerFactory {
   private static final Class<?>[] SQL_METHOD_HANDLER_TYPES = new Class[]{Class.class, Method.class};

   @Override
   public boolean accepts(Class<?> extensionType, Method method) {
      return !method.isBridge() && !SqlObjectAnnotationHelper.findSqlMethodAnnotations(method).isEmpty();
   }

   @Override
   public Optional<ExtensionHandler> createExtensionHandler(Class<?> sqlObjectType, Method method) {
      List<Class<?>> sqlMethodAnnotations = SqlObjectAnnotationHelper.findSqlMethodAnnotations(method);
      if (sqlMethodAnnotations.size() > 1) {
         throw new IllegalStateException(
            String.format("Mutually exclusive annotations on method %s.%s: %s", sqlObjectType.getName(), method.getName(), sqlMethodAnnotations)
         );
      } else if (method.isDefault() && !method.isSynthetic()) {
         throw new IllegalStateException(
            String.format(
               "Default method %s.%s has @%s annotation. SQL object methods may be default, or have a SQL method annotation, but not both.",
               sqlObjectType.getSimpleName(),
               method.getName(),
               sqlMethodAnnotations.get(0).getSimpleName()
            )
         );
      } else {
         return Optional.of(
            SqlObjectAnnotationHelper.findOldAnnotations(method)
               .map(type -> type.getAnnotation(SqlOperation.class))
               .map(SqlOperation::value)
               .map(
                  klass -> JdbiClassUtils.findConstructorAndCreateInstance(
                     (Class<? extends Handler>)klass, SQL_METHOD_HANDLER_TYPES, handle -> (Object)handle.invokeExact((Class)sqlObjectType, (Method)method)
                  )
               )
               .findFirst()
               .orElseThrow(
                  () -> new IllegalStateException(
                     String.format(
                        "Method %s.%s must be default or be annotated with a SQL method annotation.", sqlObjectType.getSimpleName(), method.getName()
                     )
                  )
               )
         );
      }
   }
}
