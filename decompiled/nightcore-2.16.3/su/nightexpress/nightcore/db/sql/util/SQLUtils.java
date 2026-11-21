package su.nightexpress.nightcore.db.sql.util;

import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.db.sql.column.Column;

@Deprecated
public class SQLUtils {
   @NonNull
   public static String escape(@NonNull String string) {
      if (string.isBlank()) {
         return string;
      } else {
         return string.charAt(0) == '`' ? string : "`" + string + "`";
      }
   }

   @NonNull
   public static String forWhere(@NonNull Column column, @NonNull WhereOperator operator) {
      return column.getNameEscaped() + " " + operator.getLiteral() + " ?";
   }

   @NonNull
   public static String forWhereLowercase(@NonNull Column column, @NonNull WhereOperator operator) {
      return column.getNameLowercase() + " " + operator.getLiteral() + " ?";
   }
}
