package org.jdbi.v3.core.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.jdbi.v3.core.result.RowView;
import org.jdbi.v3.core.result.internal.RowViewImpl;
import org.jdbi.v3.core.statement.StatementContext;

@FunctionalInterface
public interface RowViewMapper<T> extends RowMapper<T> {
   @Override
   default T map(ResultSet rs, StatementContext ctx) throws SQLException {
      return this.map(new RowViewImpl(rs, ctx));
   }

   @Override
   default RowMapper<T> specialize(ResultSet rs, StatementContext ctx) throws SQLException {
      RowView row = new RowViewImpl(rs, ctx);
      return (x, y) -> this.map(row);
   }

   T map(RowView rowView) throws SQLException;
}
