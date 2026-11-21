package org.jdbi.v3.core;

import java.sql.Connection;
import java.sql.SQLException;
import org.jdbi.v3.core.statement.Cleanable;

@FunctionalInterface
public interface ConnectionFactory {
   Connection openConnection() throws SQLException;

   default void closeConnection(Connection conn) throws SQLException {
      conn.close();
   }

   default Cleanable getCleanableFor(Connection conn) {
      return () -> this.closeConnection(conn);
   }
}
