package org.jdbi.v3.core.argument;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.jdbi.v3.core.statement.StatementContext;

public class ObjectArgument implements Argument {
   private final Object value;
   private final Integer sqlType;

   @Deprecated(
      since = "3.11.0",
      forRemoval = true
   )
   public ObjectArgument(Object value, Integer sqlType) {
      this.sqlType = sqlType;
      this.value = value;
      if (sqlType == null && value == null) {
         throw new IllegalArgumentException("Null value provided without a type");
      }
   }

   public static Argument of(Object value) {
      return of(value, null);
   }

   public static Argument of(Object value, Integer sqlType) {
      return (Argument)(value == null ? new NullArgument(sqlType) : new ObjectArgument(value, sqlType));
   }

   @Override
   public void apply(final int position, PreparedStatement statement, StatementContext ctx) throws SQLException {
      if (this.value == null) {
         statement.setNull(position, this.sqlType);
      } else if (this.sqlType == null) {
         statement.setObject(position, this.value);
      } else {
         statement.setObject(position, this.value, this.sqlType);
      }
   }

   final <T> T getValue(Class<T> type) {
      return type.cast(this.value);
   }

   @Override
   public String toString() {
      return (this.value == null ? "NULL" : String.valueOf(this.value)) + (this.sqlType == null ? "" : " (type " + this.sqlType + ")");
   }
}
