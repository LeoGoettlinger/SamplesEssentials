package org.jdbi.v3.core.statement;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public final class StatementCustomizers {
   private StatementCustomizers() {
   }

   public static StatementCustomizer statementTimeout(final int seconds) {
      return new StatementCustomizer() {
         @Override
         public void beforeExecution(final PreparedStatement stmt, final StatementContext ctx) throws SQLException {
            stmt.setQueryTimeout(seconds);
         }
      };
   }

   public static StatementCustomizer fetchSize(final int fetchSize) {
      return new StatementCustomizer() {
         @Override
         public void beforeExecution(final PreparedStatement stmt, final StatementContext ctx) throws SQLException {
            stmt.setFetchSize(fetchSize);
         }
      };
   }

   public static StatementCustomizer maxRows(final int maxRows) {
      return new StatementCustomizer() {
         @Override
         public void beforeExecution(final PreparedStatement stmt, final StatementContext ctx) throws SQLException {
            stmt.setMaxRows(maxRows);
         }
      };
   }

   public static StatementCustomizer maxFieldSize(final int maxFieldSize) {
      return new StatementCustomizer() {
         @Override
         public void beforeExecution(final PreparedStatement stmt, final StatementContext ctx) throws SQLException {
            stmt.setMaxFieldSize(maxFieldSize);
         }
      };
   }
}
