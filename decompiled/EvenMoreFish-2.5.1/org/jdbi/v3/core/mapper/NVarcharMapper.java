package org.jdbi.v3.core.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.jdbi.v3.core.qualifier.NVarchar;
import org.jdbi.v3.core.statement.StatementContext;

@NVarchar
class NVarcharMapper implements ColumnMapper<String> {
   public String map(ResultSet r, int columnNumber, StatementContext ctx) throws SQLException {
      return r.getNString(columnNumber);
   }
}
