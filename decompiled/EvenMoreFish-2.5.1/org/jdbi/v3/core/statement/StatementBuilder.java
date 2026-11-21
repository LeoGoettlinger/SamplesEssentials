package org.jdbi.v3.core.statement;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public interface StatementBuilder {
   Statement create(Connection conn, StatementContext ctx) throws SQLException;

   PreparedStatement create(Connection conn, String sql, StatementContext ctx) throws SQLException;

   CallableStatement createCall(Connection conn, String sql, StatementContext ctx) throws SQLException;

   void close(Connection conn, String sql, Statement stmt) throws SQLException;

   default void close(Connection conn) {
   }
}
