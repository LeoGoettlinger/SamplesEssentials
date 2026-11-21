package org.jdbi.v3.core.statement;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class DefaultStatementBuilder implements StatementBuilder {
   public static final StatementBuilderFactory FACTORY = c -> new DefaultStatementBuilder();

   @Override
   public Statement create(Connection conn, StatementContext ctx) throws SQLException {
      return conn.createStatement();
   }

   @Override
   public PreparedStatement create(Connection conn, String sql, StatementContext ctx) throws SQLException {
      if (ctx.isReturningGeneratedKeys()) {
         String[] columnNames = ctx.getGeneratedKeysColumnNames();
         return columnNames != null && columnNames.length > 0 ? conn.prepareStatement(sql, columnNames) : conn.prepareStatement(sql, 1);
      } else {
         return ctx.isConcurrentUpdatable() ? conn.prepareStatement(sql, 1003, 1008) : conn.prepareStatement(sql, 1003, 1007);
      }
   }

   @Override
   public void close(Connection conn, String sql, Statement stmt) throws SQLException {
      if (stmt != null) {
         stmt.close();
      }
   }

   @Override
   public CallableStatement createCall(Connection conn, String sql, StatementContext ctx) throws SQLException {
      return conn.prepareCall(sql);
   }
}
