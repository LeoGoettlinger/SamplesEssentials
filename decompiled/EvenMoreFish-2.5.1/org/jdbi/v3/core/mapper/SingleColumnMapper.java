package org.jdbi.v3.core.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.jdbi.v3.core.statement.StatementContext;

public class SingleColumnMapper<T> implements RowMapper<T> {
   private final RowMapper<T> delegate;

   public SingleColumnMapper(ColumnMapper<T> mapper) {
      this(mapper, 1);
   }

   public SingleColumnMapper(ColumnMapper<T> mapper, int columnNumber) {
      this.delegate = (r, ctx) -> mapper.map(r, columnNumber, ctx);
   }

   public SingleColumnMapper(ColumnMapper<T> mapper, String columnLabel) {
      this.delegate = (r, ctx) -> mapper.map(r, columnLabel, ctx);
   }

   @Override
   public T map(ResultSet r, StatementContext ctx) throws SQLException {
      return this.delegate.map(r, ctx);
   }
}
