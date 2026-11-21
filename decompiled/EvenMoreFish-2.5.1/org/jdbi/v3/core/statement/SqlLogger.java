package org.jdbi.v3.core.statement;

import java.sql.SQLException;

public interface SqlLogger {
   SqlLogger NOP_SQL_LOGGER = new SqlLogger() {};

   default void logBeforeExecution(StatementContext context) {
   }

   default void logAfterExecution(StatementContext context) {
   }

   default void logException(StatementContext context, SQLException ex) {
   }
}
