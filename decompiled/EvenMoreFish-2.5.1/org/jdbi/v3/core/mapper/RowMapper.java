package org.jdbi.v3.core.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.statement.StatementContext;

@FunctionalInterface
public interface RowMapper<T> {
   T map(ResultSet rs, StatementContext ctx) throws SQLException;

   default RowMapper<T> specialize(ResultSet rs, StatementContext ctx) throws SQLException {
      return this;
   }

   default void init(ConfigRegistry registry) {
   }
}
