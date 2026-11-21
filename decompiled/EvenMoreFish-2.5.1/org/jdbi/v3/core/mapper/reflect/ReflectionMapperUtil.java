package org.jdbi.v3.core.mapper.reflect;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.OptionalInt;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import org.jdbi.v3.core.internal.UtilityClassException;
import org.jdbi.v3.core.mapper.CaseStrategy;

public final class ReflectionMapperUtil {
   private ReflectionMapperUtil() {
      throw new UtilityClassException();
   }

   @Deprecated(
      since = "3.34.0",
      forRemoval = true
   )
   public static List<String> getColumnNames(ResultSet rs) throws SQLException {
      return getColumnNames(rs, CaseStrategy.LOCALE_LOWER);
   }

   public static List<String> getColumnNames(ResultSet rs, UnaryOperator<String> caseStrategy) throws SQLException {
      ResultSetMetaData metadata = rs.getMetaData();
      int count = metadata.getColumnCount();
      List<String> columnNames = new ArrayList<>(count);

      for (int i = 0; i < count; i++) {
         columnNames.add(caseStrategy.apply(metadata.getColumnLabel(i + 1)));
      }

      return columnNames;
   }

   public static OptionalInt findColumnIndex(String paramName, List<String> columnNames, List<ColumnNameMatcher> columnNameMatchers, Supplier<String> debugName) {
      OptionalInt result = OptionalInt.empty();

      for (int i = 0; i < columnNames.size(); i++) {
         String columnName = columnNames.get(i);

         for (ColumnNameMatcher strategy : columnNameMatchers) {
            if (strategy.columnNameMatches(columnName, paramName)) {
               if (result.isPresent()) {
                  throw new IllegalArgumentException(
                     String.format(
                        "'%s' (%s) matches multiple columns: '%s' (%d) and '%s' (%d)",
                        debugName.get(),
                        paramName,
                        columnNames.get(result.getAsInt()),
                        result.getAsInt(),
                        columnNames.get(i),
                        i
                     )
                  );
               }

               result = OptionalInt.of(i);
               break;
            }
         }
      }

      return result;
   }

   public static boolean anyColumnsStartWithPrefix(Collection<String> columnNames, String prefix, List<ColumnNameMatcher> columnNameMatchers) {
      return columnNames.stream().anyMatch(columnName -> columnNameMatchers.stream().anyMatch(matcher -> matcher.columnNameStartsWith(columnName, prefix)));
   }

   public static String addPropertyNamePrefix(String prefix, String name) {
      return prefix.isEmpty() ? name : prefix + "." + name;
   }
}
