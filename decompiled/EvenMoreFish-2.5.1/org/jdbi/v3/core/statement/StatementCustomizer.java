package org.jdbi.v3.core.statement;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface StatementCustomizer {
   default void beforeTemplating(PreparedStatement stmt, StatementContext ctx) throws SQLException {
   }

   default void beforeBinding(PreparedStatement stmt, StatementContext ctx) throws SQLException {
   }

   default void beforeExecution(PreparedStatement stmt, StatementContext ctx) throws SQLException {
   }

   default void afterExecution(PreparedStatement stmt, StatementContext ctx) throws SQLException {
   }
}
