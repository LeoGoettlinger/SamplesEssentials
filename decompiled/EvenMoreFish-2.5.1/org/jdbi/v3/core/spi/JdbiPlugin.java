package org.jdbi.v3.core.spi;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.sql.Connection;
import java.sql.SQLException;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;

public interface JdbiPlugin {
   default void customizeJdbi(Jdbi jdbi) throws SQLException {
   }

   default Handle customizeHandle(Handle handle) throws SQLException {
      return handle;
   }

   default Connection customizeConnection(Connection conn) throws SQLException {
      return conn;
   }

   @SuppressFBWarnings({"SING_SINGLETON_HAS_NONPRIVATE_CONSTRUCTOR"})
   public abstract static class Singleton implements JdbiPlugin {
      @Override
      public boolean equals(Object obj) {
         return obj != null && this.getClass().equals(obj.getClass());
      }

      @Override
      public int hashCode() {
         return this.getClass().hashCode();
      }
   }
}
