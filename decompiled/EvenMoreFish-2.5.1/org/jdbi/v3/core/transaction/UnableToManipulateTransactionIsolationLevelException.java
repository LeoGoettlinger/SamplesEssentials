package org.jdbi.v3.core.transaction;

import java.sql.SQLException;
import org.jdbi.v3.core.JdbiException;

public class UnableToManipulateTransactionIsolationLevelException extends JdbiException {
   private static final long serialVersionUID = 1L;

   public UnableToManipulateTransactionIsolationLevelException(int i, SQLException e) {
      super("Unable to set isolation level to " + i, e);
   }

   public UnableToManipulateTransactionIsolationLevelException(String msg, SQLException e) {
      super(msg, e);
   }
}
