package org.jdbi.v3.core.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.statement.StatementContext;

@FunctionalInterface
public interface ColumnMapper<T> {
   static <U> ColumnMapper<U> getDefaultColumnMapper() {
      return (r, n, c) -> (U)r.getObject(n);
   }

   T map(ResultSet r, int columnNumber, StatementContext ctx) throws SQLException;

   default T map(ResultSet r, String columnLabel, StatementContext ctx) throws SQLException {
      return this.map(r, r.findColumn(columnLabel), ctx);
   }

   default void init(ConfigRegistry registry) {
   }
}
