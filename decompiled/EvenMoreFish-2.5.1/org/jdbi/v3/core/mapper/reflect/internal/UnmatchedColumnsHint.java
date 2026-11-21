package org.jdbi.v3.core.mapper.reflect.internal;

import java.util.List;

public final class UnmatchedColumnsHint {
   private UnmatchedColumnsHint() {
   }

   public static String forColumns(String prefix, List<String> columnNames) {
      return prefix.isEmpty()
         ? String.format(" Result set columns: %s.", columnNames)
         : String.format(
            " Result set columns: %s. The mapper uses prefix '%2$s' and only matches columns whose label starts with it. Alias each selected column with the prefix, for example \"%2$s.id AS %2$s_id\" instead of \"%2$s.*\".",
            columnNames,
            prefix
         );
   }
}
