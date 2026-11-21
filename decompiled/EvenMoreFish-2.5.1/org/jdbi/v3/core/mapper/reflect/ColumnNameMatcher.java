package org.jdbi.v3.core.mapper.reflect;

public interface ColumnNameMatcher {
   boolean columnNameMatches(String columnName, String javaName);

   default boolean columnNameStartsWith(String columnName, String prefix) {
      return columnName.startsWith(prefix);
   }
}
