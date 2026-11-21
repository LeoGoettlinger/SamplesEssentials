package org.jdbi.v3.core.argument;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.jdbi.v3.core.statement.StatementContext;

public class NullArgument implements Argument {
   private final Integer sqlType;

   public NullArgument(int sqlType) {
      this.sqlType = sqlType;
   }

   public NullArgument(Integer sqlType) {
      this.sqlType = sqlType;
   }

   @Override
   public void apply(final int position, PreparedStatement statement, StatementContext ctx) throws SQLException {
      if (this.sqlType == null) {
         ctx.getConfig(Arguments.class).getUntypedNullArgument().apply(position, statement, ctx);
      } else {
         statement.setNull(position, this.sqlType);
      }
   }

   public Integer getSqlType() {
      return this.sqlType;
   }

   @Override
   public String toString() {
      return "NULL";
   }
}
