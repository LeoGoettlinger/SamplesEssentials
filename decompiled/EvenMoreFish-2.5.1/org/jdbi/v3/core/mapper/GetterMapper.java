package org.jdbi.v3.core.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.jdbi.v3.core.statement.StatementContext;

class GetterMapper<T> implements ColumnMapper<T> {
   private final ColumnGetter<T> getter;

   GetterMapper(ColumnGetter<T> getter) {
      this.getter = getter;
   }

   @Override
   public T map(ResultSet r, int i, StatementContext ctx) throws SQLException {
      T value = this.getter.get(r, i);
      return r.wasNull() ? null : value;
   }
}
