package org.jdbi.v3.sqlobject;

import java.util.stream.Stream;

public final class SqlObjectFactory extends AbstractSqlObjectFactory {
   public static final String EXTENSION_ID = "SQL_OBJECT";

   SqlObjectFactory() {
   }

   @Override
   public boolean accepts(Class<?> extensionType) {
      if (!extensionType.isInterface()) {
         return false;
      } else if (isConcrete(extensionType)) {
         return false;
      } else {
         return SqlObject.class.isAssignableFrom(extensionType)
            ? true
            : Stream.of(extensionType.getMethods()).flatMap(m -> Stream.of(m.getAnnotations())).anyMatch(SqlObjectAnnotationHelper::matchSqlAnnotations);
      }
   }
}
