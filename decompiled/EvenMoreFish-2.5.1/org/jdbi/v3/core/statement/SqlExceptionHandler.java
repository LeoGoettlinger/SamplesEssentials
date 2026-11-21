package org.jdbi.v3.core.statement;

import java.sql.SQLException;
import org.jdbi.v3.meta.Alpha;

@FunctionalInterface
@Alpha
public interface SqlExceptionHandler {
   void handle(SQLException ex, StatementContext ctx);
}
