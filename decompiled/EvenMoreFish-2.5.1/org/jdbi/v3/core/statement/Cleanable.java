package org.jdbi.v3.core.statement;

import java.sql.SQLException;

@FunctionalInterface
public interface Cleanable extends AutoCloseable {
   Cleanable NO_OP = () -> {};

   @Override
   void close() throws SQLException;

   default void closeAndSuppress(Throwable t) {
      try {
         this.close();
      } catch (SQLException var3) {
         t.addSuppressed(var3);
      }
   }
}
